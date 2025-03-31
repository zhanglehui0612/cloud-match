package com.cloud.match.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Data
public class CloudConfig {

    @Value("${cloud.match.symbols}")
    private List<String> symbols;


}
