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
import rdhxb.common.krs.KRSNumberModel;
import rdhxb.common.krs.KrsData;
import rdhxb.common.nip.NIPMessage;
import rdhxb.raportservice.config.RabbitMqConfig;
import rdhxb.raportservice.dto.RaportDto;
import rdhxb.raportservice.dto.SourceRaportDto;
import rdhxb.raportservice.entity.Raport;
import rdhxb.raportservice.entity.RaportSource;
import rdhxb.raportservice.model.GeneratedFrom;
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

        KRSNumberModel krsMessage = new KRSNumberModel();
        krsMessage.setId(UUID.randomUUID());
        krsMessage.setKrsNum(krs);

        Raport raport = generateRaport(krsMessage.getId(),krsMessage.getKrsNum(),null,GeneratedFrom.KRS);

        generateRaportSource(raport,Source.KRS);
        generateRaportSource(raport,Source.CRBR);


        rabbitTemplate.convertAndSend(RabbitMqConfig.REQUESTS_EXCHANGE,"",krsMessage);

        return raport.getRequestId();
    }

    public void sendNipToExchangerFromKrs(UUID requestId, String nip){
        NIPMessage nipMessage = new NIPMessage();
        nipMessage.setRequestid(requestId);
        nipMessage.setNIP(nip);

        generateRaportSource(raportRepository.findById(requestId).orElseThrow(), Source.WL);


        rabbitTemplate.convertAndSend(RabbitMqConfig.NIP_EXCHANGER,"nip.krs",nipMessage);
    }
    public UUID sendNipToExchangerDirectly(String nip){

        NIPMessage nipMessage = new NIPMessage();
        nipMessage.setRequestid(UUID.randomUUID());
        nipMessage.setNIP(nip);

        Raport raport = generateRaport(nipMessage.getRequestid(),null,nipMessage.getNIP(),GeneratedFrom.NIP);


        generateRaportSource(raport,Source.CEIDG);
        generateRaportSource(raport,Source.WL);


        rabbitTemplate.convertAndSend(RabbitMqConfig.NIP_EXCHANGER,"nip.direct",nipMessage);

        return raport.getRequestId();
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

        switch (replies.getStatus()){
            case SUCCESS -> {
                Source source = replies.getSource();

                switch (source){
                    case KRS, CRBR, WL, CEIDG, SUDOP -> completeSource(replies);
                    default -> log.error("getReplies does not catch response ! default logic !");
                }
            }
            case NOT_FOUND -> notFoundSource(replies);
            case FAILED -> failSource(replies);
            case null, default -> log.error("Unknown status for requestId : " + replies.getRequestId());
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



    private void completeSource(RepliesModel replies){
        Optional<RaportSource> raportSource = raportSourceRepository.findByRaportRequestIdAndSource(replies.getRequestId(), replies.getSource());
        if (raportSource.isPresent()){
            raportSource.get().setReceivedAt(Instant.now());
            raportSource.get().setPayload(mapper.writeValueAsString(replies.getPayload()));
            raportSource.get().setStatus(SourceStatus.SUCCESS);
            raportSourceRepository.save(raportSource.get());


            if (replies.getSource() == Source.KRS){
                KrsData krsData = mapper.convertValue(replies.getPayload(),KrsData.class);
                String nip = krsData.nip();
                if (nip != null && !nip.isBlank()){
                        Raport raport = raportRepository.findById(replies.getRequestId()).orElseThrow();
                        sendNipToExchangerFromKrs(replies.getRequestId(), nip);
                        raport.setNipNumber(nip);
                }
            }

            if (replies.getSource() != Source.SUDOP) {
                markDoneIfComplete(replies.getRequestId());
            }

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

            if (replies.getSource() != Source.SUDOP) {
                markDoneIfComplete(replies.getRequestId());
            }
        }else {
            log.error("FAILED !! requestId {} source : {} error : {}", replies.getRequestId(), replies.getSource(), replies.getErrorMessage());
        }
    }

    private void notFoundSource(RepliesModel replies){
        var raportSource = raportSourceRepository.findByRaportRequestIdAndSource(replies.getRequestId(),replies.getSource());

        if (raportSource.isPresent()){
            raportSource.get().setStatus(SourceStatus.SKIPPED);
            raportSource.get().setErrorMessage(replies.getErrorMessage());
            raportSource.get().setReceivedAt(Instant.now());
            raportSourceRepository.save(raportSource.get());

            if (replies.getSource() != Source.SUDOP) {
                markDoneIfComplete(replies.getRequestId());
            }
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
                sourceRaports,
                raport.getGeneratedFrom(),
                raport.getKrsNumber(),
                raport.getNipNumber()


        );

        return raportDto;
    }

    public UUID requestSudop(UUID requestId){
        Raport raport = raportRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (raport.getNipNumber() == null){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Raport has no NIP number");
        }

        Optional<RaportSource> existing = raportSourceRepository.findByRaportRequestIdAndSource(requestId, Source.SUDOP);

        RaportSource raportSource;
        if (existing.isPresent()){
            raportSource = existing.get();
            if (raportSource.getStatus() != SourceStatus.FAILED){
                return raport.getRequestId();
            }
            raportSource.setErrorMessage(null);
            raportSource.setPayload(null);
            raportSource.setReceivedAt(null);
        }else {
            raportSource = new RaportSource();
            raportSource.setSource(Source.SUDOP);
            raportSource.setRaport(raport);
        }
        raportSource.setStatus(SourceStatus.PENDING);

        raportSourceRepository.save(raportSource);

        NIPMessage nipMessage = new NIPMessage();
        nipMessage.setNIP(raport.getNipNumber());
        nipMessage.setRequestid(requestId);
        rabbitTemplate.convertAndSend(RabbitMqConfig.SUDOP_EXCHANGER,"", nipMessage);

        return raport.getRequestId();
    }

    private void generateRaportSource(Raport raport, Source source){
        RaportSource raportSource = new RaportSource();
        raportSource.setSource(source);
        raportSource.setStatus(SourceStatus.PENDING);
        raportSource.setRaport(raport);

        raportSourceRepository.save(raportSource);
    }

    private Raport generateRaport(UUID requestId, String krsNum, String nipNumber, GeneratedFrom generatedFrom){
        Raport raport = new Raport();
        raport.setRequestId(requestId);
        raport.setStatus(RaportStatus.IN_PROGRESS);
        raport.setGeneratedFrom(generatedFrom);
        raport.setKrsNumber(krsNum);
        raport.setNipNumber(nipNumber);


        return raportRepository.save(raport);
    }
}
