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
        }

        var response = client.getData(krsMessage.getKrsNum());

        try {

            CrbrApiResponse data = mapper.toCrbrResponse(response);

            System.out.println(data);


//            map api response to CrbrModel (Entity) save into the db and pass the response model to rapost-serivice


        }catch (Exception e){
            System.out.println(e.getMessage());
        }





    }

}
