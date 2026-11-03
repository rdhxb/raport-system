package rdhxb.crbrservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.gov.mf.uslugibiznesowe.uslugiesb.ap.apiprzegladowecrbr._2022._12._01.PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp;
import rdhxb.common.krs.KRSNumberModel;
import rdhxb.crbrservice.client.CrbrClient;
import rdhxb.crbrservice.crbrMapper.Mapper;
import rdhxb.crbrservice.dto.CrbrApiResponse;
import rdhxb.crbrservice.dto.CrbrApiResponse.Beneficjent;
import rdhxb.crbrservice.dto.CrbrApiResponse.Spolka;
import rdhxb.crbrservice.entity.CrbrModel;
import rdhxb.crbrservice.entity.CrbrPowiazanieModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    @Captor
    ArgumentCaptor<Collection<CrbrPowiazanieModel>> powiazaniaCaptor;


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

    @Test
    public void shouldSaveCrbrModelAndPowiazaniaForKrs(){

        // given
        UUID requestId = UUID.randomUUID();
        String krs = "0000123456";
        String krsInnejSpolki = "0000999999";
        String pesel = "90010112345";

        KRSNumberModel message = new KRSNumberModel(requestId, krs);

        PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp krsSoap = new PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp();
        PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp peselSoap = new PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp();

        Spolka spolkaZKrs = spolka(krs, pesel);
        Spolka spolkaZPesel = spolka(krsInnejSpolki);
        CrbrApiResponse krsData = response(spolkaZKrs);
        CrbrApiResponse peselData = response(spolkaZPesel);

        CrbrModel crbrModel = new CrbrModel();
        CrbrPowiazanieModel powiazanieZKrs = powiazanie(pesel, krs);
        CrbrPowiazanieModel powiazanieZPesel = powiazanie(pesel, krsInnejSpolki);

        when(client.getDataFromKrs(krs)).thenReturn(krsSoap);
        when(client.getDataFromPesel(pesel)).thenReturn(peselSoap);
        when(mapper.toCrbrResponse(krsSoap)).thenReturn(krsData);
        when(mapper.toCrbrResponse(peselSoap)).thenReturn(peselData);
        when(mapper.toCrbrModel(krsData, requestId, krs)).thenReturn(crbrModel);
        when(mapper.toPowiazania(spolkaZKrs, requestId, krs)).thenReturn(Map.of(pesel + ":" + krs, powiazanieZKrs));
        when(mapper.toPowiazania(spolkaZPesel, requestId, krs)).thenReturn(Map.of(pesel + ":" + krsInnejSpolki, powiazanieZPesel));

        // when
        crbrService.receiverKrsNumber(message);

        // then
        verify(client).getDataFromKrs(krs);
        verify(client).getDataFromPesel(pesel);

        verify(crbrTranService).saveData(eq(crbrModel), powiazaniaCaptor.capture());
        assertThat(powiazaniaCaptor.getValue())
                .containsExactlyInAnyOrder(powiazanieZKrs, powiazanieZPesel);
    }


    private static CrbrApiResponse response(Spolka... spolki) {
        return new CrbrApiResponse(null, null, null, null, List.of(spolki));
    }

    private static Spolka spolka(String krs, String... pesele) {
        List<Beneficjent> beneficjenci = Arrays.stream(pesele)
                .map(CrbrServiceTest::beneficjent)
                .toList();
        return new Spolka(null, null, krs, null, null, null, null, null, null, null,
                beneficjenci, List.of(), List.of());
    }

    private static Beneficjent beneficjent(String pesel) {
        return new Beneficjent(null, null, null, pesel, null, List.of(), null, List.of(), null, null);
    }

    private static CrbrPowiazanieModel powiazanie(String pesel, String krs) {
        CrbrPowiazanieModel powiazanie = new CrbrPowiazanieModel();
        powiazanie.setPesel(pesel);
        powiazanie.setKrs(krs);
        return powiazanie;
    }

    @Test
    public void shouldNotSaveWhenKrsLookupFails(){

        // given
        String krs = "1231231231";
        KRSNumberModel message = new KRSNumberModel(UUID.randomUUID(), krs);

        when(client.getDataFromKrs(krs)).thenThrow(new RuntimeException("CRBR niedostępne"));

        // when + then
        assertDoesNotThrow(() -> crbrService.receiverKrsNumber(message));

        // then
        verify(client, never()).getDataFromPesel(any());
        verifyNoInteractions(mapper, crbrTranService);
    }

}
