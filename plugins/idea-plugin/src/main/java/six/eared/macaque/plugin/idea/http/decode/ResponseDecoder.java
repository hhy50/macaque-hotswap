package six.eared.macaque.plugin.idea.http.decode;

public interface ResponseDecoder <T> {
    public T decode(byte[] bytes);
}
