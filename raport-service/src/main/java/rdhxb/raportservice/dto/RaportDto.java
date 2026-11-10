package rdhxb.raportservice.dto;



import rdhxb.raportservice.model.GeneratedFrom;
import rdhxb.raportservice.model.RaportStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RaportDto(

        UUID requestId,
        RaportStatus status,
        Instant completedAt,
        List<SourceRaportDto> raportSources,
        GeneratedFrom generatedFrom,
        String krsNumber,
        String nipNumber

) {}


