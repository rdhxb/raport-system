package rdhxb.crbrservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import rdhxb.common.krs.KRSNumberModel;
import rdhxb.crbrservice.client.CrbrClient;
import rdhxb.crbrservice.config.RabbitMqConfig;
import rdhxb.crbrservice.crbrMapper.Mapper;
import rdhxb.crbrservice.dto.CrbrApiResponse;
import rdhxb.crbrservice.entity.CrbrModel;
import rdhxb.crbrservice.entity.CrbrPowiazanieModel;

import java.util.*;

import static rdhxb.crbrservice.client.CrbrClient.mask;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrbrService {

    private final CrbrClient client;
    private final Mapper mapper;

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public void receiverKrsNumber(KRSNumberModel krsMessage){

        if (krsMessage.getKrsNum() == null){
            log.error("No KRS number !");
            return;
        }

        try {
            var response = client.getDataFromKrs(krsMessage.getKrsNum());

            CrbrApiResponse data = mapper.toCrbrResponse(response);

            CrbrModel crbrModel = mapper.toCrbrModel(data, krsMessage.getId(), krsMessage.getKrsNum());
            System.out.println(crbrModel);

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

            System.out.println(powiazania.values());

        }catch (Exception e){
            log.error("Error with krs : {}", krsMessage.getKrsNum(), e);
        }



    }

    private void dodajPowiazania(Map<String, CrbrPowiazanieModel> powiazania, CrbrApiResponse.Spolka spolka, KRSNumberModel krsMessage){

        for (CrbrPowiazanieModel powiazanie : mapper.toPowiazania(spolka, krsMessage.getId(), krsMessage.getKrsNum())){
            powiazania.putIfAbsent(powiazanie.getPesel() + ":" + powiazanie.getKrs(), powiazanie);
        }
    }

}
