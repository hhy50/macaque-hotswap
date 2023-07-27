package six.eared.macaque.http.request;

public class MultipartFile {

    private final byte[] bytes;

    public MultipartFile(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return new String(bytes);
    }
}
