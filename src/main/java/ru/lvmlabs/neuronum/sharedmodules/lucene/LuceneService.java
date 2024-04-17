package ru.lvmlabs.neuronum.sharedmodules.lucene;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.springframework.stereotype.Service;
import ru.lvmlabs.neuronum.sharedmodules.lucene.entity.LuceneSearch;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class LuceneService {
    private static final Analyzer analyzer = new RussianAnalyzer(CharArraySet.copy(Set.of("к")));

    public LuceneSearch search(List<String> administratorNames, List<String> doctorSpecialities, String text) throws IOException {
        return new LuceneSearch(administratorNames, doctorSpecialities, text, analyzer);
    }

    @PreDestroy
    public void onDestroy() {
        log.debug("Closing lucene analyser...");
        analyzer.close();
    }
}
