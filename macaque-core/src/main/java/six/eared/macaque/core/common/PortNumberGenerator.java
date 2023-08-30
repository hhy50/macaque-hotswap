package six.eared.macaque.core.common;


public class PortNumberGenerator {

    /**
     *
     * @param pid
     * @return
     */
    public static Integer getPort(Integer pid) {
        int low = pid ^ 3030;
        int high = (pid>>>10) ^ 3030;
        return ((low ^ high) % 3030) + 3030;
    }
}
