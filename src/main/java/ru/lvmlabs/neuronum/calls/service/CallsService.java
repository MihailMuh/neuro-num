package ru.lvmlabs.neuronum.calls.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.lvmlabs.neuronum.baseconfigs.utils.DateUtils;
import ru.lvmlabs.neuronum.calls.dto.CallAudioResponse;
import ru.lvmlabs.neuronum.calls.dto.CallResponseDto;
import ru.lvmlabs.neuronum.calls.dto.LLmParsingResponse;
import ru.lvmlabs.neuronum.calls.dto.controller.DateTimeDto;
import ru.lvmlabs.neuronum.calls.dto.controller.FiltersToApply;
import ru.lvmlabs.neuronum.calls.exceptions.ClinicNotFoundException;
import ru.lvmlabs.neuronum.calls.model.Call;
import ru.lvmlabs.neuronum.calls.repository.CallsRepository;
import ru.lvmlabs.neuronum.calls.ws.WebSocketHandler;
import ru.lvmlabs.neuronum.users.enums.Clinic;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallsService {
    private final CallsRepository callsRepository;

    private final WebSocketHandler webSocketHandler;

    public void create(Call call, LLmParsingResponse lLmParsingResponse) {
        if (lLmParsingResponse == null || lLmParsingResponse.isEmpty()) return;

        call.setText(lLmParsingResponse.getText());
        call.setAnalysis(lLmParsingResponse.getAnalysis());
        call.setComplaint(lLmParsingResponse.getComplaint());
        call.setRecord(lLmParsingResponse.getRecord());
        call.setDoctor(lLmParsingResponse.getDoctor());
        call.setWhyNo(lLmParsingResponse.getWhyNo());
        call.setClientName(lLmParsingResponse.getClientName());
        call.setAdministratorName(lLmParsingResponse.getAdministratorName());
        call.setWasBefore(lLmParsingResponse.getWasBefore());
        call.setLateMarker(lLmParsingResponse.getLateMarker());
        call.setAdminQuality(lLmParsingResponse.getAdminQuality());

        if (call.getMp3() != null && call.getMp3().length > 0) {
            convertAudioToMp3(call);
            call.setFileName(createFileName(call.getDate(), call.getTime(), call.getPhoneNumber()));
        }

        callsRepository.save(call);

        webSocketHandler.publish(call);
    }

    public Clinic parse(String clinic) {
        try {
            return Clinic.valueOf(clinic);
        } catch (IllegalArgumentException _) {
            log.error("Can't find required clinic by name: '{}'", clinic);
        }

        throw new ClinicNotFoundException();
    }

    public boolean existsByTimeAndPhoneNumber(Date time, String phoneNumber) {
        return callsRepository.existsByTimeAndPhoneNumber(time, phoneNumber);
    }

    public List<CallResponseDto> allWithPaging(FiltersToApply filtersToApply, Clinic clinic) {
        try {
            return callsRepository.findNumberOfCallsWithFilters_WithPaging(
                    filtersToApply, clinic, false, "date", "time"
            );
        } catch (Exception exception) {
            log.error("Can't execute allWithPaging with filters: {}", filtersToApply);
            exception.printStackTrace();
        }

        return Collections.emptyList();
    }

    public long countOfAllWithoutPaging(FiltersToApply filtersToApply, Clinic clinic) {
        try {
            return callsRepository.findNumberOfCallsWithFilters_WithoutPaging(filtersToApply, clinic);
        } catch (Exception exception) {
            log.error("Can't execute countOfAllWithoutPaging with filters: {}", filtersToApply);
            exception.printStackTrace();
        }

        return 0;
    }

    public List<String> getPossibleFilterVariants(String nameOfFilterToFind, DateTimeDto dateTimeToFilter, Clinic clinic) {
        try {
            List<String> foundVariants = callsRepository.getPossibleFilterVariants(nameOfFilterToFind, dateTimeToFilter, clinic);

            log.trace("Successfully got {} variants for filter '{}'", foundVariants.size(), nameOfFilterToFind);
            return foundVariants;
        } catch (Exception exception) {
            log.error("Can't execute getPossibleFilterVariants for filter: {}", nameOfFilterToFind);
            exception.printStackTrace();
        }

        return Collections.emptyList();
    }

    public CallAudioResponse downloadAudio(UUID callId, Clinic clinic) {
        try {
            return callsRepository.getMp3AndFileNameByIdAndClinic(callId, clinic);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new NoSuchElementException();
        }
    }

    public void saveComment(UUID callId, Clinic clinic, String comment) {
        callsRepository.updateCommentByCallIdAndClinic(callId, comment, clinic);
    }

    private void convertAudioToMp3(Call call) {
        byte[] mp3 = FFmpegService.convertToMp3(call.getMp3());
        if (mp3 == null) return;

        call.setMp3(mp3);
    }

    private String createFileName(Date date, Date time, String phoneNumber) {
        return DateUtils.toString(date, "dd_MM_yyyy") + "_" +
               DateUtils.toString(time, "HH_mm_ss") + "_" +
               phoneNumber + ".mp3";
    }
}
