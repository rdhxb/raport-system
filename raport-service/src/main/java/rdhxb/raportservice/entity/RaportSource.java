package rdhxb.raportservice.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import rdhxb.common.base.Source;
import rdhxb.raportservice.model.SourceStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "raport_source",
        uniqueConstraints = @UniqueConstraint(columnNames = {"raport_id", "source"}))
@Getter
@Setter
@NoArgsConstructor
public class RaportSource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raport_id", nullable = false)
    private Raport raport;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Source source;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SourceStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    @Column(columnDefinition = "text")
    private String errorMessage;

    private Instant receivedAt;
}
