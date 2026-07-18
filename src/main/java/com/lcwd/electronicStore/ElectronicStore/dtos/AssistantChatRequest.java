package com.lcwd.electronicStore.ElectronicStore.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/*
Purpose:
Receives a user question and compact catalog context for the shopping assistant.
*/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AssistantChatRequest {
    @NotBlank(message = "Question is required")
    @Size(max = 500, message = "Question must be 500 characters or less")
    private String question;

    @Valid
    @Size(max = 40, message = "Assistant can compare up to 40 products at a time")
    private List<AssistantProductContextDto> products = new ArrayList<>();
}
