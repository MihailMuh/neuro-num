package ru.lvmlabs.neuronum.sharedmodules.lucene.entity;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.springframework.lang.NonNull;
import ru.lvmlabs.neuronum.calls.analysis.constants.TelephonyConstants;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Slf4j
public class LuceneSearch implements Closeable {
    private static final Pattern bestFragmentsPattern = Pattern.compile("<B>(.*?)</B>");

    private final Directory indexDirectory = new ByteBuffersDirectory();
    private final IndexSearcher indexSearcher;
    private final Analyzer analyzer;
    private final StoredFields storedFields;

    private final List<String> doctorSpecialities;
    private final String text;

    public LuceneSearch(List<String> administratorNames, List<String> doctorSpecialities, String text, Analyzer analyzer) throws IOException {
        try (IndexWriter indexWriter = new IndexWriter(indexDirectory, new IndexWriterConfig(analyzer).setCommitOnClose(true))) {
            indexWriter.addDocuments(strListTodocumentList(administratorNames, "administratorName"));
            indexWriter.addDocuments(strListTodocumentList(doctorSpecialities, "specialty"));

            Document doc = new Document();
            doc.add(new TextField("text", text, Field.Store.YES));
            indexWriter.addDocument(doc);

        }

        indexSearcher = new IndexSearcher(DirectoryReader.open(indexDirectory));
        storedFields = indexSearcher.storedFields();
        this.analyzer = analyzer;

        this.doctorSpecialities = doctorSpecialities;
        this.text = text;
    }

    @SneakyThrows
    public void findScript() {
        String finishing = """
                Записала Алису Дмитриевну на среду 19 апреля к Суслановой Юлии Валерьевне
                                
                Я записываю Кирилла на субботу, 15 апреля на 13.00, Каратаево, Владление, Александровне.

                Значит, я записываю Льва на понедельник, 17 апреля на 16.20.
                И я его Алексею Викторовичу.
                                
                Значит, записываю Тимофея на четверг, 13 апреля, на 19.20 к Зыкину Олегу Владимировичу.

                Записала Елена Александровна на понедельник, 17 апреля, к Юркову Владиславу Сергеевичу в 16.30.

                Значит, я записываю вас, Илья, на вторник, 18 апреля на 12.40 к Сушкову Михаилу Германовичу.
                """;

//        String querystr = "да конечно я вам помочь рассказ записать представьтесь пожалуйста как я могу к вам обращаться";
        String querystr = "вы уже были в нашей клинике как давно";
        Query q = new QueryParser("text", analyzer).parse(querystr);

        QueryScorer scorer = new QueryScorer(q);
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
        Formatter formatter = new SimpleHTMLFormatter();
        Highlighter highlighter = new Highlighter(formatter, scorer);
        highlighter.setTextFragmenter(fragmenter);

        String[] frag = highlighter.getBestFragments(analyzer, "text", """
                клиника лечения кашля и аллергии, администратор Наталья, здравствуйте
                Наталья, здравствуйте, я бы хотела записаться на прием к Синдяеву
                Да, конечно, я вам помогу, представьтесь, пожалуйста
                Меня зовут Валерия, ребенку год и семь
                То есть ребенка будете записывать, да, Валерия?
                Да
                А на что он жалуется?
                У нас очень сильный храп начался, ни с того ни с сего
                В нашей клинике были уже?
                Нет, не были по рекомендации.
                Первый раз будете.
                Да, конечно, с прапом необходим прием лор-врача, так как это, возможно, аденоиды.
                На приеме будет проведен полный осмотр уха, горла и носа.
                Особое внимание уделят осмотру аденоидов.
                И в случае необходимости будут назначены дополнительные исследования и анализы.
                Поставят предварительный диагноз, порекомендуют план лечения
                с учетом ваших финансовых пожеланий.
                Вы хотите к Синяеву Алексею Викторовичу записаться?
                Да.
                Прием у него будет стоить 1400 рублей,
                так как он заведующий отделением.
                И дополнительно оплачивается диагностика,
                лечебные процедуры, забор анализов по необходимости.
                Ближайшая возможная запись к Синяеву Алексею Викторовичу
                будет на...
                Секунду, пожалуйста.
                Так, на понедельник, 17 апреля, на 16.20.
                Отлично.
                Полные фамилии и имя отчества пациента, пожалуйста.
                Медянкин Лев Александрович.
                Лев Александрович. И дата рождения?
                18.09.21.
                У нас за два часа до приема отправляется напоминание в виде смс-сообщения.
                Отлично.
                А вы хотели бы, чтобы оно приходило в WhatsApp?
                Да, без проблем. Номер привязан.
                Привязан к этому номеру? 3659, да?
                На этом, да.
                Значит, я записываю Льва на понедельник, 17 апреля на 16.20.
                И я его Алексею Викторовичу.
                Адрес газеты «Звезда» 31А.
                Прием, как я уже сказала, стоит 1400.
                Дополнительно оплачивается диагностика, процедура и забор анализов.
                Так как вы в первый раз обязательно возьмите с собой паспорт для оформления документации и письма для своего рождения.
                И подойти желательно за 5 минут до назначенного времени.
                Да, хорошо, я поняла. Спасибо большое.
                Всего доброго, до свидания
                Хорошего дня, до свидания
                Вам также
                """, 1);
        for (String textFragment : frag) {
            log.debug(textFragment);
        }
    }

