package com.example.controller;

import com.example.model.SearchRequest;
import com.example.service.GenericSearchService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/generic")
public class GenericResource {

    private final GenericSearchService genericSearchService;

    public GenericResource(GenericSearchService genericSearchService) {
        this.genericSearchService = genericSearchService;
    }

    @POST
    @Path("/{resourceName}/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response search(@PathParam("resourceName") String resourceName, SearchRequest request) {
        List<?> results = genericSearchService.search(resourceName, request);
        return Response.ok(results).build();
    }
}
