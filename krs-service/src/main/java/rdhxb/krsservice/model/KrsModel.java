package rdhxb.krsservice.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rdhxb.krsservice.dto.KrsApiResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "krs_data")
public class KrsModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID requestId;

//   --- NaglowekA ---
    @Column(nullable = false, length = 10, unique = true)
    private String krsNumber;

    private String rejestr;
    private LocalDate stanZDnia;
    private LocalDate dataRejestracji;
    private Integer numerOstatniegoWpisu;
    private LocalDate dataOstatniegoWpisu;
    private Instant fetchedAt;

    // --- dane podmiotu ---
    private String nazwa;
    private String formaPrawna;
    private String nip;
    private String regon;
    private Boolean statusOpp;

    // --- adres ---
    private String ulica;
    private String nrDomu;
    private String nrLokalu;
    private String miejscowosc;
    private String kodPocztowy;
    private String poczta;
    private String kraj;
//    --- siedziba ---
    private String krajSiedziby;
    private String wojewodztwo;
    private String powiat;
    private String gmina;
    private String miejscowoscSiedziby;

    private String email;
    private String www;

    // --- kapitał ---
    private BigDecimal kapitalZakladowy;
    private String kapitalWaluta;

    // --- reprezentacja ---
    private String organReprezentacji;
    @Column(columnDefinition = "text")
    private String sposobReprezentacji;
}
