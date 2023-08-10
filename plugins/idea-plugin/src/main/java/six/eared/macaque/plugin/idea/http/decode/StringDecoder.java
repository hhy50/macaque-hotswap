package six.eared.macaque.plugin.idea.http.decode;

public class StringDecoder implements ResponseDecoder<String> {

    public String decode(byte[] bytes) {
        if (bytes != null && bytes.length > 0) {
            return new String(bytes);
        }
        return null;
    }
}
