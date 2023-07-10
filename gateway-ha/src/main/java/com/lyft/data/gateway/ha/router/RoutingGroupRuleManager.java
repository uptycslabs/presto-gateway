package com.lyft.data.gateway.ha.router;

import lombok.*;

import java.util.List;
import java.util.Map;

public interface RoutingGroupRuleManager {

    void createNewRule(Map<String, Object> ruleDetail);

    void  updateRuleDetail(Map<String, Object> ruleDetail);

    public List<Map<String, Object>> getAllRoutingRules();




    void deleteRoutingRule(String name);

     String getRulesJsonAsString();

    String getActiveRulesJsonAsString();




    @Data
    @ToString
    @RequiredArgsConstructor
    class RuleDetail {
        @NonNull private String name;
        @NonNull private String rule;
        private Boolean active;
        public RuleDetail() {}
        public RuleDetail(String name, String rule, Boolean active){
            this.name = name;
            this.rule = rule;
            this.active = active;
        }
    }
}
