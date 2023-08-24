package com.lyft.data.gateway.ha.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyft.data.gateway.ha.persistence.JdbcConnectionManager;
import com.lyft.data.gateway.ha.persistence.dao.GatewayBackend;
import com.lyft.data.gateway.ha.persistence.dao.RoutingRule;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
public class HaRoutingGroupRuleManager implements RoutingGroupRuleManager {
    private JdbcConnectionManager connectionManager;
    List<Map<String, Object>> result = new ArrayList<>();
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public HaRoutingGroupRuleManager(JdbcConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void createNewRule(Map<String, Object> ruleDetail) {
        try {
            connectionManager.open();
            Boolean success = RoutingRule.create(new RoutingRule(), ruleDetail, OBJECT_MAPPER);

        } finally {
            connectionManager.close();
        }
    }

    @Override
    public void updateRuleDetail(Map<String, Object> ruleDetail) {

        try {
            connectionManager.open();
            RoutingRule model = RoutingRule.findFirst("name = ?", ruleDetail.get("name").toString());
            if (model == null) {
                RoutingRule.create(new RoutingRule(), ruleDetail, OBJECT_MAPPER);
            } else {
                RoutingRule.update(model, ruleDetail, OBJECT_MAPPER);
            }
        } finally {
            connectionManager.close();
        }


    }


    @Override
    public List<Map<String, Object>> getAllRoutingRules() {
        try {
            connectionManager.open();
            List<RoutingRule> rules = RoutingRule.findAll().orderBy("id");
            return RoutingRule.upCast(rules, OBJECT_MAPPER);

        } finally {
            connectionManager.close();
        }

    }


    @Override
    public void deleteRoutingRule(String name) {
        try {
            connectionManager.open();
            RoutingRule.delete("name = ?", name);
        } finally {
            connectionManager.close();
        }

    }

    @Override

    public String getRulesJsonAsString() {
        String col = "active";
        List<Map<String, Object>> allActiveRules = getAllRoutingRules().stream()
                .collect(Collectors.toList());
        try {
            return OBJECT_MAPPER.writeValueAsString(allActiveRules);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public String getActiveRulesJsonAsString() {
        String col = "active";
        List<Map<String, Object>> allActiveRules = getAllRoutingRules().stream()
                .filter(m -> new Boolean(m.get(col).toString()))
                .map(m -> {
                    m.remove(col);
                    return m;
                })
                .collect(Collectors.toList());

        try {
            String json = OBJECT_MAPPER.writeValueAsString(allActiveRules);
            System.out.println(OBJECT_MAPPER.writeValueAsString(allActiveRules));
            return json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deactivateRule(String ruleName) {
        try {
            connectionManager.open();
            RoutingRule.findFirst("name = ?", ruleName).set("active", false).saveIt();
        } finally {
            connectionManager.close();
        }
    }

    @Override
    public void activateRule(String ruleName) {
        try {
            connectionManager.open();
            RoutingRule.findFirst("name = ?", ruleName).set("active", true).saveIt();
        } finally {
            connectionManager.close();
        }
    }
}
