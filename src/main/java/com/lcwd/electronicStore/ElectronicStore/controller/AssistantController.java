package com.lcwd.electronicStore.ElectronicStore.controller;

import com.lcwd.electronicStore.ElectronicStore.dtos.AssistantChatRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.AssistantChatResponse;
import com.lcwd.electronicStore.ElectronicStore.services.AssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
Purpose:
Exposes a small authenticated proxy for the storefront assistant.
*/
@RestController
@RequestMapping("/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/chat")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AssistantChatResponse> chat(@Valid @RequestBody AssistantChatRequest request) {
        return ResponseEntity.ok(assistantService.chat(request));
    }
}
