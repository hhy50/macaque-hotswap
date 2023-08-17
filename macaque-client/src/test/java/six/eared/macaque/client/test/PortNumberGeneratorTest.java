package six.eared.macaque.client.test;

import org.junit.Assert;
import org.junit.Test;
import six.eared.macaque.client.common.PID;
import six.eared.macaque.client.common.PortNumberGenerator;

import java.util.HashMap;
import java.util.Map;

public class PortNumberGeneratorTest {


    @Test
    public void testGeneRatePort() {
        Map<Integer, Integer> map = new HashMap<>(2000);
        int pid = (int) PID.getCurrentPid();
        for (int i = 0; i < 1000; i++) {
            Integer port = PortNumberGenerator.getPort(pid++);
            Integer ipid = map.get(port);
            if (ipid != null) {
                throw new RuntimeException("port duplicate");
            }
            map.put(port, pid);
            Assert.assertTrue(port >= 3030);
        }
    }
}
