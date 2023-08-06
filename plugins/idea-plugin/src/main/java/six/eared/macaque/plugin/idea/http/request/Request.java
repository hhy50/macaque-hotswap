package six.eared.macaque.plugin.idea.http.request;

import java.util.function.Consumer;

public interface Request<T> {

    /**
     * @param consumer
     */
    public void execute(Consumer<T> consumer) throws Exception;
}
