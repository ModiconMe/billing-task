package io.modicon.taskapp.rest.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

@Slf4j
public class ServerPortCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        String port = System.getProperty("server.port");
        log.info("SERVER PORT: " + port);
        if (port != null) {
            factory.setPort(Integer.parseInt(port));
        }
    }

}
