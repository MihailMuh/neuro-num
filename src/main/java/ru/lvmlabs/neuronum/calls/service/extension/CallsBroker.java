package ru.lvmlabs.neuronum.calls.service.extension;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.lvmlabs.neuronum.calls.analysis.AnalysisService;
import ru.lvmlabs.neuronum.calls.dto.LLmParsingResponse;
import ru.lvmlabs.neuronum.calls.model.Call;
import ru.lvmlabs.neuronum.calls.service.CallsService;

import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class CallsBroker {
    private final CallsService callsService;

    private final AnalysisService analysisService;

    private final List<String> administratorNames;
    private final List<String> doctorSpecialties;

    public void create(Call call) {
        LLmParsingResponse lLmParsingResponse = analysisService.getAnalysis(
                call.getText(),
                administratorNames,
                doctorSpecialties
        );

        callsService.create(call, lLmParsingResponse);
    }

    public boolean existsByTimeAndPhoneNumber(Date time, String phoneNumber) {
        return callsService.existsByTimeAndPhoneNumber(time, phoneNumber);
    }
}
