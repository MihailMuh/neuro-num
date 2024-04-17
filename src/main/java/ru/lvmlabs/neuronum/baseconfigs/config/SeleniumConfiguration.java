package ru.lvmlabs.neuronum.baseconfigs.config;

import ru.lvmlabs.neuronum.sharedmodules.selenium.domain.Browser;
import ru.lvmlabs.neuronum.sharedmodules.selenium.domain.BrowserImpl;
import lombok.SneakyThrows;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.net.URI;
import java.util.HashMap;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Configuration
class SeleniumConfiguration {
    @Value("${neuronum.browser.headless}")
    private boolean headless;

    @Value("${neuronum.moon.url}")
    private String driverUrl;

    @Bean(destroyMethod = "")
    @Scope(SCOPE_PROTOTYPE)
    public Browser browser() {
        return new BrowserImpl(createDriver());
    }

    @SneakyThrows
    private WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-extensions",
                "--disable-plugins",
                "--disable-popup-blocking",
                "--disable-infobars",
                "--window-size=1920,1280"
        );
        if (headless) {
            options.addArguments("--headless");
        }

        options.setCapability("moon:options", new HashMap<String, Object>() {{
            put("sessionTimeout", "50m");
        }});

        return new RemoteWebDriver(new URI(driverUrl).toURL(), options, false);
    }
}
