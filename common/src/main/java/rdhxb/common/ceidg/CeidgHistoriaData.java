package rdhxb.common.ceidg;

import java.time.LocalDate;

// Lightweight entry of a previous (non-current) CEIDG business of the same NIP
public record CeidgHistoriaData(
        String ceidgId,
        String nazwa,
        String status,
        LocalDate dataRozpoczecia,
        LocalDate dataZakonczenia,
        LocalDate dataWykreslenia
) {}
