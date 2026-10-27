package rdhxb.crbrservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import rdhxb.common.krs.KRSNumberModel;
import rdhxb.crbrservice.client.CrbrClient;
import rdhxb.crbrservice.config.RabbitMqConfig;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrbrService {

    private final CrbrClient client;

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public void receiverKrsNumber(KRSNumberModel krsMessage){

        if (krsMessage.getKrsNum() == null){
            log.error("No KRS number !");
        }

        var response = client.getData(krsMessage.getKrsNum());

        var listofspolkaibeneficjent = response.getPobierzInformacjeOSpolkachIBeneficjentachOdpowiedzDane().getListaInformacjiOSpolkachIBeneficjentach().getSpolkaIBeneficjenci();


        try {

            var beneficjentrzeczywistylista = listofspolkaibeneficjent.getFirst().getListaBeneficjentowRzeczywistych().getBeneficjentRzeczywisty();

            for (var beneficjent : beneficjentrzeczywistylista){
                System.out.println(beneficjent.getPierwszeImie());
                System.out.println(beneficjent.getInformacjeOUprawnieniachPrzyslugujacychBeneficjentowiGrupowemuTrust());
                System.out.println(beneficjent.getNazwisko());
                System.out.println(beneficjent.getPESEL());
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }





    }

}
