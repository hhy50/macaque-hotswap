package six.eared.macaque.http.test;

import org.junit.Before;
import org.junit.Test;
import six.eared.macaque.http.HttpConfig;
import six.eared.macaque.http.MacaqueHttpServer;
import six.eared.macaque.http.annotitions.Path;
import six.eared.macaque.http.handler.BaseRequestHandler;

import java.util.Arrays;

public class MacaqueHttpServerTest {

    private HttpConfig config;

    @Before
    public void after() {
        this.config = new HttpConfig(8081);
        this.config.setRootPath("/test");
    }


    @Test
    public void testServerStart() throws InterruptedException {
        new MacaqueHttpServer(this.config, Arrays.asList(new TestRequestHandler()))
                .start();
    }

    @Path("/getUser")
    public static class TestRequestHandler extends BaseRequestHandler<User> {
        @Override
        public Object process0(User user) {
            System.out.println(user);
            return user;
        }
    }

    public static class P {
        protected Integer age;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }

    public static class User extends P {
        private String name;


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "User{" +
                    "name=" + name +
                    ", age=" + age +
                    '}';
        }
    }
}
