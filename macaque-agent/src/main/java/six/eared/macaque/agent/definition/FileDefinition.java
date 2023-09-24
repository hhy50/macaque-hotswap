package six.eared.macaque.agent.definition;

public class FileDefinition implements Definition {

    private String name;

    private String fileType;

    private byte[] bytes;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFileType() {
        return fileType;
    }

    @Override
    public byte[] getByteArray() {
        return bytes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
