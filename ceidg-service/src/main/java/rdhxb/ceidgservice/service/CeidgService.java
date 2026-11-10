package rdhxb.ceidgservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import rdhxb.ceidgservice.ceidgMapper.CeidgMapper;
import rdhxb.ceidgservice.dto.CeidgApiResponse;
import rdhxb.ceidgservice.entity.CeidgModel;
import rdhxb.ceidgservice.repostory.CeidgRepository;
import rdhxb.common.base.RepliesModel;
import rdhxb.common.base.Source;
import rdhxb.common.base.Status;
import rdhxb.common.nip.NIPMessage;
import rdhxb.ceidgservice.client.CeidgClient;
import rdhxb.ceidgservice.config.RabbitMqConfig;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CeidgService {

    private final CeidgClient ceidgClient;
    private final CeidgMapper mapper;
    private final CeidgRepository ceidgRepository;

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    @SendTo(RabbitMqConfig.REPLIES)
    public RepliesModel receiveNipNumber(NIPMessage nipMessage){

        if (nipMessage.getNIP() == null){
            return prepRepliesModel(nipMessage,null,Status.FAILED, "No NIP number in message, for request id : " + nipMessage.getRequestid());
        }

        try {

            CeidgApiResponse data = ceidgClient.getData(nipMessage.getNIP());

            if (data.firma() == null){
                return prepRepliesModel(nipMessage,null,Status.NOT_FOUND, "CEIDG data not found for nip : " + nipMessage.getNIP());
            }

            CeidgModel ceidgModel = mapper.toCeidgModel(data,nipMessage.getRequestid());
            replaceDataIfExist(ceidgModel);
            return prepRepliesModel(nipMessage, ceidgModel, Status.SUCCESS, "");

        } catch (Exception e) {

            log.error(e.getMessage());
            return prepRepliesModel(nipMessage, null, Status.FAILED, e.getMessage());

        }

    }

    public RepliesModel prepRepliesModel(NIPMessage nipMessage, CeidgModel data, Status status, String errorMessage){

        RepliesModel repliesModel = new RepliesModel();
        repliesModel.setRequestId(nipMessage.getRequestid());
        repliesModel.setSource(Source.CEIDG);
        repliesModel.setStatus(status);
        repliesModel.setPayload(data);
        repliesModel.setErrorMessage(errorMessage);

        return repliesModel;
    }


    public void replaceDataIfExist(CeidgModel newData){
        CeidgModel oldData = ceidgRepository.findByNip(newData.getNip());
        if (oldData != null){
            UUID oldId = oldData.getId();
            newData.setId(oldId);
            ceidgRepository.save(newData);
        }else{
            ceidgRepository.save(newData);
        }

    }
}
