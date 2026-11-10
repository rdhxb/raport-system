package rdhxb.ceidgservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import rdhxb.common.ceidg.CeidgHistoriaData;
import rdhxb.common.ceidg.CeidgPkdData;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ceidg_data")
public class CeidgModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID requestId;
    private Instant fetchedAt;

    @Column(nullable = false, length = 10)
    private String nip;

    private Boolean maAktualnaFirme;

    private String ceidgId;
    private String nazwa;
    private String status;
    private Integer numerStatusu;

    private String imie;
    private String nazwisko;
    private String regon;
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> obywatelstwa;

    private LocalDate dataRozpoczecia;
    private LocalDate dataZawieszenia;
    private LocalDate dataWznowienia;
    private LocalDate dataZakonczenia;
    private LocalDate dataWykreslenia;

    private String ulica;
    private String budynek;
    private String lokal;
    private String miasto;
    private String kodPocztowy;
    private String wojewodztwo;
    private String powiat;
    private String gmina;
    private String kraj;

    private String adresKorespondencyjny;

    private String telefon;
    private String email;
    private String www;

    private String wspolnoscMajatkowa;

    private String rokPkd;
    @JdbcTypeCode(SqlTypes.JSON)
    private CeidgPkdData pkdGlowny;
    @JdbcTypeCode(SqlTypes.JSON)
    private List<CeidgPkdData> pkd;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<CeidgHistoriaData> historia;
}
