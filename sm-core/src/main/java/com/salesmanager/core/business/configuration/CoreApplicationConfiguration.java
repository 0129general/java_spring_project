package com.salesmanager.core.business.configuration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@EnableAutoConfiguration
/**
 * added in react_native-core-config.xml
 * 
 * @author c.samson
 *
 */
@ComponentScan({"com.salesmanager.core.business"})
@ImportResource("classpath:/spring/react_native-core-context.xml")
//@Import({DroolsConfiguration.class,VaultConfiguration.class})
@Import({DroolsConfiguration.class})
public class CoreApplicationConfiguration {



}
