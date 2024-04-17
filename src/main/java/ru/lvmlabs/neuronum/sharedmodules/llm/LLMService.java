package ru.lvmlabs.neuronum.sharedmodules.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.lvmlabs.neuronum.calls.analysis.Instruction;
import ru.lvmlabs.neuronum.sharedmodules.llm.constants.LLM;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LLMService {
    private final ObjectMapper objectMapper;

    private final OpenAiApi openAiApi;

    public LLMService(ObjectMapper objectMapper,
                      @Value("${spring.ai.openai.base-url}") String openaiBaseUrl,
                      @Value("${spring.ai.openai.api-key}") String openaiApiKey) {

        this.objectMapper = objectMapper;
        openAiApi = new OpenAiApi(openaiBaseUrl, openaiApiKey);
    }

    //    @PostConstruct
    public void f() {
        String dialog = """
                Администратор: Дания, здравствуйте.
                Пациент: Здравствуйте, Дания, скажите, пожалуйста, мы записывались на четвертое число к лору. Мы не можем прийти. Можно запись отменить?
                Администратор: Да, конечно, подскажите, пожалуйста, на какое время и вашу фамилию?
                Пациент: Головин Григорий, четвертое число.
                Администратор: Так, время?
                Пациент: 11:45, по-моему.
                Администратор: Так, сейчас найду вас. А, вот, Григорий Николаевич, да, на 11:45. Все хорошо, отменяю вашу запись.
                Пациент: Там потом, когда надо будет, снова записываемся, позвоним.
                Администратор: Хорошо, спасибо.
                Администратор: До свидания.
                """;

//        String dialog = """
//                Администратор: Лена, добрый день, чем могу помочь?
//                Пациент: Да, Елена, добрый день. Скажите, пожалуйста, а доктор Мор еще не освободилась?
//                Администратор: Точнее, что? Ну, как бы сейчас совсем ничего не получается решить вопрос.
//                Пациент: Вас не приняли в 23-е?
//                Администратор: Нет, мы сейчас сюда подъезжаем, но я просто знаю, что в 23-е там очереди по 50 человек, и бывает, что, не знаю, ждешь там 5 часов, 12 часов люди ждут. Вот как-нибудь поактивизировать какой-нибудь ресурс, чтобы нас там приняли хотя бы.
//                Пациент: Смотрю, я не знаю, доктор, я ее или нет.
//                Администратор: Да, повторяйтесь, пожалуйста, потому что, честно, в сороковую мы поехали, время-то потеряли просто.
//                Пациент: Да, сейчас у нее пока прием, только зашли люди.
//                Администратор: А в перерыве-то она не смогла позвонить? Вы сказали, что в перерыве она позвонит.
//                Пациент: Нет, я не говорил, что она в перерыве позвонит. Я сказал, что я ей сообщу в перерыве, что вы звонили.
//                Администратор: Так, вы сообщили в перерыве?
//                Пациент: Да, я сказал ей, что вы поехали в 23-е.
//                Администратор: Нет, вы неправильно сказали.
//                Пациент: Я сказал, что вас не приняли в 40-е, вы поехали в 23-е.
//                Администратор: Нет, вы неправильно сказали. Правильно было так: в 40-е вообще нет приема ЛОРа, абсолютно нет, и мы зря туда приехали. Вопрос так стоит. А вы передали ситуацию так, будто нас там не приняли по какой-нибудь, может быть, нашей причине.
//                Пациент: А по причине, что нет ЛОРа, я сказал.
//                Администратор: А, предсказали.
//                Пациент: Да, да, да, предсказали, что нет ЛОРа, да.
//                Администратор: Ну и что, вот как-нибудь уже-таки получится какой-то ресурс активизировать, попасть хотя бы в 23-е?
//                Пациент: Ну, я не могу сказать, я узнаю у доктора, что она там вам говорит.
//                Администратор: Ну, да, ну, как бы, да, мы время просто потеряли, пустую очень много.
//                Пациент: А в 40-е там еще сначала только ладились с бумагами, а потом сказали, что вообще приема нет. Зачем вообще время я терял? Короче говоря, суть просьбы - максимально помочь решить вопрос 23. Деньги, не деньги, как угодно. Вот суть просьбы.
//                Администратор: Я спрошу у доктора. Сейчас освободится она, пациенты выйдут с приемом.
//                Пациент: Да, пожалуйста, именно суть помощи в 23. Да, именно в помощи, чтобы хотя бы приняли. Потому что там запредельно что-то творится. И не попасть никогда. Суть помощи.
//                Администратор: Спасибо большое.
//                Пациент: Спасибо. До свидания.
//                """;

        completion(
                LLM.GPT3_5,
                new OpenAiApi.ChatCompletionMessage(
                        Instruction.builder()
//                                    .analysis()
//                                    .lateMarker()
//                                    .record()
//                                    .doctor()
//                                    .administratorName()
//                                    .clientName()
//                                    .wasBefore()
                                .complaintWithAttention()
                                .complaintWithPatient()
                                .complaintWithClinic()
                                .whyNo()
//                                    .adminQuality()
                                .build(), OpenAiApi.ChatCompletionMessage.Role.SYSTEM),
                new OpenAiApi.ChatCompletionMessage(dialog, OpenAiApi.ChatCompletionMessage.Role.USER)
        );


        completion(
                LLM.GPT3_5,
                new OpenAiApi.ChatCompletionMessage(
                        Instruction.builder()
                                .analysis()
                                .lateMarker()
                                .record()
                                .doctor()
                                .administratorName()
                                .clientName()
                                .wasBefore()
//                                .complaintWithAttention()
//                                .complaintWithPatient()
//                                .complaintWithClinic()
//                                .whyNo()
                                .adminQuality()
                                .build(), OpenAiApi.ChatCompletionMessage.Role.SYSTEM),
                new OpenAiApi.ChatCompletionMessage(dialog, OpenAiApi.ChatCompletionMessage.Role.USER)
        );
    }

    @NonNull
    public String completion(String model, OpenAiApi.ChatCompletionMessage... messages) {
        if (messages == null || messages.length == 0) return "";

        try {
            ResponseEntity<OpenAiApi.ChatCompletion> response = openAiApi.chatCompletionEntity(
                    new OpenAiApi.ChatCompletionRequest(
                            List.of(messages), model,
                            null, null, null, null, null,
                            new OpenAiApi.ChatCompletionRequest.ResponseFormat("json_object"),
                            0, null,
                            false, 0f,
                            null, null, null, null
                    )
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) return "";

            log.debug("GPT answer: {}", response.getBody());

            List<OpenAiApi.ChatCompletion.Choice> choices = response.getBody().choices();
            if (choices == null || choices.isEmpty()) return "";

            OpenAiApi.ChatCompletion.Choice choice = choices.getFirst();
            if (choice.message() == null || choice.message().content() == null) return "";

            return choice.message().content();

        } catch (Exception exception) {
            log.error("Can't send GPT request!");
            exception.printStackTrace();

            return "";
        }
    }

    @Nullable
    public Map<String, String> completionJSON(String model, OpenAiApi.ChatCompletionMessage... messages) {
        String response = completion(model, messages);
        if (response.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(response, HashMap.class);
        } catch (JsonProcessingException e) {
            log.warn("Can't parse response!");
            return null;
        }
    }
}
