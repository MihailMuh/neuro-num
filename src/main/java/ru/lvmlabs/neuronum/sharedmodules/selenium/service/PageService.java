package ru.lvmlabs.neuronum.sharedmodules.selenium.service;

import ru.lvmlabs.neuronum.sharedmodules.selenium.domain.Browser;
import ru.lvmlabs.neuronum.sharedmodules.selenium.utils.MoonClient;

import java.util.Date;

public interface PageService extends AutoCloseable {
    String envPropertyWithCrmId();

    void loginInAccount(Browser browser, String login, String password);

    void downloadCalls(Browser browser, Date oldestDate, Date newestDate, MoonClient moonClient);
}
