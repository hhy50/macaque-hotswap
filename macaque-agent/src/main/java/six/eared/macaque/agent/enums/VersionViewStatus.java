package six.eared.macaque.agent.enums;

public enum VersionViewStatus {

    ACTIVE(1),

    HISTORY(2),

    DAMAGED(3),
    ;

    private final int status;

    VersionViewStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
