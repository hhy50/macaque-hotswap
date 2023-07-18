package six.eared.macaque.http.test;

import org.junit.Before;
import org.junit.Test;
import six.eared.macaque.http.HttpConfig;
import six.eared.macaque.http.MacaqueHttpServer;

public class MacaqueHttpServerTest {

    private HttpConfig config;

    @Before
    public void after() {
        this.config = new HttpConfig(8081);
    }


    @Test
    public void testServerStart() throws InterruptedException {
        new MacaqueHttpServer(this.config).start();
        Thread.sleep(100000000L);
    }
}
