package morningsage.telecraft.exceptions;

public class InvalidProtocolException extends TransportException {
    public InvalidProtocolException() {
        super();
    }

    public InvalidProtocolException(String message) {
        super(message);
    }

    public InvalidProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidProtocolException(Throwable cause) {
        super(cause);
    }

    protected InvalidProtocolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
