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

import java.util.HashSet;
import java.util.Set;

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

            System.out.println(data);
            Set<String> pesels = new HashSet<>();

            for (var spolka : data.spolki()){
                for (var beneficjent : spolka.beneficjenci()){
                    pesels.add(beneficjent.pesel());
                }
            }

            for (String pesel : pesels){
                try {
                    if (pesel != null && pesel.length() == 11){
                        var res = client.getDataFromPesel(pesel);
                        CrbrApiResponse peselRes = mapper.toCrbrResponse(res);
                        System.out.println(peselRes);
                    }
                }catch (Exception e){
                    log.error("Error with pesel : {}", mask(pesel), e);
                }
            }

        }catch (Exception e){
            log.error("Error with krs : {}", krsMessage.getKrsNum(), e);
        }

    }

}
