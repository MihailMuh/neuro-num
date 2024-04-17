package ru.lvmlabs.neuronum.calls.service.extension;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.lvmlabs.neuronum.calls.analysis.AnalysisService;
import ru.lvmlabs.neuronum.calls.service.CallsService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
class ExtensionsRegistrar {
    private final ApplicationContext context;

    private final AnalysisService analysisService;
    private final CallsService callsService;

    @SneakyThrows
    @PostConstruct
    public void registerExtensions() {
        Map<String, Object> extensions = context.getBeansWithAnnotation(CallsExtension.class);
        log.debug("Found {} extensions", extensions.size());

        for (val extension : extensions.values()) {
            CallsExtension callsExtension = extension.getClass().getAnnotation(CallsExtension.class);

            Arrays.stream(extension.getClass().getFields())
                    .filter(field -> field.getType().equals(CallsBroker.class))
                    .findFirst()
                    .get()
                    .set(extension, new CallsBroker(
                            callsService, analysisService,
                            List.of(callsExtension.administratorNames()), List.of(callsExtension.doctorSpecialties())
                    ));
        }
    }
}
