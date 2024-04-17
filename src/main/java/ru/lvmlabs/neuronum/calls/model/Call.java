package ru.lvmlabs.neuronum.calls.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.lvmlabs.neuronum.users.enums.Clinic;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(
        name = "calls",
        indexes = @Index(name = "clinic_date_time_index", columnList = "clinic, date DESC, time DESC")
)
@ToString(exclude = "mp3")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Call {
    @Id
    @Column(name = "id")
    @GeneratedValue
    protected UUID id;

    @NotNull(message = "clinic must be non-null")
    @Column(name = "clinic", nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Название клиники в виде ENUM")
    private Clinic clinic;

    @NotBlank(message = "text must be non-blank")
    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    protected String text;

    @NotNull(message = "date must be non-null")
    @Column(name = "date", nullable = false)
    @Temporal(TemporalType.DATE)
    protected Date date;

    @NotNull(message = "time must be non-null")
    @Column(name = "time", nullable = false)
    @Temporal(TemporalType.TIME)
    protected Date time;

    @NotBlank(message = "phoneNumber must be non-blank")
    @Column(name = "phone_number", nullable = false, length = 20)
    protected String phoneNumber;

    @Column(name = "virtual_number", length = 20)
    protected String virtualNumber;

    @Column(name = "is_incoming")
    protected boolean isIncoming;

    // analysis

    @Column(name = "analysis", columnDefinition = "TEXT")
    protected String analysis;

    @Column(name = "complaint", length = 24)
    protected String complaint;

    @Column(name = "record", length = 20)
    protected String record;

    @Column(name = "doctor", length = 30)
    protected String doctor;

    @Column(name = "why_no", columnDefinition = "TEXT")
    protected String whyNo;

    @Column(name = "client_name")
    protected String clientName;

    @Column(name = "administrator_name")
    protected String administratorName;

    @Column(name = "was_before", length = 3)
    protected String wasBefore;

    @Column(name = "late_marker", columnDefinition = "TEXT")
    protected String lateMarker;

    @Column(name = "admin_quality", columnDefinition = "TEXT")
    protected String adminQuality;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "mp3")
    protected byte[] mp3;

    @Column(name = "file_name", length = 60)
    protected String fileName;

    @Column(name = "comment", length = 5000)
    protected String comment;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Call call = (Call) o;

        if (isIncoming() != call.isIncoming()) return false;
        if (getClinic() != call.getClinic()) return false;
        if (!getDate().equals(call.getDate())) return false;
        if (!getTime().equals(call.getTime())) return false;
        if (!getPhoneNumber().equals(call.getPhoneNumber())) return false;
        return getVirtualNumber() != null ? getVirtualNumber().equals(call.getVirtualNumber()) : call.getVirtualNumber() == null;
    }

    @Override
    public int hashCode() {
        int result = getClinic().hashCode();
        result = 31 * result + getDate().hashCode();
        result = 31 * result + getTime().hashCode();
        result = 31 * result + getPhoneNumber().hashCode();
        result = 31 * result + (getVirtualNumber() != null ? getVirtualNumber().hashCode() : 0);
        result = 31 * result + (isIncoming() ? 1 : 0);
        return result;
    }
}
