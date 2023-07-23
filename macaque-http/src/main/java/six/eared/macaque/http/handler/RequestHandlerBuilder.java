package six.eared.macaque.http.handler;

import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;
import six.eared.macaque.http.annotitions.Path;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class RequestHandlerBuilder {

    private final String rootPath;
    private final List<RequestHandler> requestHandlers;

    public RequestHandlerBuilder(String rootPath, List<RequestHandler> requestHandlers) {
        this.rootPath = rootPath;
        this.requestHandlers = requestHandlers;
    }

    @SuppressWarnings("unchecked")
    public void buildRouters(HttpServerRoutes router) {
        requestHandlers.forEach(requestHandler -> {
            Path path = requestHandler.getClass().getAnnotation(Path.class);
            if (path != null) {
                String uri = formatUrl(rootPath, path.value());
                BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> handler =
                        (request, response) -> {
                            return requestHandler.process(request, response);
                        };
                router.post(uri, handler);
                router.get(uri, handler);
            }
        });
    }

    public String formatUrl(String... url) {
        return Arrays.stream(url).collect(Collectors.joining("/"))
                .replaceAll("/+", "/");
    }

}
