package com.yas.customer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.yas.commonlibrary.exception.AccessDeniedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.customer.model.UserAddress;
import com.yas.customer.repository.UserAddressRepository;
import com.yas.customer.viewmodel.address.ActiveAddressVm;
import com.yas.customer.viewmodel.address.AddressDetailVm;
import com.yas.customer.viewmodel.address.AddressPostVm;
import com.yas.customer.viewmodel.address.AddressVm;
import com.yas.customer.viewmodel.useraddress.UserAddressVm;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserAddressService userAddressService;

    private static final String TEST_USER_ID = "testUser123";
    private static final Long TEST_ADDRESS_ID = 1L;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    void testGetUserAddressList_whenAuthenticated_shouldReturnAddressList() {
        // Given
        when(authentication.getName()).thenReturn(TEST_USER_ID);

        UserAddress userAddress1 = UserAddress.builder()
            .userId(TEST_USER_ID)
            .addressId(1L)
            .isActive(true)
            .build();

        UserAddress userAddress2 = UserAddress.builder()
            .userId(TEST_USER_ID)
            .addressId(2L)
            .isActive(false)
            .build();

        List<UserAddress> userAddresses = Arrays.asList(userAddress1, userAddress2);

        AddressDetailVm addressDetail1 = new AddressDetailVm(
            1L, "John Doe", "123456789", "123 Main St", "City1",
            "12345", 1L, "District1", 1L, "State1", 1L, "Country1"
        );

        AddressDetailVm addressDetail2 = new AddressDetailVm(
            2L, "Jane Doe", "987654321", "456 Oak Ave", "City2",
            "54321", 2L, "District2", 2L, "State2", 2L, "Country2"
        );

        List<AddressDetailVm> addressDetails = Arrays.asList(addressDetail1, addressDetail2);

        when(userAddressRepository.findAllByUserId(TEST_USER_ID)).thenReturn(userAddresses);
        when(locationService.getAddressesByIdList(anyList())).thenReturn(addressDetails);

        // When
        List<ActiveAddressVm> result = userAddressService.getUserAddressList();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).isActive()); // Active address should be first
        verify(userAddressRepository).findAllByUserId(TEST_USER_ID);
        verify(locationService).getAddressesByIdList(anyList());
    }

    @Test
    void testGetUserAddressList_whenAnonymousUser_shouldThrowAccessDeniedException() {
        // Given
        when(authentication.getName()).thenReturn("anonymousUser");

        // When & Then
        assertThrows(AccessDeniedException.class, () -> userAddressService.getUserAddressList());
        verify(userAddressRepository, never()).findAllByUserId(anyString());
    }

    @Test
    void testGetAddressDefault_whenAuthenticated_shouldReturnDefaultAddress() {
        // Given
        when(authentication.getName()).thenReturn(TEST_USER_ID);

        UserAddress userAddress = UserAddress.builder()
            .userId(TEST_USER_ID)
            .addressId(TEST_ADDRESS_ID)
            .isActive(true)
            .build();

        AddressDetailVm addressDetail = new AddressDetailVm(
            TEST_ADDRESS_ID, "John Doe", "123456789", "123 Main St", "City",
            "12345", 1L, "District", 1L, "State", 1L, "Country"
        );

        when(userAddressRepository.findByUserIdAndIsActiveTrue(TEST_USER_ID))
            .thenReturn(Optional.of(userAddress));
        when(locationService.getAddressById(TEST_ADDRESS_ID)).thenReturn(addressDetail);

        // When
        AddressDetailVm result = userAddressService.getAddressDefault();

        // Then
        assertNotNull(result);
        assertEquals(TEST_ADDRESS_ID, result.id());
        assertEquals("John Doe", result.contactName());
        verify(userAddressRepository).findByUserIdAndIsActiveTrue(TEST_USER_ID);
        verify(locationService).getAddressById(TEST_ADDRESS_ID);
    }

    @Test
    void testGetAddressDefault_whenNoDefaultAddress_shouldThrowNotFoundException() {
        // Given
        when(authentication.getName()).thenReturn(TEST_USER_ID);
        when(userAddressRepository.findByUserIdAndIsActiveTrue(TEST_USER_ID))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> userAddressService.getAddressDefault());
    }

    @Test
    void testGetAddressDefault_whenAnonymousUser_shouldThrowAccessDeniedException() {
        // Given
        when(authentication.getName()).thenReturn("anonymousUser");

        // When & Then
        assertThrows(AccessDeniedException.class, () -> userAddressService.getAddressDefault());
    }

    @Test
    void testCreateAddress_whenFirstAddress_shouldSetAsActive() {
        // Given
        when(authentication.getName()).thenReturn(TEST_USER_ID);

        AddressPostVm addressPostVm = new AddressPostVm(
            "John Doe", "123456789", "123 Main St", "City",
            "12345", 1L, 1L, 1L
        );

        AddressVm addressVm = new AddressVm(1L, "John Doe", "123456789", "123 Main St",
            "City", "12345", 1L, 1L, 1L);

        UserAddress savedUserAddress = UserAddress.builder()
            .userId(TEST_USER_ID)
            .addressId(1L)
            .isActive(true)
            .build();

        when(userAddressRepository.findAllByUserId(TEST_USER_ID)).thenReturn(Collections.emptyList());
        when(locationService.createAddress(addressPostVm)).thenReturn(addressVm);
        when(userAddressRepository.save(any(UserAddress.class))).thenReturn(savedUserAddress);

        // When
        UserAddressVm result = userAddressService.createAddress(addressPostVm);

        // Then
        assertNotNull(result);
        verify(locationService).createAddress(addressPostVm);
        verify(userAddressRepository).save(argThat(userAddress ->
            userAddress.getUserId().equals(TEST_USER_ID) &&
            userAddress.getIsActive() == true
        ));
    }

    @Test
    void testCreateAddress_whenNotFirstAddress_shouldSetAsInactive() {
        // Given
        when(authentication.getName()).thenReturn(TEST_USER_ID);

        AddressPostVm addressPostVm = new AddressPostVm(
            "Jane Doe", "987654321", "456 Oak Ave", "City2",
            "54321", 2L, 2L, 2L
        );

        AddressVm addressVm = new AddressVm(2L, "Jane Doe", "987654321", "456 Oak Ave",
            "City2", "54321", 2L, 2L, 2L);

        UserAddress existingAddress = UserAddress.builder()
            .userId(TEST_USER_ID)
            .addressId(1L)
            .isActive(true)
            .build();

        UserAddress savedUserAddress = UserAddress.builder()
            .userId(TEST_USER_ID)
            .addressId(2L)
            .isActive(false)
            .build();

        when(userAddressRepository.findAllByUserId(TEST_USER_ID))
            .thenReturn(Collections.singletonList(existingAddress));
        when(locationService.createAddress(addressPostVm)).thenReturn(addressVm);
        when(userAddressRepository.save(any(UserAddress.class))).thenReturn(savedUserAddress);

        // When
        UserAddressVm result = userAddressService.createAddress(addressPostVm);

        // Then
        assertNotNull(result);
        verify(userAddressRepository).save(argThat(userAddress ->
            userAddress.getUserId().equals(TEST_USER_ID) &&
            userAddress.getIsActive() == false
        ));
    }

    @Test
    void testDeleteAddress_whenAddressExists_shouldDeleteSuccessfully() {
        // Given
        when(authentication.getName()).thenReturn(TEST_USER_ID);

        UserAddress userAddress = UserAddress.builder()
            .userId(TEST_USER_ID)
            .addressId(TEST_ADDRESS_ID)
            .isActive(false)
            .build();

        when(userAddressRepository.findOneByUserIdAndAddressId(TEST_USER_ID, TEST_ADDRESS_ID))
            .thenReturn(userAddress);

        // When
        userAddressService.deleteAddress(TEST_ADDRESS_ID);

        // Then
        verify(userAddressRepository).findOneByUserIdAndAddressId(TEST_USER_ID, TEST_ADDRESS_ID);
        verify(userAddressRepository).delete(userAddress);
    }

    @Test
    void testDeleteAddress_whenAddressNotFound_shouldThrowNotFoundException() {
        // Given
        when(authentication.getName()).thenReturn(TEST_USER_ID);
        when(userAddressRepository.findOneByUserIdAndAddressId(TEST_USER_ID, TEST_ADDRESS_ID))
            .thenReturn(null);

        // When & Then
        assertThrows(NotFoundException.class, () -> userAddressService.deleteAddress(TEST_ADDRESS_ID));
        verify(userAddressRepository, never()).delete(any());
    }

    @Test
    void testChooseDefaultAddress_shouldSetCorrectAddressAsActive() {
        // Given
        when(authentication.getName()).thenReturn(TEST_USER_ID);

        UserAddress address1 = UserAddress.builder()
            .userId(TEST_USER_ID)
            .addressId(1L)
            .isActive(true)
            .build();

        UserAddress address2 = UserAddress.builder()
            .userId(TEST_USER_ID)
            .addressId(2L)
            .isActive(false)
            .build();

        UserAddress address3 = UserAddress.builder()
            .userId(TEST_USER_ID)
            .addressId(3L)
            .isActive(false)
            .build();

        List<UserAddress> userAddresses = Arrays.asList(address1, address2, address3);

        when(userAddressRepository.findAllByUserId(TEST_USER_ID)).thenReturn(userAddresses);

        // When
        userAddressService.chooseDefaultAddress(2L);

        // Then
        assertFalse(address1.getIsActive());
        assertTrue(address2.getIsActive());
        assertFalse(address3.getIsActive());
        verify(userAddressRepository).saveAll(userAddresses);
    }

    @Test
    void testChooseDefaultAddress_whenSameAddress_shouldKeepItActive() {
        // Given
        when(authentication.getName()).thenReturn(TEST_USER_ID);

        UserAddress address1 = UserAddress.builder()
            .userId(TEST_USER_ID)
            .addressId(TEST_ADDRESS_ID)
            .isActive(true)
            .build();

        List<UserAddress> userAddresses = Collections.singletonList(address1);

        when(userAddressRepository.findAllByUserId(TEST_USER_ID)).thenReturn(userAddresses);

        // When
        userAddressService.chooseDefaultAddress(TEST_ADDRESS_ID);

        // Then
        assertTrue(address1.getIsActive());
        verify(userAddressRepository).saveAll(userAddresses);
    }
}
