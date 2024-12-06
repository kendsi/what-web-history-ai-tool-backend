package cap.team3.what.exception;

public class GptResponseException extends RuntimeException {
    public GptResponseException(String message) {
        super(message);
    }
}
