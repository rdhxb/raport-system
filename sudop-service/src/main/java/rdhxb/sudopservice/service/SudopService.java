package rdhxb.sudopservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import rdhxb.common.base.RepliesModel;
import rdhxb.common.base.Source;
import rdhxb.common.base.Status;
import rdhxb.common.nip.NIPMessage;
import rdhxb.common.sudop.SudopData;
import rdhxb.sudopservice.client.SudopClient;
import rdhxb.sudopservice.config.RabbitMqConfig;
import rdhxb.sudopservice.dto.SudopApiResponse;
import rdhxb.sudopservice.entity.SudopModel;
import rdhxb.sudopservice.repository.SudopRepository;
import rdhxb.sudopservice.sudopMapper.SudopMapper;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class SudopService {

    private final SudopClient sudopClient;
    private final SudopRepository sudopRepository;
    private final SudopMapper mapper;

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    @SendTo(RabbitMqConfig.REPLIES)
    public RepliesModel receiveNipNumber(NIPMessage nipMessage){

        RepliesModel repliesModel = new RepliesModel();
        repliesModel.setSource(Source.SUDOP);
        repliesModel.setStatus(Status.PENDING);
        repliesModel.setRequestId(nipMessage.getRequestid());

        if (nipMessage.getNIP() == null){
            repliesModel.setErrorMessage("No nip in messeg with id : " + nipMessage.getRequestid());
            repliesModel.setStatus(Status.FAILED);
            repliesModel.setPayload(null);
            return repliesModel;
        }else{
            try {
                var data = sudopClient.getData(nipMessage.getNIP());
                if (data.liczbaWynikow() > 0){
                    List<SudopApiResponse.Wynik> wynikList = data.wyniki();
                    List<SudopModel> models = new ArrayList<>();
                    for (SudopApiResponse.Wynik wynik : wynikList){
                        // map API result to entity
                        var sudopModel = mapper.toSudopModel(wynik);
                        sudopModel.setRequestId(nipMessage.getRequestid());

                        models.add(sudopModel);
                    }
                    sudopRepository.saveAll(models);
                    repliesModel.setStatus(Status.SUCCESS);
                    List<SudopData> sudopDataList = new ArrayList<>();
                    models.stream().forEach(model -> sudopDataList.add(mapper.toSudopData(model)));
                    repliesModel.setPayload(sudopDataList);
                    return repliesModel;

                }else {
                    repliesModel.setStatus(Status.NOT_FOUND);
                    repliesModel.setPayload(null);
                    return repliesModel;
                }


            } catch (Exception e) {
                repliesModel.setErrorMessage(e.getMessage() + " \n API ERROR for nipMessage id : " + nipMessage.getRequestid());
                repliesModel.setPayload(null);
                repliesModel.setStatus(Status.FAILED);
                return repliesModel;
            }
        }




    }
}
