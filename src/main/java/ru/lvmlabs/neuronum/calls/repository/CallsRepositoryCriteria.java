package ru.lvmlabs.neuronum.calls.repository;

import ru.lvmlabs.neuronum.calls.dto.CallResponseDto;
import ru.lvmlabs.neuronum.calls.dto.controller.DateTimeDto;
import ru.lvmlabs.neuronum.calls.dto.controller.FiltersToApply;
import ru.lvmlabs.neuronum.users.enums.Clinic;

import java.util.List;

public interface CallsRepositoryCriteria {
    long findNumberOfCallsWithFilters_WithoutPaging(FiltersToApply filtersToApply, Clinic clinic);

    List<CallResponseDto> findNumberOfCallsWithFilters_WithPaging(FiltersToApply filtersToApply, Clinic clinic, boolean ascending, String... columnsToSort);

    List<String> getPossibleFilterVariants(String nameOfFilter, DateTimeDto dateTimeToFilter, Clinic clinic);
}
