pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK21'
    }

    options {
        skipDefaultCheckout(true)
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        MAVEN_OPTS = "-Dmaven.repo.local=.m2/repository"
        COVERAGE_THRESHOLD = "0.70"
        SONAR_PROJECT_KEY = "NPT-102_yas"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changed Services') {
            steps {
                script {
                    def changedFiles = sh(
                        script: "git diff --name-only origin/${env.CHANGE_TARGET ?: 'main'} 2>/dev/null || echo 'all'",
                        returnStdout: true
                    ).trim()

                    echo "Changed files:\n${changedFiles}"

                    def allServices = [
                        'common-library', 'backoffice-bff', 'cart', 'customer', 
                        'inventory', 'location', 'media', 'order', 'payment-paypal', 
                        'payment', 'product', 'promotion', 'rating', 'search', 
                        'storefront-bff', 'tax', 'webhook', 'sampledata', 
                        'recommendation', 'delivery'
                    ]

                    def services = []

                    if (changedFiles == 'all' || changedFiles.contains('pom.xml')) {
                        services = allServices
                        echo "Building all services due to root pom.xml change or initial build"
                    } else {
                        allServices.each { service ->
                            if (changedFiles.contains("${service}/")) {
                                services.add(service)
                            }
                        }

                        if (services.isEmpty()) {
                            echo "No backend services changed, building all services"
                            services = allServices
                        }
                    }

                    env.SERVICES = services.join(",")
                    echo "Services to build: ${env.SERVICES}"
                }
            }
        }

        stage('Security - Gitleaks') {
            steps {
                script {
                    def exitCode = sh(
                        script: 'gitleaks detect --source . --exit-code 1 || true',
                        returnStatus: true
                    )
                    if (exitCode != 0) {
                        echo "⚠️ Gitleaks found issues, but continuing build"
                    }
                }
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    echo "Building and testing services: ${env.SERVICES}"
                    sh "mvn clean verify -pl ${env.SERVICES} -am -DskipITs -Dmaven.test.failure.ignore=true"
                }
            }
        }

        stage('Generate Aggregate Coverage Report') {
            steps {
                script {
                    echo "Generating aggregate coverage report for all modules"
                    sh "mvn jacoco:report-aggregate -DskipTests"
                }
            }
        }

        stage('Publish Test Results') {
            steps {
                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml, **/target/failsafe-reports/*.xml'
                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/site/jacoco-aggregate',
                    reportFiles: 'index.html',
                    reportName: 'JaCoCo Coverage Report',
                    reportTitles: 'Code Coverage'
                ])
            }
        }

        stage('SonarQube Analysis') {
            when {
                not { buildingTag() }
            }
            steps {
                withSonarQubeEnv('SonarCloud') {
                    sh """
                    mvn org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar \
                    -Dsonar.projectKey=NPT-102_yas \
                    -Dsonar.organization=npt-102 \
                    -Dsonar.host.url=https://sonarcloud.io
                    """
                }
            }
        }



        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Snyk Scan') {
            steps {
                withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                    sh '''
                        echo "Token length:"
                        echo ${#SNYK_TOKEN}
                        snyk test --maven-aggregate-project
                    '''
                }
            }
        }



        stage('Release Build (Tag Only)') {
            when {
                buildingTag()
            }
            steps {
                sh 'mvn clean package -DskipTests'
                archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
            }
        }
    }

    post {
        success {
            echo "✓ Pipeline completed successfully"
        }

        failure {
            echo "✗ Pipeline failed"
        }

        always {
            cleanWs()
        }
    }
}
