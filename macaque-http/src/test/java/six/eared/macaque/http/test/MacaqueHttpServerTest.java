package six.eared.macaque.http.test;

import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
        new MacaqueHttpServer(this.config, Flux.just(new TestRequestHandler())).start();
        Thread.sleep(100000000L);
    }

    @Path("/getUser")
    public static class TestRequestHandler extends BaseRequestHandler<User> {
        @Override
        public Mono<Object> process0(Mono<User> user) {
            user.subscribe((u) -> {
                System.out.println(u);
            });
            return (Mono) user;
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
