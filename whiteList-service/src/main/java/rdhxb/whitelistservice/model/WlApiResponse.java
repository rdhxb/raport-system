package rdhxb.whitelistservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record WlApiResponse(Result result) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            Subject subject,
            String requestDateTime,
            String requestId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Subject(
            String name,
            String nip,
            String statusVat,
            String regon,
            String pesel,
            String krs,
            String residenceAddress,
            String workingAddress,
            List<Person> representatives,
            List<Person> authorizedClerks,
            List<Person> partners,
            String registrationLegalDate,
            String registrationDenialDate,
            String registrationDenialBasis,
            String restorationDate,
            String restorationBasis,
            String removalDate,
            String exemptionSmeDate,
            String removalBasis,
            List<String> accountNumbers,
            Boolean hasVirtualAccounts
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Person(
            String companyName,
            String firstName,
            String lastName,
            String pesel,
            String nip
    ) {}
}
