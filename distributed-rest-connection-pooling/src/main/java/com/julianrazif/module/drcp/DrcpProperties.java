package com.julianrazif.module.drcp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "drcp.httpclient")
public class DrcpProperties {

  /**
   * Max total connections across all routes.
   */
  private int maxConTotal = 200;

  /**
   * Max connections per route.
   */
  private int maxConPerRoute = 100;

  /**
   * Whether to enable autoconfigured Apache HttpClient.
   */
  private boolean enabled = true;

  public int getMaxConTotal() {
    return maxConTotal;
  }

  public void setMaxConTotal(int maxConTotal) {
    this.maxConTotal = maxConTotal;
  }

  public int getMaxConPerRoute() {
    return maxConPerRoute;
  }

  public void setMaxConPerRoute(int maxConPerRoute) {
    this.maxConPerRoute = maxConPerRoute;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

}
