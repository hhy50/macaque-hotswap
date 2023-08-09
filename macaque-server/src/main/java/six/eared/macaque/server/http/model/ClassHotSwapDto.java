package six.eared.macaque.server.http.model;

import six.eared.macaque.http.request.MultipartFile;

public class ClassHotSwapDto {
    private Integer pid;

    private String fileType;

    private MultipartFile fileData;

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public MultipartFile getFileData() {
        return fileData;
    }

    public void setFileData(MultipartFile fileData) {
        this.fileData = fileData;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
