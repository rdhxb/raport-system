package rdhxb.crbrservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rdhxb.common.krs.KRSNumberModel;
import rdhxb.crbrservice.client.CrbrClient;
import rdhxb.crbrservice.crbrMapper.Mapper;


import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CrbrServiceTest {

    @Mock
    CrbrClient client;
    @Mock
    Mapper mapper;
    @Mock
    CrbrTranService crbrTranService;

    @InjectMocks
    CrbrService crbrService;


    @Test
    public void shouldDoNothingWhenKrsNumberIsNull(){
        // given
        KRSNumberModel krsNumberModel = new KRSNumberModel();
        krsNumberModel.setKrsNum(null);

        // when
        crbrService.receiverKrsNumber(krsNumberModel);

        // then
        verifyNoInteractions(client, mapper, crbrTranService);

    }
//  1. shouldDoNothingWhenKrsNumberIsNull
//     given: KRSNumberModel z krsNum = null
//     then:  verifyNoInteractions(client, mapper, crbrTranService)
//
//  2. shouldSaveCrbrModelAndPowiazaniaForKrs  (happy path)
//     given: odpowiedź KRS z 1 spółką i 1 beneficjentem z poprawnym PESEL (11 znaków),
//            odpowiedź PESEL z 1 inną spółką
//     then:  - client.getDataFromKrs(krs) i client.getDataFromPesel(pesel) wywołane po razie
//            - mapper.toCrbrModel(data, id, krs) dostał id i krs z wiadomości
//            - saveData(crbrModel, ...) -> w kolekcji powiązania z OBU spółek (KRS + PESEL)
//
//  3. shouldSkipPeselLookupWhenPeselInvalid   (dobry kandydat na @ParameterizedTest)
//     given: beneficjent z pesel = null / "123" / "123456789012"
//     then:  verify(client, never()).getDataFromPesel(any())
//            saveData nadal wywołane
//
//  4. shouldCallPeselLookupOnceWhenSamePeselInManySpolki
//     given: 2 spółki z tym samym beneficjentem (ten sam PESEL)
//     then:  verify(client, times(1)).getDataFromPesel(pesel)    <- Set<String> pesels
//
//  5. shouldDeduplicatePowiazaniaBySamePeselAndKrs
//     given: toPowiazania dla spółki z KRS i dla spółki z PESEL zwracają powiązanie o tym samym pesel + krs
//     then:  captor -> do saveData trafia 1 powiązanie, nie 2
//            (bonus: zostaje PIERWSZE, bo putIfAbsent -> sprawdź po referencji / isSameAs)
//
//  6. shouldNotDeduplicateWhenDifferentKrs
//     given: ten sam pesel, różne krs
//     then:  captor -> 2 powiązania   (pilnuje, że klucz to pesel + krs, a nie sam pesel)
//
//  7. shouldStillSaveWhenPeselLookupFails
//     given: client.getDataFromPesel(...) rzuca RuntimeException
//     then:  saveData wywołane, w kolekcji tylko powiązania ze spółki z KRS
//            metoda nie rzuca wyjątku (assertDoesNotThrow albo po prostu brak wyjątku)
//
//  8. shouldContinueWithOtherPeselsWhenOneFails
//     given: 2 różne PESEL-e, dla pierwszego client rzuca wyjątek, drugi zwraca dane
//     then:  getDataFromPesel wywołane 2 razy, powiązania z drugiego PESEL są w saveData
//
//  9. shouldNotSaveWhenKrsLookupFails
//     given: client.getDataFromKrs(...) rzuca RuntimeException
//     then:  verifyNoInteractions(crbrTranService), verify(client, never()).getDataFromPesel(any())
//            wyjątek nie wylatuje z metody (jest złapany)
//
// 10. shouldNotPropagateExceptionWhenSaveFails
//     given: doThrow(...).when(crbrTranService).saveData(any(), any())
//     then:  metoda nie rzuca wyjątku   <- zastanów się, czy to w ogóle pożądane zachowanie dla @RabbitListener
//                                         (wiadomość zostanie "zjedzona", nie pójdzie retry/DLQ)
//
// 11. shouldSaveOnlyModelWhenNoSpolki
//     given: odpowiedź KRS z pustą listą spolki
//     then:  getDataFromPesel nigdy, saveData(crbrModel, pusta kolekcja)
//
//  Kolejność pisania: 1 -> 2 -> 9 -> 3 -> 7 -> 4 -> 5 -> reszta
//  Uwaga: MockitoExtension jest strict -> nie stubuj rzeczy, których dany test nie używa (UnnecessaryStubbingException).
}
