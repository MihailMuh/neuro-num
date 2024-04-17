package ru.lvmlabs.neuronum.familia.selenium.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.lvmlabs.neuronum.baseconfigs.utils.AwaitableExecutorService;
import ru.lvmlabs.neuronum.familia.calls.FamiliaCallsService;
import ru.lvmlabs.neuronum.familia.calls.SeleniumCallParsing;
import ru.lvmlabs.neuronum.familia.selenium.pages.CallsPage;
import ru.lvmlabs.neuronum.familia.selenium.pages.LoginPage;
import ru.lvmlabs.neuronum.sharedmodules.selenium.domain.Browser;
import ru.lvmlabs.neuronum.sharedmodules.selenium.service.PageService;
import ru.lvmlabs.neuronum.sharedmodules.selenium.utils.MoonClient;
import ru.lvmlabs.neuronum.sharedmodules.transcribe.TranscribationService;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class UisPageService implements PageService {
    private static final AwaitableExecutorService executorService = new AwaitableExecutorService();

    private final TranscribationService transcribationService;

    private final FamiliaCallsService familiaCallsService;

    @Value("${neuronum.familia.browser.url.login}")
    private String loginUrl;

    @Value("${neuronum.familia.browser.url.calls}")
    private String callsUrl;

    @Override
    public String envPropertyWithCrmId() {
        return "neuronum.familia.crm.id";
    }

    @Override
    public void loginInAccount(Browser browser, String email, String password) {
        LoginPage loginPage = new LoginPage(browser, loginUrl);
        loginPage.inputEmail(email);
        loginPage.inputPassword(password);
        loginPage.login();

        log.debug("Entered to client account");
    }

    @Override
    public void downloadCalls(Browser browser, Date oldestDate, Date newestDate, MoonClient moonClient) {
        CallsPage callsPage = new CallsPage(browser, callsUrl, moonClient);
        callsPage.setDateRange(oldestDate, newestDate);

        callsPage.forEachCall(this::analyseAndSave);

        executorService.await();
        log.debug("Calls downloaded");
    }

    @Override
    public void close() throws Exception {
        executorService.close();
    }

    private void analyseAndSave(SeleniumCallParsing seleniumCall, CallsPage.Callable audioFile) {
        // not comparing by date, because date's obtained from audioFile.get()
        if (familiaCallsService.existsByTimeAndPhoneNumber(seleniumCall.getTime(), seleniumCall.getPhoneNumber())) {
            log.warn("Call already exists!");
            return;
        }

        String downloadUrl = audioFile.get();
        if (downloadUrl.isBlank()) return;

        String textDialog = transcribationService.transcribe(downloadUrl);
        if (textDialog.isEmpty()) return;

        executorService.execute(() -> {
            seleniumCall.setAudioDownloadUrl(downloadUrl);
            seleniumCall.setText(textDialog);

            familiaCallsService.create(seleniumCall);
        });
    }
}
