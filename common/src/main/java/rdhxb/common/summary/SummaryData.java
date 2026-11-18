package rdhxb.common.summary;

import lombok.Data;
import rdhxb.common.ceidg.CeidgData;
import rdhxb.common.crbr.CrbrData;
import rdhxb.common.krs.KrsData;
import rdhxb.common.nip.WlData;
import rdhxb.common.sudop.SudopData;

import java.util.UUID;

@Data
public class SummaryData {

    private UUID requestId;
    private KrsData krs;
    private WlData wl;
    private CeidgData ceidg;
    private CrbrData crbr;
    private SudopData sudop;

}
