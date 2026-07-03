package com.example.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Provider
public class ApiExceptionHandler implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<Map<String, String>> errors = exception.getConstraintViolations().stream()
            .map(violation -> Map.of(
                "field", violation.getPropertyPath().toString(),
                "message", violation.getMessage()))
            .toList();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", "Validation failed");
        payload.put("errors", errors);

        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(payload)
            .build();
    }
}
