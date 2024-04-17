package ru.lvmlabs.neuronum.users.repository;

import ru.lvmlabs.neuronum.users.model.CrmTelephonyAccount;
import ru.lvmlabs.neuronum.users.enums.AnalysisStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.UUID;

public interface CrmTelephonyAccountsRepository extends JpaRepository<CrmTelephonyAccount, UUID> {
    @Transactional
    @Modifying
    @Query("UPDATE CrmTelephonyAccount crmTelephonyAccount " +
           "SET crmTelephonyAccount.newestDate = ?2 " +
           "WHERE crmTelephonyAccount.id = ?1")
    void updateNewestTimeById(UUID id, Date newestDate);

    @Transactional
    @Modifying
    @Query("UPDATE CrmTelephonyAccount crmTelephonyAccount " +
           "SET crmTelephonyAccount.analysisStatus = ?2 " +
           "WHERE crmTelephonyAccount.id = ?1")
    void updateStatusById(UUID id, AnalysisStatus status);
}
