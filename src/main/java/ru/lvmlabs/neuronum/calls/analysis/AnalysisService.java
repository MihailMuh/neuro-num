package ru.lvmlabs.neuronum.calls.analysis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.lvmlabs.neuronum.calls.analysis.constants.TelephonyConstants;
import ru.lvmlabs.neuronum.calls.dto.LLmParsingResponse;
import ru.lvmlabs.neuronum.sharedmodules.llm.LLMService;
import ru.lvmlabs.neuronum.sharedmodules.llm.constants.LLM;
import ru.lvmlabs.neuronum.sharedmodules.lucene.LuceneService;
import ru.lvmlabs.neuronum.sharedmodules.lucene.entity.LuceneSearch;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {
    private static final String instructionComplaints = Instruction.builder()
            .complaintWithAttention()
            .complaintWithPatient()
            .complaintWithClinic()
            .whyNo()
            .build();
    private static final String instructionCommon = Instruction.builder()
            .analysis()
            .lateMarker()
            .record()
            .doctor()
            .administratorName()
            .clientName()
            .wasBefore()
            .adminQuality()
            .build();

    private final LuceneService luceneService;

    private final LLMService LLMservice;

    @Nullable
    public LLmParsingResponse getAnalysis(String dialog, List<String> administratorNames, List<String> doctorSpecialties) {
        val dialogLower = dialog.toLowerCase();

        if (dialogLower.endsWith("алло") ||
            dialogLower.endsWith("алло.") ||
            TelephonyConstants.TRASH_TEXT_MARKERS.stream().anyMatch(dialogLower::contains) ||
            (
                    !dialogLower.replace("администратор: ", "").contains("администратор")
                    && dialogLower.split(" ").length <= 35
            )) {

            log.warn("Text is wrong!");
            return null;
        }

        LLmParsingResponse llmAnswer = getLLmResponse(dialog, true);
        if (llmAnswer == null) {
            log.warn("LLM answer is wrong!");
            return null;
        }

        if (dialogLower.contains("налогов")) {
            llmAnswer.setComplaint("налоговый вычет");
        }
        if (dialogLower.contains("лист ожидан") || dialogLower.contains("список ожидан")) {
            llmAnswer.setRecord("лист ожидания");
            llmAnswer.setComplaint("");
        }

        llmAnswer.setText(dialog);
        llmAnswer.setClientName(normalizeClientNameKey(llmAnswer.getClientName()));
        llmAnswer.setWasBefore(normalizeWasBeforeKey(llmAnswer.getWasBefore()));
        llmAnswer.setWhyNo(normalizeWhyNoKey(llmAnswer.getWhyNo()));

        try (LuceneSearch luceneSearch = luceneService.search(administratorNames, doctorSpecialties, dialog)) {
            if (!administratorNames.contains(llmAnswer.getAdministratorName())) {
                llmAnswer.setAdministratorName(luceneSearch.findAdministratorName(llmAnswer.getAdministratorName()));
            }
            llmAnswer.setDoctor(luceneSearch.findDoctorSpecialty(llmAnswer.getDoctor()));
        } catch (IOException exception) {
            log.error("Can't create LuceneSearch!");
            exception.printStackTrace();
        }

        return llmAnswer;
    }

    @NonNull
    private String normalizeWasBeforeKey(String wasBefore) {
        if (wasBefore == null || wasBefore.isBlank()) return "";
        wasBefore = wasBefore.toLowerCase();

        if (wasBefore.equals("no") || wasBefore.contains("нет") ||
            wasBefore.contains("перв") || wasBefore.contains("не ") || wasBefore.contains("не,")) {
            return "нет";
        }

        if (wasBefore.contains("yes") || wasBefore.contains("да") ||
            wasBefore.contains("втор") || wasBefore.contains("был")) {
            return "да";
        }

        return "";
    }

    @NonNull
    private String normalizeClientNameKey(String clientName) {
        if (clientName == null || clientName.isBlank()) return "";

        clientName = clientName.replace(".", "")
                .replace("фамилия", "")
                .replace("Фамилия", "")
                .replace("пациент", "")
                .replace("Пациент", "")
                .replace("девушка", "")
                .strip();

        while (clientName.contains(")") && clientName.contains("(")) {
            clientName = clientName.replace(clientName.substring(clientName.indexOf("("), clientName.indexOf(")") + 1), "").strip();
        }

        clientName = clientName.replace(" ,", ",");
        String clientNameLower = clientName.toLowerCase();

        if (TelephonyConstants.TRASH_CLIENT_NAMES.stream().anyMatch(clientNameLower::contains) ||
            (clientNameLower.length() <= 3 && TelephonyConstants.SHORT_NAMES.stream().noneMatch(clientNameLower::equals)) ||
            clientNameLower.replace(",", "").contains("имя отчество") ||
            clientNameLower.contains("?")) {

            return "";
        }

        return clientName;
    }

    @NonNull
    private String normalizeWhyNoKey(String whyNo) {
        if (whyNo == null || whyNo.isBlank()) return "";

        String finalWhyNo = whyNo.toLowerCase();

        if (finalWhyNo.endsWith(".")) {
            finalWhyNo = finalWhyNo.substring(0, finalWhyNo.length() - 1);
        }

        if ((whyNo.contains("успешн") && !whyNo.contains("не успешн")) ||
            (whyNo.contains("был записан") && !whyNo.contains("не был записан") && !whyNo.contains("был записан, но")) ||
            TelephonyConstants.TRASH_WHY_NO.stream().anyMatch(whyNo::contains) ||
            TelephonyConstants.TRASH_TEXT_MARKERS.stream().anyMatch(whyNo::contains)) {
            return "";
        }

        return finalWhyNo;
    }

    @Nullable
    private LLmParsingResponse getLLmResponse(String dialog, boolean needComplaints) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            Supplier<Map<String, String>> complainsResponse = () -> null;

            if (needComplaints) {
                complainsResponse = scope.fork(
                        () -> LLMservice.completionJSON(
                                LLM.GPT3_5,
                                new ChatCompletionMessage(instructionComplaints, ChatCompletionMessage.Role.SYSTEM),
                                new ChatCompletionMessage(dialog, ChatCompletionMessage.Role.USER)
                        )
                );
            }
            Supplier<Map<String, String>> commonResponse = scope.fork(
                    () -> LLMservice.completionJSON(
                            LLM.GPT3_5,
                            new ChatCompletionMessage(instructionCommon, ChatCompletionMessage.Role.SYSTEM),
                            new ChatCompletionMessage(dialog, ChatCompletionMessage.Role.USER)
                    )
            );

            scope.joinUntil(Instant.now().plus(5, ChronoUnit.MINUTES));

            LLmParsingResponse lLmParsingResponse = LLmParsingResponse.getInstance(
                    complainsResponse.get(), commonResponse.get()
            );
            if (lLmParsingResponse == null || lLmParsingResponse.isEmpty()) {
                log.warn("LLM answer is wrong!");
                return null;
            }
            return lLmParsingResponse;

        } catch (Exception exception) {
            log.error("Can't process llm analysis pipeline!");
            exception.printStackTrace();
            return null;
        }
    }
}
