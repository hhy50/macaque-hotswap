package six.eared.macaque.server.http.model;

public class ClassHotSwapDto {
    private Integer pid;
    private String className;
    private String newClassData; // 16进制

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

    public String getNewClassData() {
        return newClassData;
    }

    public void setNewClassData(String newClassData) {
        this.newClassData = newClassData;
    }

    @Override
    public String toString() {
        return "ClassHotSwapDto{" +
                "pid=" + pid +
                ", className='" + className + '\'' +
                ", newClassData='" + newClassData + '\'' +
                '}';
    }
}
