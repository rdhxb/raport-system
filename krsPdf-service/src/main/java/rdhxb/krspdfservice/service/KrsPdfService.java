package rdhxb.krspdfservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import rdhxb.common.krs.KRSNumberModel;
import rdhxb.krspdfservice.config.RabbitMqConfig;
import rdhxb.krspdfservice.scraper.KrsPdfScraper;

@Slf4j
@Service
public class KrsPdfService {

    KrsPdfScraper krsPdfScraper = new KrsPdfScraper();

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public void receiveKrsNumber(KRSNumberModel krsMess){

        try {
            krsPdfScraper.scrapePdf(krsMess.getKrsNum());
            log.info("succesfuly proces krs <>|<>");

        }catch (Exception e){
            log.error(e.getMessage());
        }

    }

}
