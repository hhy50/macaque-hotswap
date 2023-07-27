package six.eared.macaque.mbean.rmi;

import java.io.Serializable;

public class RmiResult implements Serializable {

    private boolean success;

    private String message;

    private Object data;

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

    public RmiResult data(Object data) {
        this.data = data;
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RmiResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
