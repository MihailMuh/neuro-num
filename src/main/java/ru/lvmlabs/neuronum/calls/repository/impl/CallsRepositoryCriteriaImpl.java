package ru.lvmlabs.neuronum.calls.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.lvmlabs.neuronum.calls.dto.CallResponseDto;
import ru.lvmlabs.neuronum.calls.dto.controller.DateTimeDto;
import ru.lvmlabs.neuronum.calls.dto.controller.FiltersToApply;
import ru.lvmlabs.neuronum.calls.model.Call;
import ru.lvmlabs.neuronum.calls.repository.CallsRepositoryCriteria;
import ru.lvmlabs.neuronum.users.enums.Clinic;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Repository
public class CallsRepositoryCriteriaImpl implements CallsRepositoryCriteria {
    private static final List<String> callResponseDtoFields = Arrays.stream(CallResponseDto.class.getDeclaredFields()).map(Field::getName).toList();

    private final EntityManager entityManager;

    public CallsRepositoryCriteriaImpl(@Qualifier("callsEntityManagerFactory") EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public long findNumberOfCallsWithFilters_WithoutPaging(FiltersToApply filtersToApply, Clinic clinic) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Call> call = criteriaQuery.from(Call.class);

        Collection<Predicate> predicates = applyFiltersToQuery(criteriaBuilder, call, filtersToApply, clinic);

        return entityManager
                .createQuery(
                        criteriaQuery.select(criteriaBuilder.count(call))
                                .where(predicates.toArray(new Predicate[0]))
                )
                .getSingleResult();
    }

    @Override
    public List<CallResponseDto> findNumberOfCallsWithFilters_WithPaging(FiltersToApply filtersToApply, Clinic clinic, boolean ascending, String... columnsToSort) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CallResponseDto> criteriaQuery = criteriaBuilder.createQuery(CallResponseDto.class);
        Root<Call> call = criteriaQuery.from(Call.class);

        Collection<Predicate> predicates = applyFiltersToQuery(criteriaBuilder, call, filtersToApply, clinic);

        criteriaQuery.select(
                criteriaBuilder.construct(
                        CallResponseDto.class,
                        callResponseDtoFields.stream()
                                .map(call::get)
                                .toArray(Selection[]::new)
                )
        ).orderBy(
                Stream.of(columnsToSort)
                        .map(columnToSort -> ascending ? criteriaBuilder.asc(call.get(columnToSort)) : criteriaBuilder.desc(call.get(columnToSort)))
                        .toList()
        );

        return entityManager
                .createQuery(criteriaQuery.where(predicates.toArray(new Predicate[0])))
                .setMaxResults(filtersToApply.getPageSize())
                .setFirstResult(filtersToApply.getPageNumber() * filtersToApply.getPageSize())
                .getResultList();
    }

    @Override
    public List<String> getPossibleFilterVariants(String nameOfFilter, DateTimeDto dateTimeToFilter, Clinic clinic) {
        if (!callResponseDtoFields.contains(nameOfFilter)) return Collections.emptyList();
        if ("isIncoming".equals(nameOfFilter)) return List.of("true", "false");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);

        List<Predicate> predicates = new ArrayList<>();
        Root<Call> call = criteriaQuery.from(Call.class);
        Expression<String> columnWhereFind = call.get(nameOfFilter);

        predicates.add(criteriaBuilder.equal(call.get("clinic"), clinic));
        predicates.add(criteriaBuilder.isNotNull(columnWhereFind));
        predicates.add(criteriaBuilder.notEqual(columnWhereFind, ""));
        predicates.addAll(applyDateTimeConditions(dateTimeToFilter, criteriaBuilder, call));

        return entityManager.createQuery(
                        criteriaQuery.multiselect(columnWhereFind)
                                .distinct(true)
                                .where(predicates.toArray(new Predicate[0]))
                )
                .getResultList();
    }

    private Collection<Predicate> applyDateTimeConditions(DateTimeDto dateTimeToFilter, CriteriaBuilder criteriaBuilder, Root<Call> call) {
        List<Predicate> predicates = new ArrayList<>();

        Date newestTime = dateTimeToFilter.getNewestTime();
        Date oldestTime = dateTimeToFilter.getOldestTime();
        Date newestDate = dateTimeToFilter.getNewestDate();
        Date oldestDate = dateTimeToFilter.getOldestDate();

        predicates.add(criteriaBuilder.between(call.get("date"), oldestDate, newestDate));
        if (newestTime != null && oldestTime != null) {
            predicates.add(criteriaBuilder.between(call.get("time"), oldestTime, newestTime));
        }

        return predicates;
    }

    private Collection<Predicate> applyFiltersToQuery(CriteriaBuilder criteriaBuilder, Root<Call> call, FiltersToApply filtersToApply, Clinic clinic) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.equal(call.get("clinic"), clinic));

        if (filtersToApply.getPhoneNumber() != null) {
            predicates.add(criteriaBuilder.like(call.get("phoneNumber"), "%" + filtersToApply.getPhoneNumber() + "%"));
        }
        predicates.addAll(applyDateTimeConditions(filtersToApply.getDateTimeDto(), criteriaBuilder, call));

        // filters processing

        FiltersToApply.Filters filters = filtersToApply.getFilters();
        if (filters == null) {
            return predicates;
        }

        if (filters.getVirtualNumber() != null && !filters.getVirtualNumber().isEmpty()) {
            predicates.add(criteriaBuilder.like(call.get("virtualNumber"), "%" + filters.getVirtualNumber() + "%"));
        }

        if (filters.getIsIncoming() != null) {
            predicates.add(criteriaBuilder.equal(call.get("isIncoming"), filters.getIsIncoming()));
        }

        if (filters.getRecord() != null && !filters.getRecord().isEmpty()) {
            predicates.add(criteriaBuilder.like(call.get("record"), "%" + filters.getRecord() + "%"));
        }

        if (filters.getDoctor() != null && !filters.getDoctor().isEmpty()) {
            predicates.add(criteriaBuilder.like(call.get("doctor"), "%" + filters.getDoctor() + "%"));
        }

        if (filters.getAdministratorName() != null && !filters.getAdministratorName().isEmpty()) {
            predicates.add(criteriaBuilder.like(call.get("administratorName"), "%" + filters.getAdministratorName() + "%"));
        }

        if (filters.getWasBefore() != null && !filters.getWasBefore().isEmpty()) {
            predicates.add(criteriaBuilder.like(call.get("wasBefore"), "%" + filters.getWasBefore() + "%"));
        }

        if (filters.getComplaint() != null && !filters.getComplaint().isEmpty()) {
            predicates.add(criteriaBuilder.like(call.get("complaint"), "%" + filters.getComplaint() + "%"));
        }

        if (filters.getClientName() != null && !filters.getClientName().isEmpty()) {
            predicates.add(criteriaBuilder.like(call.get("clientName"), "%" + filters.getClientName() + "%"));
        }

        if (filters.getWhyNo() != null && !filters.getWhyNo().isEmpty()) {
            predicates.add(criteriaBuilder.like(call.get("whyNo"), "%" + filters.getWhyNo() + "%"));
        }

        if (filters.getLateMarker() != null && !filters.getLateMarker().isEmpty()) {
            predicates.add(criteriaBuilder.like(call.get("lateMarker"), "%" + filters.getLateMarker() + "%"));
        }

        return predicates;
    }
}
