package six.eared.macaque.mbean.rmi;

import java.util.Map;

public class ClassHotSwapRmiData extends RmiData {
    private String fileName;
    private String fileType;
    private byte[] fileData;
    private Map<String, String> extProperties;

    public ClassHotSwapRmiData(String fileName, String fileType, byte[] fileData, Map<String, String> extProperties) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileData = fileData;
        this.extProperties = extProperties;
    }

    public ClassHotSwapRmiData(String fileType, byte[] fileData, Map<String, String> extProperties) {
        this.fileType = fileType;
        this.fileData = fileData;
        this.extProperties = extProperties;
    }

    public ClassHotSwapRmiData(String fileType, byte[] fileData) {
        this(fileType, fileData, null);
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Map<String, String> getExtProperties() {
        return extProperties;
    }

    public void setExtProperties(Map<String, String> extProperties) {
        this.extProperties = extProperties;
    }
}
