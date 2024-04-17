package ru.lvmlabs.neuronum.familia.selenium.pages;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import ru.lvmlabs.neuronum.baseconfigs.utils.DateUtils;
import ru.lvmlabs.neuronum.baseconfigs.utils.ThreadUtils;
import ru.lvmlabs.neuronum.familia.calls.SeleniumCallParsing;
import ru.lvmlabs.neuronum.sharedmodules.selenium.domain.Browser;
import ru.lvmlabs.neuronum.sharedmodules.selenium.utils.MoonClient;

import java.util.Date;
import java.util.List;

import static org.openqa.selenium.By.xpath;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElementsLocatedBy;

@Slf4j
public class CallsPage extends Page {
    private final MoonClient moonClient;

    public CallsPage(Browser browser, String url, MoonClient moonClient) {
        super(browser, url);
        this.moonClient = moonClient;
    }

    public void setDateRange(Date oldestDate, Date newestDate) {
        browser.get(formatUrl(oldestDate, newestDate));

        if (!waitForNextPage()) {
            log.error("Waiting for calls page is too long!");
            throw new RuntimeException("Waiting for calls page is too long!");
        }

        log.debug("CallsPage has been initialized");
    }

    public void forEachCall(CallConsumer callConsumer) {
        log.trace("Start message iterating");

        while (true) {
            for (List<WebElement> divCall : findDivCalls()) {
                String[] datetime = divCall.get(1).getText().strip().split(" ");

                SeleniumCallParsing call = new SeleniumCallParsing();
                call.setIncoming("Звонок ВАТС".equals(divCall.get(0).getAttribute("data-qtip").strip()));
                call.setTime(DateUtils.fromString(datetime[1], "hh:mm:ss"));
                call.setPhoneNumber(divCall.get(2).getText().strip());
                call.setVirtualNumber(divCall.get(4).getText().strip());

                callConsumer.accept(
                        call,

                        // audioBytes. Callable - because bytes need if call not exists in db
                        // click in js - because ElementClickInterceptedException: element click intercepted:
                        () -> {
                            WebElement downloadButton = divCall.get(3);
                            browser.javaScript("arguments[0].click()", downloadButton);

                            String url = moonClient.getUrlToFileWithNameContains(browser, downloadButton.getAttribute("id").strip());
                            if (!url.isBlank()) {
                                call.setDate(DateUtils.fromString(datetime[0] + "." + moonClient.extractFileYearFromUrl(url), "dd.MM.yyyy"));
                            }
                            return url;
                        }
                );
            }

            if (!goToNextPage()) {
                log.trace("Message iterating has been finished");
                return;
            }
        }
    }

    private List<List<WebElement>> findDivCalls() {
        @SuppressWarnings("unchecked")
        List<List<WebElement>> divCalls = (List<List<WebElement>>) browser.javaScript("""
                const callsMatrix = []
                const divCalls = document.getElementsByClassName("x-grid-row-checker")
                const downloadButtons = document.getElementsByClassName("btn download-btn")
                const buttonsWithUniqueAudioId = document.getElementsByClassName("btn play-stop-btn")

                for (let i = 0; i < divCalls.length; i++) {
                    const firstDiv = divCalls[i].parentElement.parentElement.parentElement.getElementsByTagName("td")
                    if (firstDiv[4].children[0].innerText.length !== 11) {
                        continue // если звонок по внутренней линии, там нечего анализировать
                    }
                    
                    downloadButtons[i].id = buttonsWithUniqueAudioId[i].getAttribute("data-call_session_id")
                    callsMatrix.push([
                            firstDiv[2].children[0].children[0],    // isIncomingElem
                            firstDiv[3].children[0],                // datetimeElem
                            firstDiv[4].children[0],                // phoneNumberElem
                            downloadButtons[i],                     // downloadButton
                            downloadButtons[i].parentElement.parentElement.parentElement.children[5].children[0] // virtualNumber
                    ])
                }
                                    
                return callsMatrix
                """);

        log.trace("Total {} div calls on the page", divCalls.size());

        return divCalls;
    }

    private String formatUrl(Date oldestDate, Date newestDate) {
        String currentLink = url.replace("2024-01-06", newestDate.toString()).replace("2024-01-03", oldestDate.toString());

        log.info("Current link: {}", currentLink);
        return currentLink;
    }

    private boolean goToNextPage() {
        try {
            browser.find(buttonNextSelector()).click();
        } catch (Exception exception) {
            return false;
        }
        return waitForNextPage();
    }

    private boolean waitForNextPage() {
        try {
            // while this button non-clickable, messages not displayed on page
            ThreadUtils.sleep(1_000);
            browser.Wait(60).until(visibilityOfAllElementsLocatedBy(buttonNextSelector()));
            ThreadUtils.sleep(2_000);
            return true;
        } catch (TimeoutException exception) {
            return false;
        }
    }

    private By buttonNextSelector() {
        return xpath("//span[text()='дальше']");
    }

    public interface CallConsumer {
        void accept(SeleniumCallParsing call, Callable audioFile);
    }

    public interface Callable {
        String get();
    }
}
