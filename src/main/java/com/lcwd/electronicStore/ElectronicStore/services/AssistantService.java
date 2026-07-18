package com.lcwd.electronicStore.ElectronicStore.services;

import com.lcwd.electronicStore.ElectronicStore.dtos.AssistantChatRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.AssistantChatResponse;

public interface AssistantService {
    AssistantChatResponse chat(AssistantChatRequest request);
}
