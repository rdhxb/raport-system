package rdhxb.common.summary;

import lombok.Data;

import java.util.UUID;

@Data
public class SummaryModel {
    private UUID requestId;
    private String payload;
}
