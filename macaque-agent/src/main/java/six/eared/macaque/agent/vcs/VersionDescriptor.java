package six.eared.macaque.agent.vcs;


import lombok.Getter;
import lombok.Setter;
import six.eared.macaque.common.util.DateUtil;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class VersionDescriptor implements Comparable<VersionDescriptor> {

    private static final AtomicInteger INCREMENTOR = new AtomicInteger(0);

    private Integer number;

    private String date;

    public static VersionDescriptor incrementVersion() {
        VersionDescriptor vd = new VersionDescriptor();
        vd.setNumber(INCREMENTOR.incrementAndGet());
        vd.setDate(DateUtil.nowString());
        return vd;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof VersionDescriptor) {
            return ((VersionDescriptor) o).number.equals(this.number);
        }
        return false;
    }

    @Override
    public String toString() {
        return "VersionDescriptor{" +
                "number=" + number +
                ", date='" + date + '\'' +
                '}';
    }

    @Override
    public int compareTo(VersionDescriptor o) {
        return 0;
    }

    public static class VersionComparator implements Comparator<VersionDescriptor> {
        @Override
        public int compare(VersionDescriptor o1, VersionDescriptor o2) {
            return o1.compareTo(o2);
        }
    }
}
