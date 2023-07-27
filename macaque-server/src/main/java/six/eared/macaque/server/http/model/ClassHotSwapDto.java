package six.eared.macaque.server.http.model;

import six.eared.macaque.http.request.MultipartFile;

public class ClassHotSwapDto {
    private Integer pid;
    private String className;

    private MultipartFile newClassData;

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public MultipartFile getNewClassData() {
        return newClassData;
    }

    public void setNewClassData(MultipartFile newClassData) {
        this.newClassData = newClassData;
    }
}
