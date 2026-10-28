package rdhxb.crbrservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rdhxb.crbrservice.dto.CrbrApiResponse.StatusCrbr;

import java.time.Instant;
import java.util.UUID;

// naglowek zapytania po KRS, krawedzie grafu w CrbrPowiazanieModel (po requestId)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "crbr_data")
public class CrbrModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID requestId;

    @Column(nullable = false, length = 10)
    private String krsNumber;

    @Enumerated(EnumType.STRING)
    private StatusCrbr status;
    private String identyfikatorWniosku;
    private Instant fetchedAt;
}
