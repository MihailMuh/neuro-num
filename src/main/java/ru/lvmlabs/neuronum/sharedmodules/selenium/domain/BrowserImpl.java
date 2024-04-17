package ru.lvmlabs.neuronum.sharedmodules.selenium.domain;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@ToString
@EqualsAndHashCode
public class BrowserImpl implements Browser {
    private final WebDriver driver;

    public BrowserImpl(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public void close() {
        log.trace("Disposing the browser...");

        try {
            driver.quit();
            log.trace("Success!");
        } catch (Exception exception) {
            log.error("Error has been occurred when disposing the browser");
            exception.printStackTrace();
        }
    }

    @Override
    public void get(String url) {
        driver.get(url);
    }

    @Override
    public WebDriver getDriver() {
        return driver;
    }

    @Override
    public Object javaScript(String script, Object... args) {
        log.trace("Executing of JavaScript...");
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    @Override
    public WebElement find(By by) {
        return driver.findElement(by);
    }

    @Override
    public Map<String, String> cookies() {
        Set<Cookie> cookies = driver.manage().getCookies();
        Map<String, String> cookiesMap = new HashMap<>(cookies.size());
        for (Cookie cookie : cookies) {
            cookiesMap.put(cookie.getName(), cookie.getValue());
        }

        return cookiesMap;
    }

    @Override
    public WebDriverWait Wait(int seconds) {
        log.trace("Explicit wait has been created for {} seconds", seconds);
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    @Override
    public String getSessionId() {
        return ((RemoteWebDriver) driver).getSessionId().toString();
    }
}
