package com.lyft.data.gateway.ha.module;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.lyft.data.baseapp.AppModule;
import com.lyft.data.gateway.ha.clustermonitor.HealthChecker;
import com.lyft.data.gateway.ha.clustermonitor.PrestoClusterStatsObserver;
import com.lyft.data.gateway.ha.clustermonitor.PrestoQueueLengthChecker;
import com.lyft.data.gateway.ha.config.HaGatewayConfiguration;
import com.lyft.data.gateway.ha.config.MonitorConfiguration;
import com.lyft.data.gateway.ha.config.NotifierConfiguration;
import com.lyft.data.gateway.ha.notifier.EmailNotifier;
import com.lyft.data.gateway.ha.persistence.JdbcConnectionManager;
import com.lyft.data.gateway.ha.router.*;
import io.dropwizard.setup.Environment;
import java.util.ArrayList;
import java.util.List;

public class ClusterStateListenerModule extends AppModule<HaGatewayConfiguration, Environment> {
  List<PrestoClusterStatsObserver> observers;
  MonitorConfiguration monitorConfig;

  private final PrestoQueueLengthRoutingTable routingManager;
  private final JdbcConnectionManager connectionManager;

  public ClusterStateListenerModule(HaGatewayConfiguration config, Environment env) {
    super(config, env);
    monitorConfig = config.getMonitor();
    connectionManager = new JdbcConnectionManager(config.getDataStore());

    routingManager =
             PrestoQueueLengthRoutingTable.getInstance( new HaGatewayManager(connectionManager), (HaQueryHistoryManager)  new HaQueryHistoryManager(connectionManager));
  }

  /**
   * Observers to cluster stats updates from
   * {@link com.lyft.data.gateway.ha.clustermonitor.ActiveClusterMonitor}.
   *
   * @return
   */
  @Provides
  @Singleton
  public List<PrestoClusterStatsObserver> getClusterStatsObservers() {
    observers = new ArrayList<>();
    NotifierConfiguration notifierConfiguration = getConfiguration().getNotifier();

    observers.add(new HealthChecker(new EmailNotifier(notifierConfiguration)));
    observers.add(new PrestoQueueLengthChecker(routingManager));
    return observers;
  }

  @Provides
  public MonitorConfiguration getMonitorConfiguration() {
    return monitorConfig;
  }
}
