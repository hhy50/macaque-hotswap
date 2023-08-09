package six.eared.macaque.plugin.idea.http.interfaces;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import six.eared.macaque.plugin.idea.config.RequestUrl;
import six.eared.macaque.plugin.idea.http.request.BaseRequest;
import six.eared.macaque.plugin.idea.http.response.CommonResponse;

import java.nio.charset.StandardCharsets;

public class HotSwap extends BaseRequest<CommonResponse> {

    private String pid;

    private String fileName;

    private String fileType;

    private byte[] fileData;

    public HotSwap(String host) {
        super(host);
    }

    @Override
    protected HttpRequestBase request() {
        return new HttpPost(host + RequestUrl.HOW_SWAP);
    }

    @Override
    protected MultipartEntityBuilder entityBuild() {
        return MultipartEntityBuilder.create()
                .setCharset(StandardCharsets.UTF_8)
                .setContentType(ContentType.MULTIPART_FORM_DATA)
                .addPart("fileData", new ByteArrayBody(fileData, fileName))
                .addPart("fileName", new StringBody(fileName, ContentType.TEXT_PLAIN))
                .addPart("pid", new StringBody(pid, ContentType.TEXT_PLAIN))
                .addPart("fileType", new StringBody(fileType, ContentType.TEXT_PLAIN));
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
}
