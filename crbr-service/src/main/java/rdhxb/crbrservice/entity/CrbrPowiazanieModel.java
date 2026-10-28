package rdhxb.crbrservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "crbr_powiazanie",
        uniqueConstraints = @UniqueConstraint(columnNames = {"requestId", "pesel", "krs"}))
public class CrbrPowiazanieModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID requestId;

    @Column(nullable = false, length = 11)
    private String pesel;
    private String imie;
    private String nazwisko;

    @Column(nullable = false, length = 10)
    private String krs;
    private String nazwa;
    @Column(length = 10)
    private String nip;

    private String rodzajUprawnien;
    private BigDecimal ilosc;
    private String jednostkaMiary;

    private Boolean zrodlowa;
}
