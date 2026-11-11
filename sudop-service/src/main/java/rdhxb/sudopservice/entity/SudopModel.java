package rdhxb.sudopservice.entity;

import jakarta.persistence.*;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "sudop_data")
public class SudopModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID requestId;

    private LocalDate dzienUdzieleniaPomocy;
    private String nipUdzielajacegoPomocy;
    private String nazwaUdzielajacegoPomocy;
    private String srodekPomocowyNazwa;
    private String przeznaczeniePomocyKod;
    private String przeznaczeniePomocyNazwa;
    private String formaPomocyKod;
    private String formaPomocyNazwa;
    private BigDecimal wartoscNominalnaPln;
    private BigDecimal wartoscBruttoPln;
    private BigDecimal wartoscBruttoEur;
}
