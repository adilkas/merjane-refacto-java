package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void should_throw_exception_when_orderId_is_null() {
        assertThrows(IllegalArgumentException.class, () -> orderService.validateData(null));
    }

    @Test
    void should_throw_exception_when_orderId_is_negative() {
        assertThrows(IllegalArgumentException.class, () -> orderService.validateData(-1L));
    }

    @Test
    void should_process_order_successfully() {

        Long id = 1L;

        Product product = new Product();
        product.setType(ProductType.NORMAL);
        product.setAvailable(10);

        Order order = new Order();
        order.setId(id);
        order.setItems(Set.of(product));

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        ProcessOrderResponse result = orderService.processOrder(id);

        assertEquals(id, result.getId());
    }

    @Test
    void should_decrease_stock_for_normal_product() {

        Long id = 1L;

        Product product = new Product();
        product.setType(ProductType.NORMAL);
        product.setAvailable(5);

        Order order = new Order();
        order.setId(id);
        order.setItems(Set.of(product));

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        orderService.processOrder(id);

        assertEquals(4, product.getAvailable());
    }
}