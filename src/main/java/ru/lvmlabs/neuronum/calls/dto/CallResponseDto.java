package ru.lvmlabs.neuronum.calls.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Модель звонка")
public class CallResponseDto {
    @Schema(description = "ID звонка", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    @Schema(description = "Текст звонка", example = "Здравствуйте. Администратор Дания...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String text;

    @Schema(description = "Дата звонка", example = "19.01.2024", implementation = String.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy", timezone = "Asia/Yekaterinburg")
    private Date date;

    @Schema(description = "Время звонка", example = "17:34:00", implementation = String.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @DateTimeFormat(pattern = "HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Yekaterinburg")
    private Date time;

    @Schema(description = "Номер телефона клиента", example = "+79120475070", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 20)
    private String phoneNumber;

    @Schema(description = "Виртуальный номер телефона", example = "+73432989000", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 20)
    private String virtualNumber;

    @JsonProperty("isIncoming") // because jackson convert this to "incoming"
    @Schema(description = "Звонок входящий?", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean isIncoming;

    // analysis

    @Schema(
            description = "Анализ звонка, его темы и проблем",
            example = "Клиент интересовался наличием аллерголога в клинике, но администратор сообщила, что такого специалиста нет.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String analysis;

    @Schema(description = "Жалоба / налоговый вычет", example = "жалоба", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 24)
    private String complaint;

    @Schema(description = "Записан ли клиент / лист ожидания", example = "записан", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 20)
    private String record;

    @Schema(description = "Врачу, к которому хотели записаться", example = "сурдолог", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 30)
    private String doctor;

    @Schema(description = "Почему пациент не был записан на прием", example = "нет записи на прием на удобное для клиента время", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String whyNo;

    @Schema(description = "Имя пациента", example = "Иван Иванов", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 255)
    private String clientName;

    @Schema(description = "Имя администратора", example = "Дания", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 255)
    private String administratorName;

    @Schema(description = "Был ли пациент уже в клинике", example = "нет", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 3)
    private String wasBefore;

    @Schema(
            description = "Если пациент говорил о долгом ожидании",
            example = "клиент упоминал, что болеют уже неделю и хотел узнать, какой специалист посоветует, чтобы исключить осложнения",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String lateMarker;

    @Schema(
            description = "Оценка работы администратора",
            example = "администратор вежливо и корректно ответила на вопрос клиента, не было недопониманий или неясностей.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String adminQuality;

    @Schema(description = "Комментарий пользователя к звонку", example = "Треееш клиент. в чс его", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 5000)
    private String comment;
}
