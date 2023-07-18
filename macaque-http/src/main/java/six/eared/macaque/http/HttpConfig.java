package six.eared.macaque.http;

public class HttpConfig {

    private Integer port;

    private String rootPath;

    public HttpConfig(Integer port) {
        this.port = port;
        this.rootPath = "/";
    }

    public HttpConfig() {
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
}
