package ru.lvmlabs.neuronum.calls.dto.controller;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Модель фильтров, для которых получить списки с доступными вариантами")
public class FiltersToFindValues {
    @NotEmpty(message = "nameOfFiltersToFind must be non-null and non-empty")
    private List<String> nameOfFiltersToFind;

    @Valid
    @JsonUnwrapped
    private DateTimeDto dateTimeDto;
}
