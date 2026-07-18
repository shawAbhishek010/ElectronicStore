package com.lcwd.electronicStore.ElectronicStore.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
Purpose:
Returns the assistant's concise answer to the storefront.
*/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AssistantChatResponse {
    private String answer;
    private String provider;
    private String model;
    private String responseId;
}
