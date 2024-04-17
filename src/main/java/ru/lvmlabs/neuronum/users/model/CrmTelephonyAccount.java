package ru.lvmlabs.neuronum.users.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import ru.lvmlabs.neuronum.users.enums.AnalysisStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "crm_telephony_accounts")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Schema(description = "Модель аккаунта для парсинга селениумом")
public class CrmTelephonyAccount {
    @Id
    @GeneratedValue
    @Column(name = "id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @NotBlank(message = "crmName must be non-blank")
    @Column(name = "crm_name", nullable = false)
    @Schema(description = "Название используемой CRM системы", example = "UISCOM")
    private String crmName;

    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(description = "Пароль для входа в CRM систему", example = "32t4grwe4yhet")
    private String password;

    @Column(name = "login")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(description = "Логин для входа в CRM систему", example = "А.У.Е")
    private String login;

    @Column(name = "newest_date", nullable = false)
    @Temporal(TemporalType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy", timezone = "Asia/Yekaterinburg")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(
            description = "Дата, до какой выбирать звонки. Должно быть больше, чем oldestDate",
            pattern = "dd.MM.yyyy", example = "29.01.2024",
            implementation = String.class,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Date newestDate = Date.from(Instant.now());

    @Column(name = "oldest_date", nullable = false)
    @Temporal(TemporalType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy", timezone = "Asia/Yekaterinburg")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(
            description = "Дата, с какой выбирать звонки. Должно быть меньше, чем newestDate",
            pattern = "dd.MM.yyyy", example = "01.01.2024",
            implementation = String.class,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Date oldestDate = Date.from(Instant.now().minus(31, ChronoUnit.DAYS));

    @Min(value = 1, message = "analysisPeriod is invalid")
    @Max(value = 60 * 24 * 7 * 31, message = "analysisPeriod is invalid")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "analysis_period", nullable = false)
    @Schema(description = "Период между парсингами в минутах", example = "5")
    private int analysisPeriod = 15;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Текущий статус системы парсинга")
    private AnalysisStatus analysisStatus = AnalysisStatus.NOT_INTEGRATED;
}
