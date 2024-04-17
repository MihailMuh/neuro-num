package ru.lvmlabs.neuronum.calls.dto.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Модель фильтров, которые учитывать при поиске звонков")
public class FiltersToApply {
    @Valid
    @JsonUnwrapped
    private DateTimeDto dateTimeDto;

    @Schema(description = "Номер страницы выборки", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 0, message = "pageNumber must be >= 0")
    private int pageNumber;

    @Schema(description = "Размер страницы выборки", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "pageNumber must be >= 1")
    private int pageSize;

    @Size(message = "phoneNumber is not valid!", max = 20)
    @Schema(description = "Звонки, с каким номером телефона искать", example = "+79120475070", maxLength = 20)
    private String phoneNumber;

    @Valid
    private Filters filters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Доп фильтры")
    public static class Filters {
        @Size(message = "virtualNumber is not valid!", max = 20)
        @Schema(description = "Виртуальный номер телефона для фильтрации", example = "+73432989000", maxLength = 20)
        private String virtualNumber;

        @JsonProperty("isIncoming") // because jackson convert this to "incoming"
        @Schema(description = "Звонок входящий?")
        private Boolean isIncoming;

        @Size(message = "record is not valid!", max = 20)
        @Schema(description = "Записан ли клиент / лист ожидания", example = "записан", maxLength = 20)
        private String record;

        @Size(message = "doctor is not valid!", max = 30)
        @Schema(description = "Врачу, к которому хотели записаться", example = "сурдолог", maxLength = 30)
        private String doctor;

        @Size(message = "administratorName is not valid!", max = 255)
        @Schema(description = "Имя администратора", example = "Дания", maxLength = 255)
        private String administratorName;

        @Size(message = "wasBefore is not valid!", max = 3)
        @Schema(description = "Был ли пациент уже в клинике", example = "нет", maxLength = 3)
        private String wasBefore;

        @Size(message = "complaint is not valid!", max = 24)
        @Schema(description = "Жалоба / налоговый вычет", example = "жалоба", maxLength = 24)
        private String complaint;

        // fuzzy values

        @Size(message = "clientName is not valid!", max = 255)
        @Schema(description = "Имя пациента", example = "Иван Иванов", maxLength = 255)
        private String clientName;

        @Schema(description = "Почему пациент не был записан на прием", example = "нет записи на прием на удобное для клиента время")
        private String whyNo;

        @Schema(
                description = "Если пациент говорил о долгом ожидании",
                example = "клиент упоминал, что болеют уже неделю и хотел узнать, какой специалист посоветует, чтобы исключить осложнения"
        )
        private String lateMarker;
    }
}
