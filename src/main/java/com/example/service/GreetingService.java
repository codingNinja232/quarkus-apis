package com.example.service;

import com.example.dto.GreetingResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GreetingService {

    public GreetingResponse buildGreeting(String name) {
        return new GreetingResponse("Hello " + name.trim());
    }
}
