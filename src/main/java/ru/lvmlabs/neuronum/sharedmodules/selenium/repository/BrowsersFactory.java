package ru.lvmlabs.neuronum.sharedmodules.selenium.repository;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Repository;
import ru.lvmlabs.neuronum.sharedmodules.selenium.domain.Browser;

import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Repository
public class BrowsersFactory {
    private static final ConcurrentLinkedQueue<Browser> browsersInUse = new ConcurrentLinkedQueue<>();

    public Browser get() {
        Browser browser = generateBrowser();
        browsersInUse.add(browser);
        return browser;
    }

    @PreDestroy
    public void onDestroy() {
        browsersInUse.forEach(Browser::close);
    }

    // it runs a new browser
    @Lookup
    public Browser generateBrowser() {
        return null;
    }
}
