package com.lcwd.electronicStore.ElectronicStore.controller;

/*
Purpose:
Provides admin-only sales, revenue, order, and best-seller analytics.
*/
import com.lcwd.electronicStore.ElectronicStore.dtos.AnalyticsDashboardDto;
import com.lcwd.electronicStore.ElectronicStore.entities.Order;
import com.lcwd.electronicStore.ElectronicStore.entities.OrderItem;
import com.lcwd.electronicStore.ElectronicStore.repositories.OrderRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/analytics")
public class AdminAnalyticsController {

    private final OrderRepository orderRepository;

    public AdminAnalyticsController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AnalyticsDashboardDto getDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.minusDays(7);
        LocalDateTime monthStart = now.minusMonths(1);

        List<Order> paidOrders = orderRepository.findAll().stream()
                .filter(order -> "PAID".equals(order.getPaymentStatus()))
                .toList();

        long revenue = paidOrders.stream().mapToLong(Order::getOrderAmount).sum();
        long weeklySales = paidOrders.stream()
                .filter(order -> order.getOrderedDate() != null && !order.getOrderedDate().isBefore(weekStart))
                .mapToLong(Order::getOrderAmount)
                .sum();
        long monthlySales = paidOrders.stream()
                .filter(order -> order.getOrderedDate() != null && !order.getOrderedDate().isBefore(monthStart))
                .mapToLong(Order::getOrderAmount)
                .sum();

        return AnalyticsDashboardDto.builder()
                .totalSales(revenue)
                .weeklySales(weeklySales)
                .monthlySales(monthlySales)
                .numberOfOrders(paidOrders.size())
                .revenue(revenue)
                .bestSellingProducts(bestSellingProducts(paidOrders))
                .build();
    }

    private List<AnalyticsDashboardDto.BestSellingProductDto> bestSellingProducts(List<Order> paidOrders) {
        Map<String, ProductSales> productSales = new HashMap<>();

        paidOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .filter(item -> item.getProduct() != null)
                .forEach(item -> addProductSale(productSales, item));

        return productSales.values().stream()
                .sorted(Comparator.comparingLong(ProductSales::quantitySold).reversed())
                .limit(5)
                .map(sale -> AnalyticsDashboardDto.BestSellingProductDto.builder()
                        .productId(sale.productId())
                        .title(sale.title())
                        .quantitySold(sale.quantitySold())
                        .revenue(sale.revenue())
                        .build())
                .toList();
    }

    private void addProductSale(Map<String, ProductSales> productSales, OrderItem item) {
        String productId = item.getProduct().getProductId();
        ProductSales existing = productSales.get(productId);
        ProductSales next = new ProductSales(
                productId,
                item.getProduct().getTitle(),
                (existing == null ? 0 : existing.quantitySold()) + item.getQuantity(),
                (existing == null ? 0 : existing.revenue()) + item.getTotalPrice()
        );
        productSales.put(productId, next);
    }

    private record ProductSales(String productId, String title, long quantitySold, long revenue) {
    }
}
