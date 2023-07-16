package six.eared.macaque.common.rmi;

public class ClassHotSwapRmiData extends RmiData {

    private String className;

    private byte[] newClassByte;

    public ClassHotSwapRmiData(String className, byte[] newClassByte) {
        this.className = className;
        this.newClassByte = newClassByte;
    }


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public byte[] getNewClassByte() {
        return newClassByte;
    }

    public void setNewClassByte(byte[] newClassByte) {
        this.newClassByte = newClassByte;
    }
}
