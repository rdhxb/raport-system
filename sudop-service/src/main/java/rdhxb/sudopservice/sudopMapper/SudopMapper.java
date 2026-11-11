package rdhxb.sudopservice.sudopMapper;

import org.springframework.stereotype.Component;
import rdhxb.sudopservice.dto.SudopApiResponse;
import rdhxb.sudopservice.entity.SudopModel;

@Component
public class SudopMapper {

    public SudopModel toSudopModel(SudopApiResponse.Wynik wynik){
        SudopModel sudopModel = new SudopModel();

        sudopModel.setDzienUdzieleniaPomocy(wynik.dzienUdzieleniaPomocy());
        sudopModel.setNipUdzielajacegoPomocy(wynik.nipUdzielajacegoPomocy());
        sudopModel.setNazwaUdzielajacegoPomocy(wynik.nazwaUdzielajacegoPomocy());
        sudopModel.setSrodekPomocowyNazwa(wynik.srodekPomocowyNazwa());
        sudopModel.setPrzeznaczeniePomocyKod(wynik.przeznaczeniePomocyKod());
        sudopModel.setPrzeznaczeniePomocyNazwa(wynik.przeznaczeniePomocyNazwa());
        sudopModel.setFormaPomocyKod(wynik.formaPomocyKod());
        sudopModel.setFormaPomocyNazwa(wynik.formaPomocyNazwa());
        sudopModel.setWartoscNominalnaPln(wynik.wartoscNominalnaPln());
        sudopModel.setWartoscBruttoPln(wynik.wartoscBruttoPln());
        sudopModel.setWartoscBruttoEur(wynik.wartoscBruttoEur());

        return sudopModel;
    }
}
