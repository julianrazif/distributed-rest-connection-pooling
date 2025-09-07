package com.julianrazif.module.drcp;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class DrcpAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
    .withConfiguration(AutoConfigurations.of(DrcpAutoConfiguration.class));

  @Test
  void shouldCreateBeansByDefault() {
    contextRunner.run(context -> {
      assertThat(context).hasSingleBean(DrcpProperties.class);
      assertThat(context).hasSingleBean(PoolingAsyncClientConnectionManager.class);
      assertThat(context).hasSingleBean(CloseableHttpAsyncClient.class);
    });
  }

  @Test
  void shouldNotCreateBeansWhenDisabled() {
    contextRunner.withPropertyValues("drcp.httpclient.enabled=false").run(context -> {
      assertThat(context).doesNotHaveBean(PoolingAsyncClientConnectionManager.class);
      assertThat(context).doesNotHaveBean(CloseableHttpAsyncClient.class);
    });
  }

}
