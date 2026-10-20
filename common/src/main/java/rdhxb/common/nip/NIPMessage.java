package rdhxb.common.nip;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class NIPMessage {

    private UUID requestid;
    private String NIP;
}
