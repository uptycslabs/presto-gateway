package com.lyft.data.gateway.ha.persistence.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Cached;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@IdName("name")
@Table("routing_rule")
@Cached
public class RoutingRule extends Model {

    private static final String name = "name";
    private static final String rule = "rule";

    private static final String active = "active";


    public static List<Map<String, Object>> upCast(List<RoutingRule> routingRules, ObjectMapper objectMapper){
        return routingRules.stream().map(model -> {
            Map<String, Object> ruleMap = new HashMap<>();
            try {
                ruleMap = objectMapper.readValue(model.getString(rule), Map.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            ruleMap.put(name, model.getString(name));
            ruleMap.put(active, model.getBoolean(active));
            return ruleMap;

        }).collect(Collectors.toList());
    }


    public static String getRuleJsonString(List<RoutingRule> routingRules, ObjectMapper objectMapper){
        List<Map<String, Object>> rules =  routingRules.stream().map(model -> {
            Map<String, Object> ruleMap = new HashMap<>();
            try {
                ruleMap = objectMapper.readValue(model.getString(rule), Map.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return ruleMap;

        }).collect(Collectors.toList());

        try {
           return objectMapper.writeValueAsString(rules);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public static void update(RoutingRule model, Map<String, Object> ruleDetail, ObjectMapper objectMapper) {
        model
                .set(name, ruleDetail.get(name))
                .set(active, ruleDetail.get(active))
                .set(rule, getJsonRule(ruleDetail, objectMapper))
                .saveIt();
    }

    public static Boolean create(RoutingRule model, Map<String, Object> ruleDetail, ObjectMapper objectMapper ){

        return model
                .create( name,
                        ruleDetail.get(name).toString(),
                        active,
                        Boolean.getBoolean(ruleDetail.get(active).toString()),
                        rule,
                        getJsonRule(ruleDetail, objectMapper)).insert();

    }


    public static String getJsonRule(Map<String, Object> ruleDetail, ObjectMapper objectMapper) {
        ruleDetail.remove(active);
        try {
            return objectMapper.writeValueAsString(ruleDetail);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }



}
