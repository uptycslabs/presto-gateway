package com.lyft.data.gateway.ha.router;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyft.data.gateway.ha.HaGatewayTestUtils;
import com.lyft.data.gateway.ha.config.DataStoreConfiguration;
import com.lyft.data.gateway.ha.config.ProxyBackendConfiguration;
import com.lyft.data.gateway.ha.persistence.JdbcConnectionManager;
import java.io.File;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
public class TestHaRoutingGroupRuleManager {
    private HaRoutingGroupRuleManager haRoutingGroupRuleManager;
    ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File tempH2DbDir = new File(baseDir, "h2db-" + System.currentTimeMillis());
        tempH2DbDir.deleteOnExit();
        String jdbcUrl = "jdbc:h2:" + tempH2DbDir.getAbsolutePath();
        HaGatewayTestUtils.seedRequiredData(
                new HaGatewayTestUtils.TestConfig("", tempH2DbDir.getAbsolutePath()));
        DataStoreConfiguration db = new DataStoreConfiguration(jdbcUrl, "sa", "sa", "org.h2.Driver", 4);
        JdbcConnectionManager connectionManager = new JdbcConnectionManager(db);
        haRoutingGroupRuleManager = new HaRoutingGroupRuleManager(connectionManager);
    }

    @Test
    public void testAddRule() throws JsonProcessingException {
        String configJson ="{\n" +
                "  \"name\": \"airflow\",\n" +
                "  \"active\": true,\n" +
                "  \"description\": \"if query from airflow, route to etl group\",\n" +
                "  \"condition\": \"request.getHeader(\\\"X-Trino-Source\\\") == \\\"airflow\\\" \",\n" +
                "  \"actions\": [\n" +
                "    \"result.put(\\\"routingGroup\\\", \\\"etl\\\")\"\n" +
                "  ]\n" +
                "}";

        Map<String, Object> ruleDetail = OBJECT_MAPPER.readValue(configJson, Map.class);
        haRoutingGroupRuleManager.createNewRule(ruleDetail);
        List<Map<String, Object>> rules =  haRoutingGroupRuleManager.getAllRoutingRules();
        Assert.assertEquals(rules.size(), 1);
        Assert.assertEquals(rules.get(0).get("name").toString(), "airflow");

    }

    @Test(dependsOnMethods = {"testAddRule"})
    public void testUpdateRule() throws JsonProcessingException {

        String rule1="{\n" +
                "  \"name\": \"airflow\",\n" +
                "  \"active\": false,\n" +
                "  \"description\": \"if query from airflow, route to etl group\",\n" +
                "  \"condition\": \"request.getHeader(\\\"X-Trino-Source\\\") == \\\"airflow\\\" \",\n" +
                "  \"actions\": [\n" +
                "    \"result.put(\\\"routingGroup\\\", \\\"etl\\\")\"\n" +
                "  ]\n" +
                "}";

        Map<String, Object> ruleDetail = OBJECT_MAPPER.readValue(rule1, Map.class);
        haRoutingGroupRuleManager.updateRuleDetail(ruleDetail);
        List<Map<String, Object>> rules =  haRoutingGroupRuleManager.getAllRoutingRules();
        Assert.assertEquals(rules.size(), 1);
        Assert.assertEquals(rules.get(0).get("name").toString(), "airflow");
        Assert.assertEquals(rules.get(0).get("active").toString(), "false");

        String rule2="{\n" +
                "  \"name\": \"airflow1\",\n" +
                "  \"active\": true,\n" +
                "  \"description\": \"if query from airflow, route to etl group\",\n" +
                "  \"condition\": \"request.getHeader(\\\"X-Trino-Source\\\") == \\\"airflow\\\" \",\n" +
                "  \"actions\": [\n" +
                "    \"result.put(\\\"routingGroup\\\", \\\"etl\\\")\"\n" +
                "  ]\n" +
                "}";

        Map<String, Object> ruleDetail2= OBJECT_MAPPER.readValue(rule2, Map.class);
        haRoutingGroupRuleManager.updateRuleDetail(ruleDetail2);
        List<Map<String, Object>> updatedRules =  haRoutingGroupRuleManager.getAllRoutingRules();
        Assert.assertEquals(updatedRules.size(), 2);
        Assert.assertEquals(updatedRules.get(0).get("name").toString(), "airflow");
        Assert.assertEquals(updatedRules.get(1).get("name").toString(), "airflow1");


    }
    @Test(dependsOnMethods = {"testUpdateRule"})
    public void getActiveRules(){
        String updatedRules =  haRoutingGroupRuleManager.getActiveRulesJsonAsString();
        String expectedJson ="[{\"name\":\"airflow1\",\"description\":\"if query from airflow, route to etl group\",\"condition\":\"request.getHeader(\\\"X-Trino-Source\\\") == \\\"airflow\\\" \",\"actions\":[\"result.put(\\\"routingGroup\\\", \\\"etl\\\")\"]}]";

        Assert.assertEquals(updatedRules, expectedJson);


    }

    @Test(dependsOnMethods = {"testUpdateRule"})
    public void getAllRulesAsString(){
        List<Map<String, Object>> updatedRules =  haRoutingGroupRuleManager.getAllRoutingRules();
        Assert.assertEquals(updatedRules.size(), 2);
        String actualJson =  haRoutingGroupRuleManager.getRulesJsonAsString();
        String expectedJson ="[{\"name\":\"airflow\",\"description\":\"if query from airflow, route to etl group\",\"condition\":\"request.getHeader(\\\"X-Trino-Source\\\") == \\\"airflow\\\" \",\"actions\":[\"result.put(\\\"routingGroup\\\", \\\"etl\\\")\"],\"active\":false},{\"name\":\"airflow1\",\"description\":\"if query from airflow, route to etl group\",\"condition\":\"request.getHeader(\\\"X-Trino-Source\\\") == \\\"airflow\\\" \",\"actions\":[\"result.put(\\\"routingGroup\\\", \\\"etl\\\")\"],\"active\":true}]";
        System.out.println(actualJson);
        Assert.assertEquals(actualJson, expectedJson);


    }
    @Test(dependsOnMethods = {"testUpdateRule"})
    public void testDeleteRule() {
        List<Map<String, Object>> updatedRules =  haRoutingGroupRuleManager.getAllRoutingRules();
        Assert.assertEquals(updatedRules.size(), 2);
        haRoutingGroupRuleManager.deleteRoutingRule("airflow");
        List<Map<String, Object>> rules =  haRoutingGroupRuleManager.getAllRoutingRules();
        Assert.assertEquals(rules.size(), 1);
        Assert.assertEquals(rules.get(0).get("name").toString(), "airflow1");


    }


    @AfterClass(alwaysRun = true)
    public void cleanUp() {}
}
