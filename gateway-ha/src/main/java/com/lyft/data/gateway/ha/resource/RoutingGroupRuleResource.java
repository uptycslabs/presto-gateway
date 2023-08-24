package com.lyft.data.gateway.ha.resource;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import com.lyft.data.gateway.ha.router.RoutingGroupRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

@Slf4j
@Path("routing")
@Produces(MediaType.APPLICATION_JSON)
public class RoutingGroupRuleResource {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Inject
    RoutingGroupRuleManager routingGroupRuleManager;
    @GET
    public Response ok(@Context Request request) {
        return Response.ok("ok").build();
    }


    @GET
    @Path("/rules/all")
    public Response getAllRoutingRules() {
        return Response.ok(this.routingGroupRuleManager.getRulesJsonAsString()).build();
    }


    @Path("/rules/add")
    @POST
    public Response addRoutingRule(String jsonPayload) {
        Map<String, Object> rule;
        try {
            rule = OBJECT_MAPPER.readValue(jsonPayload, Map.class);

            routingGroupRuleManager.createNewRule(rule);
            return Response.ok(jsonPayload).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Path("/rules/update")
    @POST
    public Response updateRoutingRule(String jsonPayload) {
        Map<String, Object> rule;
        try {
            rule = OBJECT_MAPPER.readValue(jsonPayload, Map.class);

            routingGroupRuleManager.updateRuleDetail(rule);
            return Response.ok(jsonPayload).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Path("/rules/delete")
    @POST
    public Response deleteRoutingRule(String name) {
        routingGroupRuleManager.deleteRoutingRule(name);
        return Response.ok().build();
    }

    @POST
    @Path("/rules/deactivate/{name}")
    public Response deactivateRules(@PathParam("name") String name) {
        try {
            this.routingGroupRuleManager.deactivateRule(name);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return throwError(e);
        }
        return Response.ok().build();
    }

    @POST
    @Path("/rules/activate/{name}")
    public Response activateBackend(@PathParam("name") String name) {
        try {
            this.routingGroupRuleManager.activateRule(name);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return throwError(e);
        }
        return Response.ok().build();
    }

    private Response throwError(Exception e) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(e.getMessage())
                .type("text/plain")
                .build();
    }


}
