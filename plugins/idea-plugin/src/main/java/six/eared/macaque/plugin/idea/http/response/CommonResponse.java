package six.eared.macaque.plugin.idea.http.response;

public class CommonResponse {

    private String message;

    private Boolean success;

    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
