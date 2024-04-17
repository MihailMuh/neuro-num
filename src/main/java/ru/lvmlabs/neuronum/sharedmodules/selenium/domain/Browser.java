package ru.lvmlabs.neuronum.sharedmodules.selenium.domain;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Map;

import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;

public interface Browser extends AutoCloseable {
    void get(String url);

    WebDriver getDriver();

    Object javaScript(String script, Object... args);

    WebElement find(By by);

    Map<String, String> cookies();

    default WebDriverWait Wait() {
        return Wait(30);
    }

    WebDriverWait Wait(int seconds);

    default WebElement waitWhileNotClickable(By by) {
        return Wait().until(elementToBeClickable(by));
    }

    String getSessionId();

    @Override
    void close();
}
