package rdhxb.raportservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import rdhxb.raportservice.model.GeneratedFrom;
import rdhxb.raportservice.model.RaportStatus;


import java.time.Instant;
import java.util.UUID;

@Table(name = "raport")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Raport {
    @Id
    private UUID requestId;

    @Column(nullable = true, length = 10)
    private String krsNumber;

    @Column(nullable = true, length = 10)
    private String nipNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RaportStatus status;

    @CreationTimestamp
    private Instant createdAt;
    private Instant completedAt;

    @Enumerated(EnumType.STRING)
    private GeneratedFrom generatedFrom;

}
