package rdhxb.financeservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import rdhxb.financeservice.dto.SprawozdanieDto.Sprawozdanie;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "sprawozdanie", uniqueConstraints = @UniqueConstraint(columnNames = {"krs", "okres_do"}))
@RequiredArgsConstructor
public class SprawozdanieModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(length = 10)
    private String krs;

    @Column(length = 10)
    private String nip;

    private Instant uploadedAt;

    private String nazwaPliku;

    private LocalDate okresOd;
    private LocalDate okresDo;

    @JdbcTypeCode(SqlTypes.JSON)
    private Sprawozdanie sprawozdanie;

}
