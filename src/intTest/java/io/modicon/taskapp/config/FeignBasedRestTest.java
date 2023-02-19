package io.modicon.taskapp.config;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.TestSocketUtils;

@EnableFeignClients(basePackages = "io.modicon.taskapp.client")
@Import(ServerPortCustomizer.class)
@ExtendWith(FeignBasedRestTest.Before.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class FeignBasedRestTest {

    public static class Before implements BeforeAllCallback {

        @Override
        public void beforeAll(ExtensionContext context) {
            if (System.getProperty("server.port") == null) {
                int port = TestSocketUtils.findAvailableTcpPort();
                System.setProperty("server.port", String.valueOf(port));
            }
        }

    }

}
