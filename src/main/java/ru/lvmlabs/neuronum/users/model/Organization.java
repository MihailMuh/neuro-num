package ru.lvmlabs.neuronum.users.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.lvmlabs.neuronum.users.enums.Clinic;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "organization")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Модель организации")
public class Organization {
    @Id
    @Column(name = "id")
    @GeneratedValue
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "ID организации в бд")
    private UUID id;

    @NotBlank(message = "name must be non-blank")
    @Column(name = "name", nullable = false)
    @Schema(description = "Название организации", example = "ООО ПАРИКМАХЕРСКАЯ ЕЛЕНА")
    private String name;

    @Column(name = "name_enum")
    @Enumerated(EnumType.STRING)
    @Schema(description = "Название организации в виде ENUM")
    private Clinic nameEnum;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "organization_id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Список аккаунтов для парсинга селениумом")
    private List<CrmTelephonyAccount> crmTelephonyAccounts;

    @NotNull(message = "owner must be non-null")
    @Column(name = "user_id", nullable = false)
    @Schema(description = "ID владельца организации")
    private UUID owner;
}