    @NonNull
    public String findAdministratorName(String nameWithMistake) {
        if (nameWithMistake == null || nameWithMistake.isBlank()) return "";

        Query query = new FuzzyQuery(new Term("administratorName", nameWithMistake));
        try {
            return storedFields.document(indexSearcher.search(query, 1).scoreDocs[0].doc).get("administratorName");
        } catch (Exception exception) {
            log.warn("Can't find administrator name by: {}", nameWithMistake);
        }

        return "";
    }

    @NonNull
    public String findDoctorSpecialty(String specialtyWithMistake) {
        if (specialtyWithMistake == null || specialtyWithMistake.isBlank() || specialtyWithMistake.contains("не уточнен"))
            return "";
        List<String> specialtyWithMistakePartitions = Arrays.stream(
                        specialtyWithMistake
                                .toLowerCase()
                                .replace(",", " ")
                                .replace("детский", " ")
                                .replace("детского", " ")
                                .replace("взрослый", " ")
                                .replace("взрослого", " ")
                                .replace("(", " ")
                                .replace(")", " ")
                                .replace("-", " ")
                                .replace("врач", " ")
                                .strip()
                                .split(" "))
                .filter(part -> part != null && !part.isBlank())
                .toList();

        if (specialtyWithMistakePartitions.size() == 1) {
            String specialtyWithMistakeValidated = specialtyWithMistakePartitions.getFirst();

            if ("лор оториноларинголог отоларинголог".contains(specialtyWithMistakeValidated)) {
                return "лор";
            }

            for (String specialtiesLine : doctorSpecialities) {
                if (specialtiesLine.contains(specialtyWithMistakeValidated)) {
                    return specialtiesLine.split(" ")[0];
                }
            }
            if (TelephonyConstants.ALL_SPECIALTIES.contains(specialtyWithMistakeValidated)) {
                return ""; // for example: "окулист" not in familia
            }
        }

        List<String> possibleVariants = new ArrayList<>();

        try {
            String toParse = "\"" + String.join(" ", specialtyWithMistakePartitions) + "\"~";
            TopDocs topResults = indexSearcher.search(new ComplexPhraseQueryParser("specialty", analyzer).parse(toParse), 1);

            if (topResults != null && topResults.scoreDocs != null && topResults.scoreDocs.length != 0) {
                possibleVariants.add(storedFields.document(topResults.scoreDocs[0].doc).get("specialty").split(" ")[0]);
            }
        } catch (Exception exception) {
            log.error("Error with doctor specialty: {}", specialtyWithMistake);
            exception.printStackTrace();
        }

        for (String partition : specialtyWithMistakePartitions) {
            try {
                TopDocs topResults = indexSearcher.search(new FuzzyQuery(new Term("specialty", partition)), 2);
                if (topResults == null || topResults.scoreDocs == null) continue;

                for (var scoreDoc : topResults.scoreDocs) {
                    possibleVariants.add(storedFields.document(scoreDoc.doc).get("specialty").split(" ")[0]);
                }
            } catch (Exception exception) {
                log.error("Error with doctor specialty: {}", specialtyWithMistake);
                exception.printStackTrace();
            }
        }

        return possibleVariants.stream()
                .collect(Collectors.groupingBy(i -> i, Collectors.counting())) // to map
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("");
    }

    @Override
    public void close() {
        try {
            indexDirectory.close();
        } catch (IOException _) {
            // this never throws, because ByteBuffersDirectory uses hashmap
        }
    }

    public List<Document> strListTodocumentList(List<String> content, String textFieldName) {
        return content.stream()
                .map(line -> {
                    Document document = new Document();
                    document.add(new TextField(textFieldName, line.strip(), Field.Store.YES));
                    return document;
                })
                .toList();
    }

    private String extractBestFromFragments(String... fragments) {
        Matcher matcher = bestFragmentsPattern.matcher(String.join(" ", fragments));
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            if (!result.isEmpty()) {
                result.append(" ");
            }
            result.append(matcher.group(1));
        }

        return result.toString();
    }
}
