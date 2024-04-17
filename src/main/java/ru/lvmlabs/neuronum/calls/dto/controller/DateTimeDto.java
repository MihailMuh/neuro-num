package ru.lvmlabs.neuronum.calls.dto.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Hidden
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateTimeDto {
    @Schema(
            description = "Дата, до какой выбирать значения фильтров. Должно быть больше, чем oldestDate", example = "29.01.2024",
            implementation = String.class
    )
    @NotNull(message = "newestDate must be non-null")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy", timezone = "Asia/Yekaterinburg")
    private Date newestDate;

    @Schema(
            description = "Дата, с какой выбирать значения фильтров. Должно быть меньше, чем newestDate", example = "01.01.2024",
            implementation = String.class
    )
    @NotNull(message = "oldestDate must be non-null")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy", timezone = "Asia/Yekaterinburg")
    private Date oldestDate;

    @Schema(
            description = "Время, до какого выбирать значения фильтров. Должно быть больше, чем oldestTime", example = "19:34:12",
            implementation = String.class
    )
    @DateTimeFormat(pattern = "HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Yekaterinburg")
    private Date newestTime;

    @Schema(
            description = "Время, с какого выбирать значения фильтров. Должно быть меньше, чем newestTime", example = "05:34:00",
            implementation = String.class
    )
    @DateTimeFormat(pattern = "HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Yekaterinburg")
    private Date oldestTime;
}
