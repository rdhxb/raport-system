package rdhxb.common.nip;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record NipData(
//        --- dane podmiotu ---
        String name,
        String nip,
        String statusVat,
        String regon,
        String pesel,
        String krs,

//        --- adresy ---
        String residenceAddress,
        String workingAddress,

//        --- daty rejestracji / wykreslenia ---
        LocalDate registrationLegalDate,
        LocalDate registrationDenialDate,
        String registrationDenialBasis,
        LocalDate restorationDate,
        String restorationBasis,
        LocalDate removalDate,
        String removalBasis,
        LocalDate exemptionSmeDate,

//        --- rachunki ---
        List<String> accountNumbers,
        Boolean hasVirtualAccounts,

//        --- metadane zapytania WL ---
        String wlRequestId,
        LocalDateTime requestDateTime
) {}
