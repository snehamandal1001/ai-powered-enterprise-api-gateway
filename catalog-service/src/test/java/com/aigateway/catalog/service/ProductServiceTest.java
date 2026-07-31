package com.aigateway.catalog.service;

import com.aigateway.catalog.dto.ProductRequest;
import com.aigateway.catalog.dto.ProductResponse;
import com.aigateway.catalog.exception.ProductNotFoundException;
import com.aigateway.catalog.model.Product;
import com.aigateway.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new ProductRequest();
        sampleRequest.setName("Wireless Mouse");
        sampleRequest.setDescription("Ergonomic mouse");
        sampleRequest.setPrice(new BigDecimal("19.99"));
        sampleRequest.setStockQuantity(100);
    }

    @Test
    void createProduct_savesAndReturnsProduct_whenNameIsUnique() {
        when(productRepository.existsByNameIgnoreCase("Wireless Mouse")).thenReturn(false);
        Product saved = new Product("Wireless Mouse", "Ergonomic mouse", new BigDecimal("19.99"), 100);
        saved.setId(1L);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.createProduct(sampleRequest);

        assertEquals("Wireless Mouse", response.getName());
        assertEquals(1L, response.getId());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_throwsException_whenNameAlreadyExists() {
        when(productRepository.existsByNameIgnoreCase("Wireless Mouse")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(sampleRequest));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductById_throwsNotFoundException_whenProductMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductById(99L));
    }

    @Test
    void reserveStock_returnsFalse_whenInsufficientStock() {
        Product product = new Product("Wireless Mouse", "desc", new BigDecimal("19.99"), 5);
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        boolean reserved = productService.reserveStock(1L, 10);

        assertFalse(reserved);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void reserveStock_decrementsStockAndReturnsTrue_whenStockSufficient() {
        Product product = new Product("Wireless Mouse", "desc", new BigDecimal("19.99"), 50);
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        boolean reserved = productService.reserveStock(1L, 10);

        assertTrue(reserved);
        assertEquals(40, product.getStockQuantity());
    }
}
