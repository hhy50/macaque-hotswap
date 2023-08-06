package six.eared.macaque.server.service;

import six.eared.macaque.server.config.ServerConfig;

/**
 * 默认服务工厂
 */
public class DefaultServiceFactoryImpl implements ServiceFactory {

    public DefaultServiceFactoryImpl() {

    }

    /**
     * 创建服务
     *
     * @param serverMode   是否是服务端模式
     * @param serverConfig 服务配置
     * @return 服务
     */
    @Override
    public MacaqueService newService(boolean serverMode, ServerConfig serverConfig) {
        // 根据serverMode创建不同的服务
        // serverMode为true时，创建MacaqueServer
        // serverMode为false时，创建MacaqueConsole
        return serverMode ? new MacaqueServer(serverConfig)
                : new MacaqueConsole(serverConfig);
    }
}
