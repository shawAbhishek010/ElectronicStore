package com.lcwd.electronicStore.ElectronicStore.dtos;

/*
Purpose:
Represents admin dashboard metrics such as sales, revenue, orders, and best sellers.
*/
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnalyticsDashboardDto {

    private long totalSales;
    private long weeklySales;
    private long monthlySales;
    private long numberOfOrders;
    private long revenue;
    private List<BestSellingProductDto> bestSellingProducts;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BestSellingProductDto {
        private String productId;
        private String title;
        private long quantitySold;
        private long revenue;
    }
}
