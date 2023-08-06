package six.eared.macaque.plugin.idea.http.interfaces;


import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import six.eared.macaque.plugin.idea.config.RequestUrl;
import six.eared.macaque.plugin.idea.http.request.BaseRequest;
import six.eared.macaque.plugin.idea.http.response.JpsResponse;


public class Jps extends BaseRequest<JpsResponse> {

    public Jps(String host) {
        super(host);
    }

    @Override
    protected HttpRequestBase request() {
        return new HttpPost(host + RequestUrl.JPS);
    }
}
