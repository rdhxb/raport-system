package rdhxb.whitelistservice.wlMapper;

import org.springframework.stereotype.Component;
import rdhxb.common.nip.WlData;
import rdhxb.whitelistservice.entity.WlModel;
import rdhxb.whitelistservice.model.WlApiResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class WlMapper {

    private static final DateTimeFormatter WL_DATE_TIME = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public WlModel toWlModel(WlApiResponse apiResponse, UUID requestId){

        var result = apiResponse.result();
        var subject = result.subject();


        WlModel wlModel = new WlModel();

        wlModel.setRequestId(requestId);
        wlModel.setFetchedAt(Instant.now());

//        --- metadane zapytania WL ---
        wlModel.setWlRequestId(result.requestId());
        wlModel.setRequestDateTime(parseDateTime(result.requestDateTime()));

        if (subject == null){
            return wlModel;
        }

//        --- dane podmiotu ---
        wlModel.setName(subject.name());
        wlModel.setNip(subject.nip());
        wlModel.setStatusVat(subject.statusVat());
        wlModel.setRegon(subject.regon());
        wlModel.setPesel(subject.pesel());
        wlModel.setKrs(subject.krs());

//        --- adresy ---
        wlModel.setResidenceAddress(subject.residenceAddress());
        wlModel.setWorkingAddress(subject.workingAddress());

//        --- daty rejestracji / wykreslenia ---
        wlModel.setRegistrationLegalDate(parseDate(subject.registrationLegalDate()));
        wlModel.setRegistrationDenialDate(parseDate(subject.registrationDenialDate()));
        wlModel.setRegistrationDenialBasis(subject.registrationDenialBasis());
        wlModel.setRestorationDate(parseDate(subject.restorationDate()));
        wlModel.setRestorationBasis(subject.restorationBasis());
        wlModel.setRemovalDate(parseDate(subject.removalDate()));
        wlModel.setRemovalBasis(subject.removalBasis());
        wlModel.setExemptionSmeDate(parseDate(subject.exemptionSmeDate()));

//        --- rachunki ---
        if (subject.accountNumbers() != null){
            wlModel.setAccountNumbers(new ArrayList<>(subject.accountNumbers()));
        }
        wlModel.setHasVirtualAccounts(subject.hasVirtualAccounts());

        return wlModel;
    }

    public WlData toWlData(WlModel wlModel){

        WlData wlData = new WlData(
                wlModel.getName(),
                wlModel.getNip(),
                wlModel.getStatusVat(),
                wlModel.getRegon(),
                wlModel.getPesel(),
                wlModel.getKrs(),

                wlModel.getResidenceAddress(),
                wlModel.getWorkingAddress(),

                wlModel.getRegistrationLegalDate(),
                wlModel.getRegistrationDenialDate(),
                wlModel.getRegistrationDenialBasis(),
                wlModel.getRestorationDate(),
                wlModel.getRestorationBasis(),
                wlModel.getRemovalDate(),
                wlModel.getRemovalBasis(),
                wlModel.getExemptionSmeDate(),

                wlModel.getAccountNumbers() != null ? List.copyOf(wlModel.getAccountNumbers()) : List.of(),
                wlModel.getHasVirtualAccounts(),

                wlModel.getWlRequestId(),
                wlModel.getRequestDateTime()
        );



        return wlData;
    }

    private LocalDate parseDate(String date){
        if (date == null || date.isBlank()){
            return null;
        }
        return LocalDate.parse(date);
    }

    private LocalDateTime parseDateTime(String dateTime){
        if (dateTime == null || dateTime.isBlank()){
            return null;
        }
        return LocalDateTime.parse(dateTime, WL_DATE_TIME);
    }
}
