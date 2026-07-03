package com.example;

import com.example.dto.GreetingResponse;
import com.example.service.GreetingService;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class GreetingResource {

    private final GreetingService greetingService;

    public GreetingResource(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GET
    @Path("/health")
    @Produces(MediaType.TEXT_PLAIN)
    public Response health() {
        return Response.ok().build();
    }

    @GET
    @Path("/greeting")
    @Produces(MediaType.APPLICATION_JSON)
    public GreetingResponse greet(@QueryParam("name") @NotBlank String name) {
        return greetingService.buildGreeting(name);
    }
}
