package com.julianrazif.module.drcp;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.net.ProxySelector;
import java.util.concurrent.TimeUnit;

@AutoConfiguration
@EnableConfigurationProperties(DrcpProperties.class)
@ConditionalOnClass({CloseableHttpAsyncClient.class, AsyncClientConnectionManager.class})
@ConditionalOnProperty(prefix = "drcp.httpclient", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DrcpAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public PoolingAsyncClientConnectionManager drcpConnectionManager(DrcpProperties properties) {
    PoolingAsyncClientConnectionManagerBuilder builder = PoolingAsyncClientConnectionManagerBuilder.create()
      .setMaxConnTotal(properties.getMaxConTotal())
      .setMaxConnPerRoute(properties.getMaxConPerRoute())
      .setDefaultConnectionConfig(
        ConnectionConfig.custom()
          .setTimeToLive(-1, TimeUnit.MILLISECONDS).build()
      )
      .setDefaultTlsConfig(
        TlsConfig.custom()
          .setVersionPolicy(HttpVersionPolicy.NEGOTIATE).build()
      )
      .setTlsStrategy(
        ClientTlsStrategyBuilder.create()
          .setSslContext(SSLContexts.createSystemDefault()).buildAsync()
      );

    return builder.build();
  }

  @Bean
  @ConditionalOnMissingBean
  public CloseableHttpAsyncClient drcpHttpClient(AsyncClientConnectionManager connectionManager) {
    return HttpAsyncClientBuilder.create()
      .setH2Config(H2Config.custom().setMaxConcurrentStreams(100).build())
      .setHttp1Config(Http1Config.DEFAULT)
      .setConnectionManager(connectionManager)
      .setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
      .disableRedirectHandling()
      .disableAutomaticRetries()
      .build();
  }

}
