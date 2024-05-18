package com.salesmanager.shop.application.config;

import com.salesmanager.shop.init.data.InitStoreData;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class react_nativePropertiesConfig {

  @Bean
  public List<String> templates() {
    return Arrays.asList("bootstrap", "generic", "exoticamobilia");
  }

  @Bean
  public InitStoreData initData() {
    return new InitStoreData();
  }

  @Bean(name = "react_native-properties")
  public PropertiesFactoryBean mapper() {
    PropertiesFactoryBean bean = new PropertiesFactoryBean();
    bean.setLocation(new ClassPathResource("react_native-properties.properties"));
    return bean;
  }
}
