package com.lyft.data.gateway.ha.resource;

import com.google.inject.Inject;
import com.lyft.data.gateway.ha.config.ProxyBackendConfiguration;
import com.lyft.data.gateway.ha.router.GatewayBackendManager;
import com.lyft.data.gateway.ha.router.QueryHistoryManager;
import com.lyft.data.gateway.ha.router.RoutingGroupRuleManager;
import io.dropwizard.views.View;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.Data;

@Path("/")
public class GatewayViewResource {
  private static final long START_TIME = System.currentTimeMillis();
  @Inject private GatewayBackendManager gatewayBackendManager;
  @Inject private QueryHistoryManager queryHistoryManager;

  @Inject private RoutingGroupRuleManager routingGroupRuleManager;



  @GET
  @Produces(MediaType.TEXT_HTML)
  public GatewayView getQueryDetailsView() {
    GatewayView queryHistoryView = new GatewayView("/template/query-history-view.ftl");
    // Get All active backends
    queryHistoryView.setBackendConfigurations(
        gatewayBackendManager.getAllBackends().stream()
            .filter(ProxyBackendConfiguration::isActive)
            .collect(Collectors.toList()));

    queryHistoryView.setQueryHistory(queryHistoryManager.fetchQueryHistory());
    queryHistoryView.setQueryDistribution(getQueryHistoryDistribution());
    return queryHistoryView;
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("viewgateway")
  public GatewayView getGatewayView() {
    GatewayView gatewayView = new GatewayView("/template/gateway-view.ftl");
    // Get All active backends
    gatewayView.setBackendConfigurations(
        gatewayBackendManager.getAllBackends().stream()
            .filter(ProxyBackendConfiguration::isActive)
            .collect(Collectors.toList()));

    gatewayView.setQueryHistory(queryHistoryManager.fetchQueryHistory());
    gatewayView.setQueryDistribution(getQueryHistoryDistribution());
    return gatewayView;
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("viewrules")
  public GatewayView getRulesView() {
    GatewayView gatewayView = new GatewayView("/template/rules-view.ftl");
    // Get All active backends
    gatewayView.setBackendConfigurations(
            gatewayBackendManager.getAllBackends().stream()
                    .filter(ProxyBackendConfiguration::isActive)
                    .collect(Collectors.toList()));

    gatewayView.setQueryHistory(queryHistoryManager.fetchQueryHistory());
    gatewayView.setQueryDistribution(getQueryHistoryDistribution());

    gatewayView.setRulesConfiguration(routingGroupRuleManager.getRulesJsonAsString());
    return gatewayView;
  }

  @GET
  @Path("api/getRules")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Map<String, Object>> getAllRules() {
    return routingGroupRuleManager.getAllRoutingRules();
  }

  @GET
  @Path("api/queryHistory")
  @Produces(MediaType.APPLICATION_JSON)
  public List<QueryHistoryManager.QueryDetail> getQueryHistory() {
    return queryHistoryManager.fetchQueryHistory();
  }

  @GET
  @Path("api/activeBackends")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ProxyBackendConfiguration> getActiveBackends() {
    return gatewayBackendManager.getAllBackends().stream()
        .filter(ProxyBackendConfiguration::isActive)
        .collect(Collectors.toList());
  }

  @GET
  @Path("api/queryHistoryDistribution")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Integer> getQueryHistoryDistribution() {
    Map<String, String> urlToNameMap = new HashMap<>();
    gatewayBackendManager
        .getAllBackends()
        .forEach(
            backend -> {
              urlToNameMap.put(backend.getProxyTo(), backend.getName());
            });

    Map<String, Integer> clusterToQueryCount = new HashMap<>();
    queryHistoryManager
        .fetchQueryHistory()
        .forEach(
            q -> {
              String backend = urlToNameMap.get(q.getBackendUrl());
              if (backend == null) {
                backend = q.getBackendUrl();
              }
              if (!clusterToQueryCount.containsKey(backend)) {
                clusterToQueryCount.put(backend, 0);
              }
              clusterToQueryCount.put(backend, clusterToQueryCount.get(backend) + 1);
            });
    return clusterToQueryCount;
  }

  @Data
  public static class GatewayView extends View {
    private final long gatewayStartTime = START_TIME;
    private List<ProxyBackendConfiguration> backendConfigurations;
    private List<QueryHistoryManager.QueryDetail> queryHistory;
    private Map<String, Integer> queryDistribution;
    private String rulesConfiguration;

    protected GatewayView(String templateName) {
      super(templateName, Charset.defaultCharset());
    }
  }
}
