package ru.lvmlabs.neuronum.sharedmodules.transcribe.impl;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.lvmlabs.neuronum.baseconfigs.utils.RestClientWrap;
import ru.lvmlabs.neuronum.sharedmodules.transcribe.TranscribationService;

import java.util.concurrent.Semaphore;

@Slf4j
@Primary
@Service
@NoArgsConstructor
public class WhisperService extends RestClientWrap implements TranscribationService {
    private static final Semaphore semaphore = new Semaphore(1);

    @Value("${neuronum.whisper.url}")
    private String whisperUrl;

    @Override
    @NonNull
    public String transcribe(String downloadUrl) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            log.warn("Current thread is interrupted! Exiting...");
            return "";
        }

        if (downloadUrl == null || downloadUrl.isEmpty()) return "";
        log.debug("Sending a file to whisper service...");

        try {
            String response = restClient
                    .get()
                    .uri(whisperUrl + "?url=" + downloadUrl)
                    .retrieve()
                    .body(String.class);

            return normalizeWhisperAnswer(response);
        } catch (Exception exception) {
            log.error("Cant send a file to whisper!");
            exception.printStackTrace();
        } finally {
            semaphore.release();
        }

        return "";
    }

    @Override
    @NonNull
    public String transcribe(Resource fileResource) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            log.warn("Current thread is interrupted! Exiting...");
            return "";
        }

        if (fileResource == null || !fileResource.isReadable()) return "";
        log.debug("Sending a file to whisper service...");

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("audio", fileResource);

        try {
            String response = restClient
                    .post()
                    .uri(whisperUrl)
                    .body(parts)
                    .retrieve()
                    .body(String.class);

            return normalizeWhisperAnswer(response);
        } catch (Exception exception) {
            log.error("Cant send a file to whisper!");
            exception.printStackTrace();
        } finally {
            semaphore.release();
        }

        return "";
    }

    private String normalizeWhisperAnswer(String whisperOutput) {
        if (whisperOutput != null) {
            whisperOutput = whisperOutput.replace("\"", "")
                    .replace("\\n", "\n")
                    .replace("Продолжение следует...", "")
                    .strip();

            log.trace("Whisper output: '{}'", whisperOutput);
            return whisperOutput;
        }

        return "";
    }
}
