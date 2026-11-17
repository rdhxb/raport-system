package rdhxb.summaryservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import rdhxb.common.summary.SummaryModel;
import rdhxb.summaryservice.client.Client;
import rdhxb.summaryservice.config.RabbitMqConfig;
import rdhxb.summaryservice.preprocessing.RaportPreprocessor;
import rdhxb.summaryservice.prompt.SystemPrompt;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class SummaryService {

    private final RabbitTemplate rabbitTemplate;
    private final Client client;

    private final RaportPreprocessor preprocessor;

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public void getRaportFromRaportService(SummaryModel summaryModel) throws IOException, InterruptedException {

        String dataForModel = preprocessor.raportProcess(summaryModel);

//        log.info(dataForModel);


        var res = client.sendData(SystemPrompt.SUMMARY,dataForModel);

        log.info(res);
    }

}
