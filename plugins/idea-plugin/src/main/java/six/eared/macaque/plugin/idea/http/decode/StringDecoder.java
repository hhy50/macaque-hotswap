package six.eared.macaque.plugin.idea.http.decode;

public class StringDecoder implements ResponseDecoder<String> {

    public String decode(byte[] bytes) {
        return new String(bytes);
    }
}
