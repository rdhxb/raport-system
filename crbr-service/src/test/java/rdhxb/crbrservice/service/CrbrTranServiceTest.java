package rdhxb.crbrservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rdhxb.crbrservice.entity.CrbrModel;
import rdhxb.crbrservice.entity.CrbrPowiazanieModel;
import rdhxb.crbrservice.repository.CrbrModelRepository;
import rdhxb.crbrservice.repository.CrbrPowiazanieModelRepository;

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

        // given
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

    @Test
    public void shouldReplaceOldDataWhenKrsExistsInDb(){
        //  given
        String krsNum = "1231231231";
        UUID oldRequestId = UUID.randomUUID();
        UUID newRequestId = UUID.randomUUID();

        UUID oldId = UUID.randomUUID();

        CrbrModel modelInDb = new CrbrModel();
        modelInDb.setId(oldId);
        modelInDb.setRequestId(oldRequestId);
        modelInDb.setKrsNumber(krsNum);

        CrbrModel crbrModel = new CrbrModel();
        crbrModel.setRequestId(newRequestId);
        crbrModel.setId(null);
        crbrModel.setKrsNumber(krsNum);

        when(crbrModelRepository.findByKrsNumber(krsNum)).thenReturn(Optional.of(modelInDb));

        List<CrbrPowiazanieModel> powiazania = List.of(new CrbrPowiazanieModel());

        //  when
        service.saveData(crbrModel, powiazania);

        //  then
        verify(powiazanieModelRepository, never()).deleteAllByRequestId(newRequestId);

        InOrder inOrder = inOrder(powiazanieModelRepository, crbrModelRepository);
        inOrder.verify(powiazanieModelRepository).deleteAllByRequestId(oldRequestId);
        inOrder.verify(powiazanieModelRepository).flush();
        inOrder.verify(crbrModelRepository).save(crbrModel);
        inOrder.verify(powiazanieModelRepository).saveAll(powiazania);

        assertThat(crbrModel.getId()).isEqualTo(oldId);
    }


}
