package ru.lvmlabs.neuronum.users.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.lvmlabs.neuronum.users.dto.CrmAccountDto;
import ru.lvmlabs.neuronum.users.model.CrmTelephonyAccount;
import ru.lvmlabs.neuronum.users.repository.CrmTelephonyAccountsRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import static ru.lvmlabs.neuronum.users.enums.AnalysisStatus.PAUSED;
import static ru.lvmlabs.neuronum.users.enums.AnalysisStatus.RUNNING;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrmAccountsService {
    private final CrmTelephonyAccountsRepository crmTelephonyAccountsRepository;

    public void newestDateToPreviousDay(UUID accountId) {
        crmTelephonyAccountsRepository.updateNewestTimeById(
                accountId,
                Date.from(Instant.now().minus(1, ChronoUnit.DAYS))
        );
    }

    public void setPausedStatus(UUID accountId) {
        crmTelephonyAccountsRepository.updateStatusById(accountId, PAUSED);
    }

    @Nullable
    public CrmTelephonyAccount get(UUID accountId) {
        return crmTelephonyAccountsRepository.findById(accountId).orElse(null);
    }

    @Nullable
    public CrmAccountDto getIfSuitableStatus(UUID accountId) {
        CrmTelephonyAccount account = get(accountId);
        if (account == null) {
            log.warn("Can't find account by id: {}", accountId);
            return null;
        }

        if (account.getAnalysisStatus().equals(RUNNING)) {
            return new CrmAccountDto(
                    account.getPassword(),
                    account.getLogin(),
                    account.getNewestDate(),
                    account.getOldestDate(),
                    account.getAnalysisPeriod()
            );
        }

        log.warn("CrmTelephonyAccount status not suitable: {}", account.getAnalysisStatus());
        return null;
    }
}
