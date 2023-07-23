package six.eared.macaque.server.http.interfaces;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.common.rmi.RmiResult;
import six.eared.macaque.http.annotitions.Path;
import six.eared.macaque.server.config.LoggerName;
import six.eared.macaque.server.http.ServerHttpInterface;

@Path("/hotSwap")
public class HotSwapClassRequestHandler extends ServerHttpInterface<HotSwapClassRequestHandler.HotSwapClass> {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.auto());

    @Override
    public RmiResult process0(HotSwapClass hotSwapClass) {
        System.out.println(hotSwapClass);
        return RmiResult.success();
    }

    public static class HotSwapClass {
        private Integer pid;
        private String className;
        private String newClassData;

        public Integer getPid() {
            return pid;
        }

        public void setPid(Integer pid) {
            this.pid = pid;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getNewClassData() {
            return newClassData;
        }

        public void setNewClassData(String newClassData) {
            this.newClassData = newClassData;
        }

        @Override
        public String toString() {
            return "HotSwapClass{" +
                    "pid=" + pid +
                    ", className='" + className + '\'' +
                    ", newClassData='" + newClassData + '\'' +
                    '}';
        }
    }
}
