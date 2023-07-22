package six.eared.macaque.http.handler;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.netty.http.server.HttpServerRoutes;
import six.eared.macaque.http.annotitions.Path;

public class RequestHandlerBuilder {


    private final Flux<RequestHandler<?>> requestHandlers;

    public RequestHandlerBuilder(Flux<RequestHandler<?>> requestHandlers) {
        this.requestHandlers = requestHandlers;
    }

    @SuppressWarnings("unchecked")
    public void buildRouters(HttpServerRoutes router) {
        requestHandlers.subscribe(requestHandler -> {
            Path path = requestHandler.getClass().getAnnotation(Path.class);
            if (path != null) {
                router.post(path.value(), (request, response) -> {
                    return (Publisher) requestHandler.process(request, response);
                });
                router.get(path.value(), (request, response) -> {
                    return (Publisher) requestHandler.process(request, response);
                });
            }
        });
    }
}
