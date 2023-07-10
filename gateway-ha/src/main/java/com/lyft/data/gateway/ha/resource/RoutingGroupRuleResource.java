package com.lyft.data.gateway.ha.resource;


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

@Slf4j
@Path("routing")
@Produces(MediaType.APPLICATION_JSON)
public class RoutingGroupRuleResource {

    @Inject RoutingGroupRuleManager routingGroupRuleManager;

    @GET
    public Response ok(@Context Request request) {
        return Response.ok("ok").build();
    }


    @GET
    @Path("/rules/all")
    public Response getAllRoutingRules() {

        String rules = this.routingGroupRuleManager.getRulesJsonAsString();

        return Response.ok(this.routingGroupRuleManager.getRulesJsonAsString()).build();
    }



   /* @Path("/add")
    @POST
    public Response addRoutingRule(RoutingGroupRuleManager.RuleDetail ruleDetail) {
        routingGroupRuleManager.createNewRule(ruleDetail);
        return Response.ok(ruleDetail).build();
    }

    @Path("/update")
    @POST
    public Response updateRoutingRule(RoutingGroupRuleManager.RuleDetail ruleDetail) {
        routingGroupRuleManager.updateRuleDetail(ruleDetail);
        return Response.ok(ruleDetail).build();
    }
*/
    @Path("/delete")
    @POST
    public Response deleteRoutingRule(String name) {
        routingGroupRuleManager.deleteRoutingRule(name);
        return Response.ok().build();
    }

    @Path("/show")
    @POST
    public Response show(String name) {
        log.info("INPUT RECEIVED  ", name);
        Yaml y = new Yaml();
        Yaml y1 = y.load(name);
        return Response.ok(name).build();
    }


}
