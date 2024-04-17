package ru.lvmlabs.neuronum.calls.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.lvmlabs.neuronum.calls.dto.CallAudioResponse;
import ru.lvmlabs.neuronum.calls.dto.CallResponseDto;
import ru.lvmlabs.neuronum.calls.dto.controller.FiltersToApply;
import ru.lvmlabs.neuronum.calls.dto.controller.FiltersToFindValues;
import ru.lvmlabs.neuronum.calls.service.CallsService;
import ru.lvmlabs.neuronum.users.enums.Clinic;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@Slf4j
@RestController
@RequestMapping("/calls/{clinic}")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = {"https://lvmlabs.ru,http://localhost:[*]"})
@Tag(name = "Звонки клиник", description = "Получение звонков с фильтрами для любой из клиник")
public class CallsController {
    private final CallsService callsService;

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации FiltersToApply!",
                    content = @Content(schema = @Schema)
            ),
            @ApiResponse(
                    responseCode = "418",
                    description = "Нет клиники с таким названием!",
                    content = @Content(schema = @Schema)
            )
    })
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @CrossOrigin(exposedHeaders = "calls-total-count")
    @Operation(summary = "Получить звонки, учитывая фильтры. В хидере 'calls-total-count' будет лежать общее количество звонков без учета пагинации")
    public ResponseEntity<List<CallResponseDto>> getCallsWithFilters(
            @Schema(description = "Название клиники в виде ENUM")
            @PathVariable String clinic,

            @RequestBody @Valid FiltersToApply filtersToApply) {

        Clinic clinicEnum = callsService.parse(clinic);

        log.debug("Getting calls in period...");
        log.trace("Filters to apply: {}", filtersToApply);

        try (var scope = new StructuredTaskScope<>()) {
            Supplier<Long> countOfAllWithoutPagingSupl = scope.fork(() -> callsService.countOfAllWithoutPaging(filtersToApply, clinicEnum));
            Supplier<List<CallResponseDto>> callsWithPagingSupl = scope.fork(() -> callsService.allWithPaging(filtersToApply, clinicEnum));

            scope.joinUntil(Instant.now().plusSeconds(4));

            List<CallResponseDto> callsWithPaging = callsWithPagingSupl.get();
            String countOfAllWithoutPaging = countOfAllWithoutPagingSupl.get().toString();

            log.trace("Calls with applied pagination: {}", callsWithPaging.size());
            log.trace("Total calls with such filters, without pagination {}", countOfAllWithoutPaging);
            return ResponseEntity.ok()
                    .header("calls-total-count", countOfAllWithoutPagingSupl.get().toString())
                    .body(callsWithPagingSupl.get());

        } catch (InterruptedException | TimeoutException e) {
            log.error("Can't join CallsController::getCallsWithFilters operations!");
            e.printStackTrace();
        }

        return ResponseEntity.ok()
                .header("calls-total-count", "0")
                .body(Collections.emptyList());
    }

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации FiltersToFindValues!",
                    content = @Content(schema = @Schema)
            ),
            @ApiResponse(
                    responseCode = "418",
                    description = "Нет клиники с таким названием!",
                    content = @Content(schema = @Schema)
            )
    })
    @PostMapping(value = "/filters", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Получить все возможные варианты для указанных фильтров")
    public ResponseEntity<Map<String, List<String>>> getPossibleFilters(
            @Schema(description = "Название клиники в виде ENUM")
            @PathVariable String clinic,

            @RequestBody @Valid FiltersToFindValues filtersToFindValues) {

        Clinic clinicEnum = callsService.parse(clinic);

        log.debug("Getting possible variants of filters...");

        try (var scope = new StructuredTaskScope<>()) {
            Map<String, Supplier<List<String>>> possibleVariantsSupl = new HashMap<>(filtersToFindValues.getNameOfFiltersToFind().size());

            for (String nameOfFilter : filtersToFindValues.getNameOfFiltersToFind()) {
                possibleVariantsSupl.put(
                        nameOfFilter,
                        scope.fork(() -> callsService.getPossibleFilterVariants(nameOfFilter, filtersToFindValues.getDateTimeDto(), clinicEnum))
                );
            }

            scope.joinUntil(Instant.now().plusSeconds(4));

            return ResponseEntity.ok(
                    possibleVariantsSupl
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()))
            );
        } catch (InterruptedException | TimeoutException e) {
            log.error("Can't join CallsController::getPossibleFilters operations!");
            e.printStackTrace();
        }

        return ResponseEntity.ok(HashMap.newHashMap(0));
    }

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации FiltersToApply!",
                    content = @Content(schema = @Schema)
            ),
            @ApiResponse(
                    responseCode = "418",
                    description = "Нет клиники с таким названием!",
                    content = @Content(schema = @Schema)
            )
    })
    @PostMapping(value = "/dashboards", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @CrossOrigin(exposedHeaders = "calls-total-count")
    @Operation(summary = "Получить число вариантов для указанных фильтров (хидер 'calls-total-count' из POST -> /calls/{clinic})")
    public ResponseEntity<Long> getDashBoardStatistics(
            @Schema(description = "Название клиники в виде ENUM")
            @PathVariable String clinic,

            @RequestBody @Valid FiltersToApply filtersToApply) {

        Clinic clinicEnum = callsService.parse(clinic);

        log.debug("Getting dashboard statistics by filters...");
        return ResponseEntity.ok(callsService.countOfAllWithoutPaging(filtersToApply, clinicEnum));
    }

    // /audio/download/{callId:.mp3} can't use because CORS failures
    @GetMapping(value = "/audio/download/{callId}", produces = APPLICATION_OCTET_STREAM_VALUE)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(schema = @Schema)
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Нет звонка с таким ID!",
                    content = @Content(schema = @Schema)
            ),
            @ApiResponse(
                    responseCode = "418",
                    description = "Нет клиники с таким названием!",
                    content = @Content(schema = @Schema)
            )
    })
    @Operation(summary = "Скачать аудио-запись звонка по его ID")
    @CrossOrigin(exposedHeaders = HttpHeaders.CONTENT_DISPOSITION)
    public ResponseEntity<StreamingResponseBody> downloadAudio(
            @Schema(description = "Название клиники в виде ENUM")
            @PathVariable String clinic,

            @Parameter(description = "ID звонка", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID callId) {

        Clinic clinicEnum = callsService.parse(clinic);

        log.debug("Downloading audio record by id: {}", callId);
        CallAudioResponse callAudioResponse = callsService.downloadAudio(callId, clinicEnum);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, STR."attachment; filename=\{callAudioResponse.getFileName()}")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(callAudioResponse.getMp3().length)
                .body(outputStream -> outputStream.write(callAudioResponse.getMp3()));
    }

    @PutMapping(value = "/edit/comment/{callId}", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Слишком длинный комментарий!"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Нет звонка с таким ID!"
            ),
            @ApiResponse(
                    responseCode = "418",
                    description = "Нет клиники с таким названием!"
            )
    })
    @Operation(summary = "Добавить комментарий к звонку по его ID")
    public void addCommentToCall(
            @Schema(description = "Название клиники в виде ENUM")
            @PathVariable String clinic,

            @Parameter(description = "ID звонка", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID callId,

            @RequestBody
            @Size(message = "comment too long!", max = 5000)
            @Schema(description = "Комментарий пользователя к звонку", example = "Треееш клиент. в чс его")
            String comment) {

        Clinic clinicEnum = callsService.parse(clinic);

        log.debug("Saving user comment for call by id: {}", callId);
        callsService.saveComment(callId, clinicEnum, comment);
    }
}
