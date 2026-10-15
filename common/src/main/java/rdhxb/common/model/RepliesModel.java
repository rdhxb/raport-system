package rdhxb.common.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class RepliesModel {
    private UUID requestId;
    private Source source;
    private Object payload;
}
