package com.ap10er.yandex_disk.java.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientConfiguration
{

    @Bean
    public RestTemplate restTemplate()
    {
        return new RestTemplate();
    }

}
