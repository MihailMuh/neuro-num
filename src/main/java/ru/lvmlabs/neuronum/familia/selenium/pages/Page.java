package ru.lvmlabs.neuronum.familia.selenium.pages;

import ru.lvmlabs.neuronum.sharedmodules.selenium.domain.Browser;

public abstract class Page {
    protected final Browser browser;

    protected final String url;

    public Page(Browser browser, String url) {
        this.browser = browser;
        this.url = url;
    }
}
