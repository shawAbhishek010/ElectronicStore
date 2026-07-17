package com.lcwd.electronicStore.ElectronicStore.config;
/*
Purpose:
Defines shared application beans like ModelMapper for DTO/entity conversion.
*/
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
