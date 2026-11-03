package rdhxb.crbrservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import rdhxb.common.base.RepliesModel;
import rdhxb.common.base.Source;
import rdhxb.common.base.Status;
import rdhxb.common.crbr.CrbrData;
import rdhxb.common.krs.KRSNumberModel;
import rdhxb.crbrservice.client.CrbrClient;
import rdhxb.crbrservice.config.RabbitMqConfig;
import rdhxb.crbrservice.crbrMapper.Mapper;
import rdhxb.crbrservice.dto.CrbrApiResponse;
import rdhxb.crbrservice.entity.CrbrModel;
import rdhxb.crbrservice.entity.CrbrPowiazanieModel;


import java.util.*;

import static rdhxb.common.base.PeselMasker.mask;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrbrService {

    private final CrbrClient client;
    private final Mapper mapper;
    private final CrbrTranService crbrTranService;

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    @SendTo(RabbitMqConfig.REPLIES)
    public RepliesModel receiverKrsNumber(KRSNumberModel krsMessage){

        RepliesModel repliesModel = new RepliesModel();
        repliesModel.setSource(Source.CRBR);
        repliesModel.setRequestId(krsMessage.getId());


        if (krsMessage.getKrsNum() == null){
            log.error("No KRS number !");
            repliesModel.setErrorMessage("No KRS number provided !");
            repliesModel.setStatus(Status.FAILED);
            repliesModel.setPayload(null);

            return repliesModel;
        }

        try {
            var response = client.getDataFromKrs(krsMessage.getKrsNum());

            CrbrApiResponse data = mapper.toCrbrResponse(response);

            CrbrModel crbrModel = mapper.toCrbrModel(data, krsMessage.getId(), krsMessage.getKrsNum());

            Set<String> pesels = new HashSet<>();

            Map<String, CrbrPowiazanieModel> powiazania = new HashMap<>();

            for (var spolka : data.spolki()){
                for (var beneficjent : spolka.beneficjenci()){
                    pesels.add(beneficjent.pesel());
                }
                dodajPowiazania(powiazania, spolka, krsMessage);
            }

            for (String pesel : pesels){
                try {
                    if (pesel != null && pesel.length() == 11){
                        var res = client.getDataFromPesel(pesel);
                        CrbrApiResponse peselRes = mapper.toCrbrResponse(res);

                        for (CrbrApiResponse.Spolka spolka : peselRes.spolki()){
                            dodajPowiazania(powiazania, spolka, krsMessage);
                        }
                    }
                }catch (Exception e){
                    log.error("Error with pesel : {}", mask(pesel), e);
                }
            }

            crbrTranService.saveData(crbrModel,powiazania.values());

            CrbrData crbrData = mapper.toCrbrData(crbrModel, powiazania.values());

            repliesModel.setPayload(crbrData);
            repliesModel.setStatus(Status.SUCCESS);

            return repliesModel;

        }catch (Exception e){
            log.error("Error with krs : {}", krsMessage.getKrsNum(), e);
            repliesModel.setErrorMessage(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            repliesModel.setStatus(Status.FAILED);

            return repliesModel;
        }
    }

    private void dodajPowiazania(Map<String, CrbrPowiazanieModel> powiazania, CrbrApiResponse.Spolka spolka, KRSNumberModel krsMessage){

        mapper.toPowiazania(spolka, krsMessage.getId(), krsMessage.getKrsNum())
                .forEach(powiazania::putIfAbsent);
    }

}
