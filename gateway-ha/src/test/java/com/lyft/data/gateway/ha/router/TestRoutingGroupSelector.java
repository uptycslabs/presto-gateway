package com.lyft.data.gateway.ha.router;

import static com.lyft.data.gateway.ha.router.RoutingGroupSelector.ROUTING_GROUP_HEADER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.io.*;
import java.nio.file.Paths;
import javax.servlet.http.HttpServletRequest;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test
public class TestRoutingGroupSelector {
    public static final String TRINO_SOURCE_HEADER = "X-Trino-Source";
    public static final String TRINO_CLIENT_TAGS_HEADER = "X-Trino-Client-Tags";
    HaRoutingGroupRuleManager routingGroupRuleManager = mock(HaRoutingGroupRuleManager.class);
    public void testByRoutingGroupHeader() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        // If the header is present the routing group is the value of that header.
        when(mockRequest.getHeader(ROUTING_GROUP_HEADER)).thenReturn("batch_backend");
        Assert.assertEquals(
                RoutingGroupSelector.byRoutingGroupHeader().findRoutingGroup(mockRequest), "batch_backend");

        // If the header is not present just return null.
        when(mockRequest.getHeader(ROUTING_GROUP_HEADER)).thenReturn(null);
        Assert.assertNull(RoutingGroupSelector.byRoutingGroupHeader().findRoutingGroup(mockRequest));
    }

    @DataProvider(name = "routingRuleConfigString")
    public Object[][] provideData() {
        String rulesDir = "src/test/resources/rules_json/";

        return new Object[][] {
                { readFileAsString(rulesDir + "routing_rules_atomic.json") },
                { readFileAsString(rulesDir + "routing_rules_composite.json") },
                { readFileAsString (rulesDir + "routing_rules_priorities.json") },
                { readFileAsString (rulesDir + "routing_rules_if_statements.json") }
        };
    }

    @Test(dataProvider = "routingRuleConfigString")
    public void testByRoutingRulesEngine(String jsonConfig) {
        when(routingGroupRuleManager.getActiveRulesJsonAsString()).thenReturn(jsonConfig);
        RoutingGroupSelector routingGroupSelector =
                RoutingGroupSelector.byRoutingRulesEngine(routingGroupRuleManager);

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        when(mockRequest.getHeader(TRINO_SOURCE_HEADER)).thenReturn("airflow");
        Assert.assertEquals(
                routingGroupSelector.findRoutingGroup(mockRequest), "etl");
    }

    @Test(dataProvider = "routingRuleConfigString")
    public void testByRoutingRulesEngineSpecialLabel(String jsonConfig) {

        when(routingGroupRuleManager.getActiveRulesJsonAsString()).thenReturn(jsonConfig);
        RoutingGroupSelector routingGroupSelector =
                RoutingGroupSelector.byRoutingRulesEngine(routingGroupRuleManager);

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        when(mockRequest.getHeader(TRINO_SOURCE_HEADER)).thenReturn("airflow");
        when(mockRequest.getHeader(TRINO_CLIENT_TAGS_HEADER)).thenReturn(
                "email=test@example.com,label=special");
        Assert.assertEquals(
                routingGroupSelector.findRoutingGroup(mockRequest), "etl-special");
    }

    @Test(dataProvider = "routingRuleConfigString")
    public void testByRoutingRulesEngineNoMatch(String jsonConfig) {

        when(routingGroupRuleManager.getActiveRulesJsonAsString()).thenReturn(jsonConfig);
        RoutingGroupSelector routingGroupSelector =
                RoutingGroupSelector.byRoutingRulesEngine(routingGroupRuleManager);

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        // even though special label is present, query is not from airflow.
        // should return no match
        when(mockRequest.getHeader(TRINO_CLIENT_TAGS_HEADER)).thenReturn(
                "email=test@example.com,label=special");
        Assert.assertEquals(
                routingGroupSelector.findRoutingGroup(mockRequest), null);
    }

    public void testByRoutingRulesEngineFileChange() throws Exception {
        File file = File.createTempFile("routing_rules", ".json");


        String configJson ="[{\n" +
                "  \"name\": \"airflow\",\n" +
                "  \"description\": \"if query from airflow, route to etl group\",\n" +
                "  \"condition\": \"request.getHeader(\\\"X-Trino-Source\\\") == \\\"airflow\\\" \",\n" +
                "  \"actions\": [\n" +
                "    \"result.put(\\\"routingGroup\\\", \\\"etl\\\")\"\n" +
                "  ]\n" +
                "}]";

        when(routingGroupRuleManager.getActiveRulesJsonAsString()).thenReturn(configJson);
        RoutingGroupSelector routingGroupSelector =
                RoutingGroupSelector.byRoutingRulesEngine(routingGroupRuleManager);


        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        when(mockRequest.getHeader(TRINO_SOURCE_HEADER)).thenReturn("airflow");
        Assert.assertEquals(
                routingGroupSelector.findRoutingGroup(mockRequest), "etl");

        configJson = "[{\n" +
                "  \"name\": \"airflow\",\n" +
                "  \"description\": \"if query from airflow, route to etl group\",\n" +
                "  \"condition\": \"request.getHeader(\\\"X-Trino-Source\\\") == \\\"airflow\\\" \",\n" +
                "  \"actions\": [\n" +
                "    \"result.put(\\\"routingGroup\\\", \\\"etl2\\\")\"\n" +
                "  ]\n" +
                "}]";

        when(routingGroupRuleManager.getActiveRulesJsonAsString()).thenReturn(configJson);


        when(mockRequest.getHeader(TRINO_SOURCE_HEADER)).thenReturn("airflow");
        Assert.assertEquals(
                routingGroupSelector.findRoutingGroup(mockRequest), "etl2");
        file.deleteOnExit();
    }


    private String readFileAsString(String configFile){
        Path path = Paths.get(configFile);

        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException ex) {
            // Handle exception
        }
        String jsonData = new String(bytes, StandardCharsets.UTF_8);
        return  jsonData;
    }
   /* @Test
    public void testRulesFromDB(){
        String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/prestogateway?enabledTLSProtocols=TLSv1.2";
        DataStoreConfiguration db = new DataStoreConfiguration(jdbcUrl, "root", "root123", "com.mysql.cj.jdbc.Driver");
        JdbcConnectionManager connectionManager = new JdbcConnectionManager(db);
        HaRoutingGroupRuleManager ruleManager = new HaRoutingGroupRuleManager(connectionManager);
        String rulesString = ruleManager.getRulesJsonString();
        System.out.println(ruleManager.getRulesJsonString());
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader(TRINO_SOURCE_HEADER)).thenReturn("qq");
        RulesEngine rulesEngine = new DefaultRulesEngine();
        MVELRuleFactory ruleFactory =  new MVELRuleFactory(new JsonRuleDefinitionReader()) ;

        try {
            Rules rules = ruleFactory.createRules(
                   new StringReader(rulesString));
            Facts facts = new Facts();
            HashMap<String, String> result = new HashMap<String, String>();
            facts.put("request", mockRequest);
            facts.put("result", result);
            rulesEngine.fire(rules, facts);
             System.out.println("***********"  + result.get("routingGroup"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/
}
