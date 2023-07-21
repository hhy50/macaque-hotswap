package six.eared.macaque.http.decode;

public interface Encoder<T> {

    public T encode(Object obj);
}
