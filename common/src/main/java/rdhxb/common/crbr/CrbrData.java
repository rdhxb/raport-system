package rdhxb.common.crbr;



import java.time.Instant;
import java.util.List;

public record CrbrData(
        String krsNumber,
        StatusCrbr status,
        String identyfikatorWniosku,
        Instant fetchedAt,
        List<CrbrPowiazanieData> powiazania
) {}
