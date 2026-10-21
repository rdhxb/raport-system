package rdhxb.whitelistservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import rdhxb.common.base.RepliesModel;
import rdhxb.common.base.Source;
import rdhxb.common.base.Status;
import rdhxb.common.nip.NIPMessage;
import rdhxb.whitelistservice.client.WlClient;
import rdhxb.whitelistservice.entity.WlModel;
import rdhxb.whitelistservice.repository.WlRepository;
import rdhxb.whitelistservice.wlMapper.WlMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServiceTest {

    @Mock
    WlClient wlClient;
    @Mock
    WlRepository wlRepository;
    @Mock
    WlMapper wlMapper;

    @InjectMocks
    WlService wlService;

    @Test
    public void saveNewDataToDb(){
        UUID id = UUID.randomUUID();
        String nipNum = "000000000";
        NIPMessage nipMessage = new NIPMessage();
        nipMessage.setNIP(nipNum);
        nipMessage.setRequestid(id);

        WlModel wlModel = new WlModel();
        wlModel.setNip(nipNum);
        wlModel.setRequestId(id);

        when(wlMapper.toWlModel(any(), any())).thenReturn(wlModel);

        RepliesModel replies = wlService.receiveNipNumber(nipMessage);

        verify(wlRepository).save(wlModel);

        assertEquals(Status.SUCCESS, replies.getStatus());
        assertEquals(Source.WL, replies.getSource());
        assertEquals(id, replies.getRequestId());

    }

    @Test
    public void refreshDataInDb(){
        UUID id = UUID.randomUUID();
        String nipNum = "000000000";
        NIPMessage nipMessage = new NIPMessage();
        nipMessage.setNIP(nipNum);
        nipMessage.setRequestid(id);

        WlModel wlModel = new WlModel();
        wlModel.setNip(nipNum);
        wlModel.setRequestId(id);

        WlModel dataInDb = new WlModel();
        dataInDb.setNip(nipNum);
        dataInDb.setRequestId(UUID.randomUUID());


        when(wlMapper.toWlModel(any(), any())).thenReturn(wlModel);

        when(wlRepository.getWlModelByNip(nipNum)).thenReturn(dataInDb);

        RepliesModel replies = wlService.receiveNipNumber(nipMessage);


        verify(wlRepository).save(wlModel);
        verify(wlRepository).delete(dataInDb);


        assertEquals(Status.SUCCESS, replies.getStatus());
        assertEquals(Source.WL, replies.getSource());
        assertEquals(id, replies.getRequestId());

    }

    @Test
    public void returnNullWhenNipIsNull(){
        NIPMessage nipMessage = new NIPMessage();
        nipMessage.setRequestid(UUID.randomUUID());

        RepliesModel replies = wlService.receiveNipNumber(nipMessage);

        assertNull(replies);
        verifyNoInteractions(wlClient, wlMapper, wlRepository);

    }

    @Test
    public void returnFailedWhenClientThrows() throws Exception {
        UUID id = UUID.randomUUID();
        String nipNum = "000000000";
        NIPMessage nipMessage = new NIPMessage();
        nipMessage.setNIP(nipNum);
        nipMessage.setRequestid(id);

        when(wlClient.getData(nipNum)).thenThrow(new RuntimeException("API error"));

        RepliesModel replies = wlService.receiveNipNumber(nipMessage);

        verify(wlRepository, never()).save(any());

        assertEquals(Status.FAILED, replies.getStatus());
        assertEquals(Source.WL, replies.getSource());
        assertEquals(id, replies.getRequestId());
        assertEquals("API error", replies.getErrorMessage());
        assertNull(replies.getPayload());

    }

    @Test
    public void returnNullWhenMapperReturnsNull(){
        String nipNum = "000000000";
        NIPMessage nipMessage = new NIPMessage();
        nipMessage.setNIP(nipNum);
        nipMessage.setRequestid(UUID.randomUUID());

        when(wlMapper.toWlModel(any(), any())).thenReturn(null);

        RepliesModel replies = wlService.receiveNipNumber(nipMessage);

        assertNull(replies);
        verify(wlRepository, never()).save(any());
        verify(wlRepository, never()).delete(any());

    }

    @Test
    public void refreshDoesNothingWhenOldDataIsNull(){
        WlModel freshData = new WlModel();

        wlService.refreshWlData(null, freshData);

        verify(wlRepository, never()).delete(any());
        verify(wlRepository, never()).save(any());

    }

    @Test
    public void refreshDoesNotSaveWhenDeleteThrows(){
        WlModel oldData = new WlModel();
        WlModel freshData = new WlModel();

        doThrow(new RuntimeException("DB error")).when(wlRepository).delete(oldData);

        assertDoesNotThrow(() -> wlService.refreshWlData(oldData, freshData));

        verify(wlRepository, never()).save(any());

    }
}
