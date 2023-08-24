package com.lyft.data.gateway.ha.router;

import java.util.List;
import java.util.Map;

public interface RoutingGroupRuleManager {

    void createNewRule(Map<String, Object> ruleDetail);

    void  updateRuleDetail(Map<String, Object> ruleDetail);

    public List<Map<String, Object>> getAllRoutingRules();

    void deleteRoutingRule(String name);

     String getRulesJsonAsString();

    String getActiveRulesJsonAsString();

    void deactivateRule(String ruleName);

    void activateRule(String ruleName);


}
