package six.eared.macaque.http.handler;

import io.netty.handler.codec.http.HttpHeaders;
import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRoutes;
import six.eared.macaque.http.annotitions.Path;

import java.util.List;
import java.util.function.Consumer;

public class RequestHandlerBuilder {


    @SuppressWarnings("unchecked")
    public Consumer<? super HttpServerRoutes> buildRouters(List<RequestHandler> requestHandlers) {
        return (routers) -> {
            for (RequestHandler<?> requestHandler : requestHandlers) {
                Path[] paths = requestHandler.getClass().getDeclaredAnnotationsByType(Path.class);
                if (paths.length > 0) {
                    routers.post(paths[0].value(), (request, response) -> {
                        return (Publisher) requestHandler.process(request, response);
                    });
                }
            }
        };
    }
}
