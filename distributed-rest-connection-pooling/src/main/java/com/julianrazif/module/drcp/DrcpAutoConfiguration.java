package com.julianrazif.module.drcp;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
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
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.net.ProxySelector;
import java.util.concurrent.ThreadFactory;
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
      .setDefaultRequestConfig(
        RequestConfig.custom()
          .setCookieSpec(StandardCookieSpec.STRICT)
          .build()
      )
      .setDefaultCookieStore(new BasicCookieStore())
      .setThreadFactory(workerThreadFactory())
      .setIOReactorConfig(ioReactorConfig(Timeout.ofSeconds(35)))
      .setConnectionManager(connectionManager)
      .setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
      .disableRedirectHandling()
      .disableAutomaticRetries()
      .build();
  }

  private ThreadFactory workerThreadFactory() {
    CustomizableThreadFactory workerThreadFactory = new CustomizableThreadFactory("DRCP-worker-");
    workerThreadFactory.setThreadPriority(Thread.MAX_PRIORITY);
    workerThreadFactory.setDaemon(true);
    return workerThreadFactory;
  }

  private IOReactorConfig ioReactorConfig(Timeout timeout) {
    return IOReactorConfig.custom()
      .setSoTimeout(timeout)
      .setIoThreadCount(4)
      .build();
  }

}
