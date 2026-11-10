package rdhxb.raportservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rdhxb.common.base.RepliesModel;
import rdhxb.common.base.Source;
import rdhxb.common.base.Status;
import rdhxb.common.crbr.CrbrData;
import rdhxb.common.krs.KRSNumberModel;
import rdhxb.common.krs.KrsData;
import rdhxb.common.nip.NIPMessage;
import rdhxb.common.nip.WlData;
import rdhxb.raportservice.config.RabbitMqConfig;
import rdhxb.raportservice.dto.RaportDto;
import rdhxb.raportservice.dto.SourceRaportDto;
import rdhxb.raportservice.entity.Raport;
import rdhxb.raportservice.entity.RaportSource;
import rdhxb.raportservice.model.RaportStatus;
import rdhxb.raportservice.model.SourceStatus;
import rdhxb.raportservice.repository.RaportRepository;
import rdhxb.raportservice.repository.RaportSourceRepository;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaportService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper;
    private final RaportRepository raportRepository;
    private final RaportSourceRepository raportSourceRepository;

    public UUID sendKrsToExchanger(String krs){
        KRSNumberModel krsModel = new KRSNumberModel();
        krsModel.setId(UUID.randomUUID());
        krsModel.setKrsNum(krs);

        Raport raport = new Raport();
        raport.setRequestId(krsModel.getId());
        raport.setStatus(RaportStatus.IN_PROGRESS);
        raport.setKrsNumber(krsModel.getKrsNum());
        raportRepository.save(raport);

        RaportSource raportSourceKrs = new RaportSource();
        raportSourceKrs.setSource(Source.KRS);
        raportSourceKrs.setStatus(SourceStatus.PENDING);
        raportSourceKrs.setRaport(raport);

        RaportSource raportSourceCrbr = new RaportSource();
        raportSourceCrbr.setSource(Source.CRBR);
        raportSourceCrbr.setStatus(SourceStatus.PENDING);
        raportSourceCrbr.setRaport(raport);

        List<RaportSource> raportSources = new ArrayList<>();

        raportSources.add(raportSourceKrs);
        raportSources.add(raportSourceCrbr);
        raportSourceRepository.saveAll(raportSources);

        rabbitTemplate.convertAndSend(RabbitMqConfig.REQUESTS_EXCHANGE,"",krsModel);

        return raport.getRequestId();
    }

    public void sendNipToExchanger(UUID requestId, String nip){
        NIPMessage nipMessage = new NIPMessage();
        nipMessage.setRequestid(requestId);
        nipMessage.setNIP(nip);

        RaportSource raportSourceWL = new RaportSource();
        raportSourceWL.setSource(Source.WL);
        raportSourceWL.setStatus(SourceStatus.PENDING);
        raportSourceWL.setRaport(raportRepository.findById(requestId).orElseThrow());
        raportSourceRepository.save(raportSourceWL);

        rabbitTemplate.convertAndSend(RabbitMqConfig.NIP_EXCHANGER,"",nipMessage);
    }

    public void testSendToCeidg(String nip){
        NIPMessage nipMessage = new NIPMessage();
        nipMessage.setRequestid(UUID.randomUUID());
        nipMessage.setNIP(nip);

        rabbitTemplate.convertAndSend(RabbitMqConfig.NIP_EXCHANGER,"",nipMessage);
    }


    @RabbitListener(queues = RabbitMqConfig.REPLIES)
    @Transactional
    public void getReplies(RepliesModel replies){

        if (replies == null){
            return;
        }
        if (replies.getSource() == null){
            return;
        }
        if (replies.getStatus() == Status.SUCCESS) {
            Source source = replies.getSource();
            switch (source){
                case KRS, CRBR, WL -> completeSource(replies);
                default -> log.error("getReplies does not catch response ! default logic !");
            }
        } else if (replies.getStatus() == Status.FAILED) {
            failSource(replies);
        }

    }

    private void markDoneIfComplete(UUID requestId){
        boolean isAnyPending = raportSourceRepository.existsByRaportRequestIdAndStatus(requestId, SourceStatus.PENDING);
        if (isAnyPending){
            return;
        }

        raportRepository.findById(requestId).ifPresent(raport -> {
            raport.setStatus(RaportStatus.DONE);
            raport.setCompletedAt(Instant.now());
            log.info("Raport {} DONE", requestId);
        });
    }

    public Object convertPayload(RepliesModel repliesModel){
        Source source = repliesModel.getSource();

        switch (source){
            case KRS -> {
                return mapper.convertValue(repliesModel.getPayload(), KrsData.class);
            }
            case WL -> {
                return mapper.convertValue(repliesModel.getPayload(), WlData.class);
            }
            case CRBR -> {
                return mapper.convertValue(repliesModel.getPayload(), CrbrData.class);
            }
            default -> {
                return null;
            }
        }
    }

    private void handleKrsResponse(RepliesModel replies){
        try {

            KrsData data = (KrsData) convertPayload(replies);

            sendNipToExchanger(replies.getRequestId(), data.nip());
            log.info("NIP number sanded to exchanger from raport service !!!!");
            log.info("Request id should be : " + replies.getRequestId());
            System.out.println(data);


        } catch (Exception e) {
            System.out.println("Cant convert playload for request id : " + replies.getRequestId());
        }
    }

    private void handleWlResponse(RepliesModel replies){

        try {
            WlData wlData = (WlData) convertPayload(replies);
            var accountsNumbers = wlData.accountNumbers();
            System.out.println("Account numbers >");
            System.out.println(accountsNumbers);

            log.info("Processed Wl data in Raport Service !");
            System.out.println(wlData);

        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    private void handleCrbrResponse(RepliesModel replies){
        try {
            CrbrData crbrData = (CrbrData) convertPayload(replies);
            var powiazaniCrbr = crbrData.powiazania();
            System.out.println(powiazaniCrbr);
            log.info("Processed crbr data ! in raport-service !");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void completeSource(RepliesModel replies){
        Optional<RaportSource> raportSource = raportSourceRepository.findByRaportRequestIdAndSource(replies.getRequestId(), replies.getSource());
        if (raportSource.isPresent()){
            raportSource.get().setReceivedAt(Instant.now());
            raportSource.get().setPayload(mapper.writeValueAsString(replies.getPayload()));
            raportSource.get().setStatus(SourceStatus.SUCCESS);
            raportSourceRepository.save(raportSource.get());

            switch (replies.getSource()){
                case KRS -> handleKrsResponse(replies);
                case WL -> handleWlResponse(replies);
                case CRBR -> handleCrbrResponse(replies);
                default -> {
                    return;
                }
            }

            markDoneIfComplete(replies.getRequestId());

        }else {
            log.error("Could not find a raport source ! request id " + replies.getRequestId() + " source : " + replies.getSource());
        }
    }

    private void failSource(RepliesModel replies){
        var raportSource = raportSourceRepository.findByRaportRequestIdAndSource(replies.getRequestId(), replies.getSource());

        if (raportSource.isPresent()){
            raportSource.get().setStatus(SourceStatus.FAILED);
            raportSource.get().setErrorMessage(replies.getErrorMessage());
            raportSource.get().setReceivedAt(Instant.now());
            raportSourceRepository.save(raportSource.get());
            log.info("Error massage in raportSource !");

            markDoneIfComplete(replies.getRequestId());
        }else {
            log.error("FAILED !! requestId {} source : {} error : {}", replies.getRequestId(), replies.getSource(), replies.getErrorMessage());
        }
    }


    public RaportDto getRaport(UUID requestId){
        Raport raport = raportRepository.findById(requestId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<SourceRaportDto> sourceRaports = raportSourceRepository.findAllByRaport(raport);

        RaportDto raportDto = new RaportDto(
                requestId,
                raport.getStatus(),
                raport.getCompletedAt(),
                sourceRaports

        );

        return raportDto;
    }
}
