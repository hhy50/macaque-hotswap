package six.eared.macaque.plugin.idea.http.request;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.lf5.util.StreamUtils;
import six.eared.macaque.plugin.idea.common.GenericTyped;
import six.eared.macaque.plugin.idea.http.decode.JsonDecoder;
import six.eared.macaque.plugin.idea.http.decode.ResponseDecoder;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;


public abstract class BaseRequest<T> extends GenericTyped<T> implements Request<T> {

    protected String host;

    protected BaseRequest(String host) {
        this.host = host;
    }

    /**
     * @param consumer
     */
    public void execute(Consumer<T> consumer) throws Exception {
        HttpRequestBase httpRequest = request();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(1000)
                .setConnectionRequestTimeout(1000)
                .build();
        httpRequest.setConfig(requestConfig);

        if (httpRequest instanceof HttpPost) {
            ((HttpPost) httpRequest).setEntity(entityBuild()
                    .build());
        }
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            CloseableHttpResponse response = httpClient.execute(httpRequest);
            InputStream is = response.getEntity().getContent();

            byte[] bytes = StreamUtils.getBytes(is);

            T responseEntity = decoder()
                    .decode(bytes);
            if (responseEntity != null) {
                consumer.accept(responseEntity);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    protected abstract HttpRequestBase request();

    protected MultipartEntityBuilder entityBuild() {
        return MultipartEntityBuilder.create()
                .setCharset(StandardCharsets.UTF_8)
                .setContentType(ContentType.APPLICATION_JSON);
    }

    protected ResponseDecoder<T> decoder() {
        return new JsonDecoder<>(getType());
    }
}
