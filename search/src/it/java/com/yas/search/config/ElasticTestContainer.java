package com.yas.search.config;

import java.time.Duration;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class ElasticTestContainer extends ElasticsearchContainer {

    private static final String IMAGE_NAME = "docker.elastic.co/elasticsearch/elasticsearch:%s";

    private static final String CLUSTER_NAME = "cluster.name";

    private static final String ELASTIC_SEARCH = "elasticsearch";

    public ElasticTestContainer(String version) {
        super(IMAGE_NAME.formatted(version));
        this.addEnv(CLUSTER_NAME, ELASTIC_SEARCH);
        this.withEnv("ES_JAVA_OPTS", "-Xms256m -Xmx256m");
        this.withEnv("xpack.security.enabled", "false");
        this.withEnv("xpack.security.transport.ssl.enabled", "false");
        this.withEnv("xpack.security.http.ssl.enabled", "false");
        this.waitingFor(Wait.forHttp("/").forPort(9200).forStatusCode(200));
        this.withStartupTimeout(Duration.ofMinutes(3));
    }
}
