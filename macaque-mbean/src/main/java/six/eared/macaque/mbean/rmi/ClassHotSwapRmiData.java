package six.eared.macaque.mbean.rmi;

public class ClassHotSwapRmiData extends RmiData {
    private String fileType;
    private byte[] fileData;

    public ClassHotSwapRmiData(String fileType, byte[] fileData) {
        this.fileType = fileType;
        this.fileData = fileData;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }
}
