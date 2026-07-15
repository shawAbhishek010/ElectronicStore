package com.lcwd.electronicStore.ElectronicStore.controller;

import com.lcwd.electronicStore.ElectronicStore.dtos.ProductDto;
import com.lcwd.electronicStore.ElectronicStore.entities.Product;
import com.lcwd.electronicStore.ElectronicStore.entities.ProductView;
import com.lcwd.electronicStore.ElectronicStore.entities.User;
import com.lcwd.electronicStore.ElectronicStore.exceptions.ResourceNotFoundException;
import com.lcwd.electronicStore.ElectronicStore.repositories.ProductRepository;
import com.lcwd.electronicStore.ElectronicStore.repositories.ProductViewRepository;
import com.lcwd.electronicStore.ElectronicStore.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/product-views")
public class ProductViewController {

    private final ProductViewRepository productViewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public ProductViewController(ProductViewRepository productViewRepository,
                                 UserRepository userRepository,
                                 ProductRepository productRepository,
                                 ModelMapper modelMapper) {
        this.productViewRepository = productViewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<ProductDto>> getRecentlyViewed(@PathVariable String userId) {
        User user = getUser(userId);
        List<ProductDto> products = productViewRepository.findTop20ByUserOrderByViewedAtDesc(user)
                .stream()
                .map(view -> modelMapper.map(view.getProduct(), ProductDto.class))
                .toList();
        return ResponseEntity.ok(products);
    }

    @PostMapping("/{userId}/products/{productId}")
    public ResponseEntity<ProductDto> trackProductView(@PathVariable String userId, @PathVariable String productId) {
        User user = getUser(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in database !!"));

        ProductView productView = productViewRepository.findByUserUserIdAndProductProductId(userId, productId)
                .orElseGet(() -> ProductView.builder()
                        .user(user)
                        .product(product)
                        .viewCount(0)
                        .build());

        productView.setViewedAt(LocalDateTime.now());
        productView.setViewCount(productView.getViewCount() + 1);
        productViewRepository.save(productView);

        return ResponseEntity.ok(modelMapper.map(product, ProductDto.class));
    }

    private User getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found in database !!"));
    }
}
