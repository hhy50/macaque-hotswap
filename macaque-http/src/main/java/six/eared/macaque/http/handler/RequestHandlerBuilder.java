package six.eared.macaque.http.handler;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;
import six.eared.macaque.http.annotitions.Path;

import java.util.function.BiFunction;

public class RequestHandlerBuilder {

    private final Flux<RequestHandler> requestHandlers;

    public RequestHandlerBuilder(Flux<RequestHandler> requestHandlers) {
        this.requestHandlers = requestHandlers;
    }

    @SuppressWarnings("unchecked")
    public void buildRouters(HttpServerRoutes router) {
        requestHandlers.subscribe(requestHandler -> {
            Path path = requestHandler.getClass().getAnnotation(Path.class);
            if (path != null) {
                BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> handler =
                        (request, response) -> {
                            return requestHandler.process(request, response);
                        };
                router.post(path.value(), handler);
                router.get(path.value(), handler);
            }
        });
    }

}
