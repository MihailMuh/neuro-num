package ru.lvmlabs.neuronum.calls.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.lvmlabs.neuronum.calls.dto.CallAudioResponse;
import ru.lvmlabs.neuronum.calls.model.Call;
import ru.lvmlabs.neuronum.users.enums.Clinic;

import java.util.Date;
import java.util.UUID;

public interface CallsRepository extends JpaRepository<Call, UUID>, CallsRepositoryCriteria {
    boolean existsByTimeAndPhoneNumber(Date time, String phoneNumber);

    @Transactional(transactionManager = "callsTransactionManager")
    @Query("SELECT NEW ru.lvmlabs.neuronum.calls.dto.CallAudioResponse(call.mp3, call.fileName) " +
           "FROM Call call " +
           "WHERE call.id = ?1 AND call.clinic = ?2")
    CallAudioResponse getMp3AndFileNameByIdAndClinic(UUID id, Clinic clinic);

    @Query("UPDATE Call call SET call.comment = ?2 WHERE call.id = ?1 AND call.clinic = ?3")
    void updateCommentByCallIdAndClinic(UUID callId, String comment, Clinic clinic);
}
