package six.eared.macaque.http.test;

import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import six.eared.macaque.http.HttpConfig;
import six.eared.macaque.http.MacaqueHttpServer;
import six.eared.macaque.http.annotitions.Path;
import six.eared.macaque.http.handler.BaseRequestHandler;

public class MacaqueHttpServerTest {

    private HttpConfig config;

    @Before
    public void after() {
        this.config = new HttpConfig(8081);
    }


    @Test
    public void testServerStart() throws InterruptedException {
        new MacaqueHttpServer(this.config, Flux.just(new TestRequestHandler<User>() {

        })).start();
        Thread.sleep(100000000L);
    }

    @Path("/getUser")
    public static abstract class TestRequestHandler<T> extends BaseRequestHandler<T> {
        @Override
        public Object process0(T user) {
            return user;
        }
    }


    public static class User {
        private String name;

        private Integer age;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
