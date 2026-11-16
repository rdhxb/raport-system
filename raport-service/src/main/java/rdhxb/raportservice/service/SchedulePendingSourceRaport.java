package rdhxb.raportservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rdhxb.common.base.Source;
import rdhxb.raportservice.entity.RaportSource;
import rdhxb.raportservice.model.SourceStatus;
import rdhxb.raportservice.repository.RaportSourceRepository;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class SchedulePendingSourceRaport {
    private final RaportSourceRepository raportSourceRepository;
    private final RaportService raportService;

    // How long a source may stay PENDING before it is marked FAILED
    @Value("${raport.pending-timeout-seconds}")
    private long pendingTimeoutSeconds;


    @Scheduled(cron = "${raport.pending-check-cron}")
    @Transactional
    public void handlePendingRaportSource(){
        List<RaportSource> sources = raportSourceRepository.findAllByCreatedAtBeforeAndStatus(Instant.now().minusSeconds(pendingTimeoutSeconds), SourceStatus.PENDING);
        Set<UUID> uuidSet = new HashSet<>();
        if (!sources.isEmpty()){
            for (RaportSource source : sources){
                if (source.getSource() != Source.SUDOP){
                    source.setStatus(SourceStatus.FAILED);
                    source.setErrorMessage("Timeout set status FAILED");
                    uuidSet.add(source.getRaport().getRequestId());
                }
            }
            for (UUID uuid : uuidSet){
                raportService.markDoneIfComplete(uuid);
            }

        }

    }
}
