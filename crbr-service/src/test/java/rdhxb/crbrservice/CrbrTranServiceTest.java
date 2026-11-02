package rdhxb.crbrservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rdhxb.crbrservice.entity.CrbrModel;
import rdhxb.crbrservice.entity.CrbrPowiazanieModel;
import rdhxb.crbrservice.repository.CrbrModelRepository;
import rdhxb.crbrservice.repository.CrbrPowiazanieModelRepository;
import rdhxb.crbrservice.service.CrbrTranService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CrbrTranServiceTest {
    @Mock
    CrbrModelRepository crbrModelRepository;
    @Mock
    CrbrPowiazanieModelRepository powiazanieModelRepository;
    @InjectMocks
    CrbrTranService service;

    @Test
    public void shouldSaveWithoutDeletingWhenKrsNotInDb(){

        String krsNum = "1231231231";
        UUID requestId = UUID.randomUUID();

        CrbrModel crbrModel = new CrbrModel();
        crbrModel.setKrsNumber(krsNum);
        crbrModel.setRequestId(requestId);

        List<CrbrPowiazanieModel> powiazania = List.of(new CrbrPowiazanieModel());

        when(crbrModelRepository.findByKrsNumber(krsNum)).thenReturn(Optional.empty());

        // when
        service.saveData(crbrModel, powiazania);

        // then
        verify(powiazanieModelRepository, never()).deleteAllByRequestId(any());
        verify(powiazanieModelRepository, never()).flush();
        verify(crbrModelRepository).save(crbrModel);
        verify(powiazanieModelRepository).saveAll(powiazania);
        assertThat(crbrModel.getId()).isNull();
    }


}
