package ru.lvmlabs.neuronum;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication(exclude = {
        ErrorMvcAutoConfiguration.class, // disables /error endpoint redirections
})
@Slf4j
@RequiredArgsConstructor
public class NeuronumApplication {
    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(NeuronumApplication.class);
        application.run();
    }

    @PostConstruct
    public void init() {
        log.debug("Detected active profiles: '{}'", activeProfiles);
    }
}
