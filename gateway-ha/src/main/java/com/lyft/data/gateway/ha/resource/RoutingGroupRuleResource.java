package com.lyft.data.gateway.ha.resource;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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


}
