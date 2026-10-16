package rdhxb.common.base;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class RepliesModel {
    private UUID requestId;
    private Source source;
    private Object payload;
    private Status status;
    private String errorMessage;
}
