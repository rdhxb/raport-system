package rdhxb.common.exception;


public class TransientApiException extends RuntimeException {
    public TransientApiException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
