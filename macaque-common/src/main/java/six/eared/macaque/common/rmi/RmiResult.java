package six.eared.macaque.common.rmi;

import java.io.Serializable;

public class RmiResult implements Serializable {
    private boolean success;

    private String message;

    public RmiResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }


    public static RmiResult success() {
        return new RmiResult(true, "success");
    }

    public static RmiResult error(String message) {
        return new RmiResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
