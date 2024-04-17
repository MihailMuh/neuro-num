package ru.lvmlabs.neuronum.earthroatnose.analysis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.lvmlabs.neuronum.baseconfigs.utils.RestClientWrap;
import ru.lvmlabs.neuronum.sharedmodules.lucene.LuceneService;
import ru.lvmlabs.neuronum.sharedmodules.transcribe.TranscribationService;

import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EarThroatNoseAnalysisService extends RestClientWrap {
    private final ExecutorService executorService;

    private final LuceneService luceneService;

    private final TranscribationService transcribationService;

    public void getAnalysis(Resource fileResource) {
        transcribationService.transcribe(fileResource);
    }
}
