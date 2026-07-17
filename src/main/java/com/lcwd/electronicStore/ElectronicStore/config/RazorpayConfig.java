package com.lcwd.electronicStore.ElectronicStore.config;

/*
Purpose:
Loads Razorpay payment settings from application properties and environment variables.
*/
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "razorpay")
public class RazorpayConfig {

    private String keyId;
    private String keySecret;
    private String currency = "INR";
}
