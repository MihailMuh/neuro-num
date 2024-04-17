package ru.lvmlabs.neuronum.sharedmodules.selenium.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import ru.lvmlabs.neuronum.sharedmodules.selenium.domain.Browser;
import ru.lvmlabs.neuronum.sharedmodules.selenium.repository.BrowsersFactory;
import ru.lvmlabs.neuronum.sharedmodules.selenium.utils.MoonClient;
import ru.lvmlabs.neuronum.users.dto.CrmAccountDto;
import ru.lvmlabs.neuronum.users.service.CrmAccountsService;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Service
public class CrmAnalysisScheduler {
    private static final ConcurrentHashMap<UUID, ScheduledFuture<?>> scheduledAnalyses = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduledExecutor;

    private final Collection<PageService> pageServices;

    private final CrmAccountsService crmAccountsService;

    private final BrowsersFactory browsersFactory;
    private final MoonClient moonClient;

    public CrmAnalysisScheduler(CrmAccountsService crmAccountsService, BrowsersFactory browsersFactory, MoonClient moonClient,
                                Environment env, ApplicationContext context) {

        this.crmAccountsService = crmAccountsService;
        this.browsersFactory = browsersFactory;
        this.moonClient = moonClient;

        this.pageServices = context.getBeansOfType(PageService.class).values();
        scheduledExecutor = Executors.newScheduledThreadPool(pageServices.size());

        for (PageService pageService : pageServices) {
            try {
                scheduleAnalysis(UUID.fromString(env.getProperty(pageService.envPropertyWithCrmId())), pageService);
            } catch (Exception exception) {
                log.error("Can't schedule account for env: '{}'", pageService.envPropertyWithCrmId());
                exception.printStackTrace();
            }
        }
    }

    public void scheduleAnalysis(UUID accountId, PageService pageService) {
        if (scheduledAnalyses.containsKey(accountId)) return;

        CrmAccountDto telephonyAccount = crmAccountsService.getIfSuitableStatus(accountId);
        if (telephonyAccount == null) return;

        log.trace("Found account: {}", telephonyAccount);

        scheduledAnalyses.put(
                accountId,
                scheduledExecutor.scheduleWithFixedDelay(() -> {
                    try (Browser browser = browsersFactory.get()) {
                        pageService.loginInAccount(browser, telephonyAccount.getLogin(), telephonyAccount.getPassword());
                        pageService.downloadCalls(browser, telephonyAccount.getOldestDate(), telephonyAccount.getNewestDate(), moonClient);

//                        crmAccountsService.newestDateToPreviousDay(accountId);
//                        log.debug("The oldest date has been set to {}", telephonyAccount.getOldestDate());

                    } catch (Exception exception) {
                        log.error("Error in CrmAnalysisScheduler::scheduleCollecting");
                        exception.printStackTrace();
                    }

                }, 0, telephonyAccount.getAnalysisPeriod(), TimeUnit.MINUTES)
        );

        log.debug("Collecting for account: {} has been scheduled", accountId);
    }

    public void cancelAnalysis(UUID id) {
        log.debug("Cancelling the analysis...");

        while (!scheduledAnalyses.get(id).isCancelled()) {
            scheduledAnalyses.get(id).cancel(true);
        }

        scheduledAnalyses.remove(id);
        crmAccountsService.setPausedStatus(id);

        log.debug("Analysis successfully canceled!");
    }

    @PreDestroy
    public void onDestroy() {
        for (PageService pageService : pageServices) {
            try {
                pageService.close();
            } catch (Exception exception) {
                log.error("Error while closing pageService!");
                exception.printStackTrace();
            }
        }
    }
}
