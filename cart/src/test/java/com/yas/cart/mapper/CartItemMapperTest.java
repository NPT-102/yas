package com.yas.cart.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.yas.cart.model.CartItem;
import com.yas.cart.viewmodel.CartItemGetVm;
import com.yas.cart.viewmodel.CartItemPostVm;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartItemMapperTest {

    private CartItemMapper cartItemMapper;

    private static final String TEST_CUSTOMER_ID = "customer123";
    private static final Long TEST_PRODUCT_ID = 100L;
    private static final Integer TEST_QUANTITY = 5;

    @BeforeEach
    void setUp() {
        cartItemMapper = new CartItemMapper();
    }

    @Test
    void testToGetVm_whenValidCartItem_shouldMapCorrectly() {
        // Given
        CartItem cartItem = CartItem.builder()
            .customerId(TEST_CUSTOMER_ID)
            .productId(TEST_PRODUCT_ID)
            .quantity(TEST_QUANTITY)
            .build();

        // When
        CartItemGetVm result = cartItemMapper.toGetVm(cartItem);

        // Then
        assertNotNull(result);
        assertEquals(TEST_CUSTOMER_ID, result.customerId());
        assertEquals(TEST_PRODUCT_ID, result.productId());
        assertEquals(TEST_QUANTITY, result.quantity());
    }

    @Test
    void testToGetVm_whenCartItemWithMinimalData_shouldMapCorrectly() {
        // Given
        CartItem cartItem = CartItem.builder()
            .customerId("user1")
            .productId(1L)
            .quantity(1)
            .build();

        // When
        CartItemGetVm result = cartItemMapper.toGetVm(cartItem);

        // Then
        assertNotNull(result);
        assertEquals("user1", result.customerId());
        assertEquals(1L, result.productId());
        assertEquals(1, result.quantity());
    }

    @Test
    void testToCartItem_fromCartItemPostVm_shouldMapCorrectly() {
        // Given
        CartItemPostVm cartItemPostVm = new CartItemPostVm(TEST_PRODUCT_ID, TEST_QUANTITY);

        // When
        CartItem result = cartItemMapper.toCartItem(cartItemPostVm, TEST_CUSTOMER_ID);

        // Then
        assertNotNull(result);
        assertEquals(TEST_CUSTOMER_ID, result.getCustomerId());
        assertEquals(TEST_PRODUCT_ID, result.getProductId());
        assertEquals(TEST_QUANTITY, result.getQuantity());
    }

    @Test
    void testToCartItem_fromCartItemPostVmWithQuantityOne_shouldMapCorrectly() {
        // Given
        CartItemPostVm cartItemPostVm = new CartItemPostVm(200L, 1);
        String userId = "testUser";

        // When
        CartItem result = cartItemMapper.toCartItem(cartItemPostVm, userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getCustomerId());
        assertEquals(200L, result.getProductId());
        assertEquals(1, result.getQuantity());
    }

    @Test
    void testToCartItem_fromParameters_shouldMapCorrectly() {
        // Given
        String customerId = "customer456";
        Long productId = 999L;
        int quantity = 10;

        // When
        CartItem result = cartItemMapper.toCartItem(customerId, productId, quantity);

        // Then
        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals(productId, result.getProductId());
        assertEquals(quantity, result.getQuantity());
    }

    @Test
    void testToCartItem_withLargeQuantity_shouldMapCorrectly() {
        // Given
        String customerId = "bulkBuyer";
        Long productId = 50L;
        int quantity = 1000;

        // When
        CartItem result = cartItemMapper.toCartItem(customerId, productId, quantity);

        // Then
        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals(productId, result.getProductId());
        assertEquals(quantity, result.getQuantity());
    }

    @Test
    void testToGetVms_whenMultipleCartItems_shouldMapAll() {
        // Given
        CartItem cartItem1 = CartItem.builder()
            .customerId(TEST_CUSTOMER_ID)
            .productId(1L)
            .quantity(2)
            .build();

        CartItem cartItem2 = CartItem.builder()
            .customerId(TEST_CUSTOMER_ID)
            .productId(2L)
            .quantity(3)
            .build();

        CartItem cartItem3 = CartItem.builder()
            .customerId(TEST_CUSTOMER_ID)
            .productId(3L)
            .quantity(5)
            .build();

        List<CartItem> cartItems = Arrays.asList(cartItem1, cartItem2, cartItem3);

        // When
        List<CartItemGetVm> result = cartItemMapper.toGetVms(cartItems);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        assertEquals(1L, result.get(0).productId());
        assertEquals(2, result.get(0).quantity());

        assertEquals(2L, result.get(1).productId());
        assertEquals(3, result.get(1).quantity());

        assertEquals(3L, result.get(2).productId());
        assertEquals(5, result.get(2).quantity());
    }

    @Test
    void testToGetVms_whenEmptyList_shouldReturnEmptyList() {
        // Given
        List<CartItem> emptyList = Collections.emptyList();

        // When
        List<CartItemGetVm> result = cartItemMapper.toGetVms(emptyList);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToGetVms_whenSingleCartItem_shouldMapCorrectly() {
        // Given
        CartItem cartItem = CartItem.builder()
            .customerId("singleUser")
            .productId(777L)
            .quantity(7)
            .build();

        List<CartItem> singleItemList = Collections.singletonList(cartItem);

        // When
        List<CartItemGetVm> result = cartItemMapper.toGetVms(singleItemList);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("singleUser", result.get(0).customerId());
        assertEquals(777L, result.get(0).productId());
        assertEquals(7, result.get(0).quantity());
    }

    @Test
    void testToGetVms_preservesOrder() {
        // Given
        CartItem cartItem1 = CartItem.builder()
            .customerId("user")
            .productId(10L)
            .quantity(1)
            .build();

        CartItem cartItem2 = CartItem.builder()
            .customerId("user")
            .productId(20L)
            .quantity(2)
            .build();

        List<CartItem> cartItems = Arrays.asList(cartItem1, cartItem2);

        // When
        List<CartItemGetVm> result = cartItemMapper.toGetVms(cartItems);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).productId());
        assertEquals(20L, result.get(1).productId());
    }
}
