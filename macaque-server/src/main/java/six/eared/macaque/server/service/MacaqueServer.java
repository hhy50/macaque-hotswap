package six.eared.macaque.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.eared.macaque.core.attach.DefaultAttachFactory;
import six.eared.macaque.http.HttpConfig;
import six.eared.macaque.http.MacaqueHttpServer;
import six.eared.macaque.http.handler.RequestHandler;
import six.eared.macaque.server.config.ServerConfig;
import six.eared.macaque.server.http.interfaces.ClassHotSwapRequestHandler;
import six.eared.macaque.server.http.interfaces.JpsRequestHandler;

import java.util.Arrays;
import java.util.List;

/**
 * Macaque服务
 */
public class MacaqueServer implements MacaqueService {

    private static final Logger log = LoggerFactory.getLogger(MacaqueServer.class);

    /**
     * 服务配置
     */
    private final ServerConfig serverConfig;

    /**
     * http服务
     */
    private MacaqueHttpServer httpServer;

    private final DefaultAttachFactory defaultAttachFactory;

    public MacaqueServer(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.defaultAttachFactory = new DefaultAttachFactory();
    }

    /**
     * 启动服务
     */
    @Override
    public void start() {
        String rootPath = "/macaque";
        Integer port = this.serverConfig.getServerPort();
        //创建http服务
        this.httpServer = new MacaqueHttpServer(
                new HttpConfig(port, rootPath),
                buildInterface());
        this.httpServer.start();

        log.info("MacaqueServer start success, port is {}, rootPath:[{}]", port, rootPath);
    }

    /**
     * 停止服务
     */
    @Override
    public void stop() {
        this.httpServer.stop();
        log.info("MacaqueServer stopped");
    }

    /**
     * 构建http接口
     *
     * @return http接口封装
     */
    private List<RequestHandler> buildInterface() {
        return Arrays.asList(
                new ClassHotSwapRequestHandler(this.serverConfig),
                new JpsRequestHandler()
        );
    }
}
