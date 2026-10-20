package rdhxb.whitelistservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wl_data")
public class WlModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID requestId;

    // --- dane podmiotu ---
    private String name;
    @Column(nullable = false, length = 10)
    private String nip;
    private String statusVat;
    private String regon;
    private String pesel;
    private String krs;

    // --- adresy ---
    private String residenceAddress;
    private String workingAddress;

    // --- daty rejestracji / wykreslenia ---
    private LocalDate registrationLegalDate;
    private LocalDate registrationDenialDate;
    @Column(columnDefinition = "text")
    private String registrationDenialBasis;
    private LocalDate restorationDate;
    @Column(columnDefinition = "text")
    private String restorationBasis;
    private LocalDate removalDate;
    @Column(columnDefinition = "text")
    private String removalBasis;
    private LocalDate exemptionSmeDate;

    // --- rachunki ---
    @ElementCollection
    @CollectionTable(name = "wl_account_numbers", joinColumns = @JoinColumn(name = "wl_data_id"))
    @Column(name = "account_number", length = 26)
    private List<String> accountNumbers = new ArrayList<>();
    private Boolean hasVirtualAccounts;

    // --- metadane zapytania WL ---
    private String wlRequestId;
    private LocalDateTime requestDateTime;
    private Instant fetchedAt;
}
