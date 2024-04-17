package ru.lvmlabs.neuronum.sharedmodules.selenium.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.lvmlabs.neuronum.baseconfigs.utils.RestClientWrap;
import ru.lvmlabs.neuronum.baseconfigs.utils.ThreadUtils;
import ru.lvmlabs.neuronum.sharedmodules.selenium.domain.Browser;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoonClient extends RestClientWrap {
    @Value("${neuronum.moon.url}")
    private String moonUrl;

    @NonNull
    public String getUrlToFileWithNameContains(Browser browser, String toFind) {
        log.trace("Trying to find a file with name contains: {}", toFind);

        String moonWithSessionUrl = moonUrl + "/session/" + browser.getSessionId() + "/aerokube/download";
        for (int i = 0; i < 5; i++) {
            try {
                String availableFiles = restClient.get()
                        .uri(moonWithSessionUrl)
                        .retrieve()
                        .body(String.class);

                if (availableFiles != null && !availableFiles.isEmpty()) {
                    String fileName = Arrays
                            .stream(availableFiles.split("\""))
                            .filter(s -> s.contains(toFind) && !s.endsWith("crdownload"))
                            .findFirst()
                            .orElse(null);

                    log.trace("Found a file: {}", fileName);
                    if (fileName != null && !fileName.isEmpty()) {
                        return moonWithSessionUrl + "/" + fileName;
                    }
                }

            } catch (Exception exception) {
                log.error("Can't get available files!");
                exception.printStackTrace();
            }

            ThreadUtils.sleep(1_000);
        }

        return "";
    }

    // whe need 2024 from this
    // http://ip:port/wd/hub/session/chrome-119-0-6045-123-6-9ce857ad-1faf-4349-b285-362e0126a799/aerokube/download/2024-01-14_15-59-24.136740_from_79822698444_to_052116_session_3500833886_talk.mp3
    public String extractFileYearFromUrl(String url) {
        String[] temp = url.split("/");
        return temp[temp.length - 1].split("-")[0];
    }
}
