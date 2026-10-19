package rdhxb.krsservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rdhxb.common.base.RepliesModel;
import rdhxb.common.base.Status;
import rdhxb.common.krs.KRSNumberModel;
import rdhxb.krsservice.Client.KrsClient;
import rdhxb.krsservice.krsMapper.KrsMapper;
import rdhxb.krsservice.model.KrsModel;
import rdhxb.krsservice.repository.KrsRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KrsServiceTest {

    // Atrapy (mocki) - zamiast prawdziwego API, bazy i mappera
    @Mock
    KrsClient krsClient;
    @Mock
    KrsRepository krsRepository;
    @Mock
    KrsMapper mapper;

    @InjectMocks
    KrsService krsService;

    @Test
    void shouldSaveNewModelWhenNotInDb() {
        UUID requestId = UUID.fromString("ae9c9327-3793-4f0c-9834-d5209e2b1841");
        KRSNumberModel message = new KRSNumberModel(requestId, "0000028860");

        KrsModel modelFromApi = new KrsModel();
        modelFromApi.setKrsNumber("0000028860");
        modelFromApi.setDataOstatniegoWpisu(LocalDate.of(2026, 7, 21));


        when(mapper.toKrsModel(any(), any())).thenReturn(modelFromApi);

        when(krsRepository.findKrsModelByKrsNumber("0000028860")).thenReturn(null);

        RepliesModel result = krsService.receiveKrsNumber(message);


        verify(krsRepository).save(modelFromApi);

        assertEquals(Status.SUCCESS, result.getStatus());
        assertEquals(requestId, result.getRequestId());
    }

    @Test
    public void shouldUpdateOnlyFatchedAt(){
        UUID requestId = UUID.fromString("ae9c9327-3793-4f0c-9834-d5209e2b1841");
        KRSNumberModel message = new KRSNumberModel(requestId, "1234567890");

        KrsModel krsModel= new KrsModel();
        krsModel.setKrsNumber("1234567890");
        krsModel.setFetchedAt(Instant.parse("2026-10-01T10:00:00Z"));
        krsModel.setDataOstatniegoWpisu(LocalDate.parse("2027-02-01"));

        var dataInDb = new KrsModel();
        dataInDb.setKrsNumber("1234567890");
        dataInDb.setFetchedAt(Instant.parse("2026-09-01T10:00:00Z"));
        dataInDb.setDataOstatniegoWpisu(LocalDate.parse("2027-02-01"));
        dataInDb.setId(UUID.randomUUID());

        when(krsRepository.findKrsModelByKrsNumber("1234567890")).thenReturn(dataInDb);

        when(mapper.toKrsModel(any(), any())).thenReturn(krsModel);

        krsService.receiveKrsNumber(message);

        verify(krsRepository).save(dataInDb);


        assertEquals(dataInDb.getFetchedAt(), krsModel.getFetchedAt());

    }

    @Test
    public void updateDataWhenDifferenDataOstatinegoWpisu(){

        UUID requestId = UUID.randomUUID();
        KRSNumberModel requestMess = new KRSNumberModel(requestId, "1231231231");

        KrsModel krs = new KrsModel();
        krs.setDataOstatniegoWpisu(LocalDate.parse("2026-03-15"));
        krs.setKrsNumber("1231231231");

        var dataInDb = new KrsModel();
        dataInDb.setKrsNumber("1231231231");
        dataInDb.setDataOstatniegoWpisu(LocalDate.parse("2026-03-11"));
        dataInDb.setId(UUID.randomUUID());

        when(mapper.toKrsModel(any(), any())).thenReturn(krs);

        when(krsRepository.findKrsModelByKrsNumber("1231231231")).thenReturn(dataInDb);

        RepliesModel result = krsService.receiveKrsNumber(requestMess);

        verify(krsRepository).save(krs);

        assertEquals(dataInDb.getId(), krs.getId());
        assertEquals(Status.SUCCESS, result.getStatus());
        assertEquals(requestId, result.getRequestId());

    }




}
