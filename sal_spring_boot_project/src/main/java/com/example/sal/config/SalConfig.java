package com.example.sal.config;

import com.example.sal.entitlements.EntitlementsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({EntitlementsProperties.class})
public class SalConfig {}
