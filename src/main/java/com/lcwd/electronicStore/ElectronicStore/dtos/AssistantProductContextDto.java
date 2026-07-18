package com.lcwd.electronicStore.ElectronicStore.dtos;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/*
Purpose:
Carries the small product snapshot selected by the frontend for assistant answers.
*/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AssistantProductContextDto {
    private String id;

    @Size(max = 120)
    private String title;

    @Size(max = 80)
    private String category;

    @Size(max = 260)
    private String description;

    private long price;
    private long originalPrice;
    private int discountPercent;
    private boolean stock;
    private int quantity;
    private List<String> signals;
}
