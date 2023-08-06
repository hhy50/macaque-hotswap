package six.eared.macaque.server.service;

import six.eared.macaque.server.config.ServerConfig;

public interface ServiceFactory {

    /**
     * 创建服务
     *
     * @param serverMode   是否是服务端模式
     * @param serverConfig 服务配置
     * @return 服务
     */
    MacaqueService newService(boolean serverMode, ServerConfig serverConfig);
}
