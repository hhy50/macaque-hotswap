package six.eared.macaque.agent.vcs;


public class VersionDescriptor implements Comparable<VersionDescriptor> {

    private Integer number;

    private String date;

    @Override
    public boolean equals(Object o) {
        if (o instanceof VersionDescriptor) {
            return ((VersionDescriptor) o).number.equals(this.number);
        }
        return false;
    }

    @Override
    public int compareTo(VersionDescriptor o) {
        return 0;
    }
}
