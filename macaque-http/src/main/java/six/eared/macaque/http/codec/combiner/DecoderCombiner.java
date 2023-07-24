package six.eared.macaque.http.codec.combiner;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import six.eared.macaque.common.util.CollectionUtil;
import six.eared.macaque.common.util.ReflectUtil;
import six.eared.macaque.http.codec.decoder.BaseDecoder;
import six.eared.macaque.http.codec.decoder.Decoder;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class DecoderCombiner<Req> extends BaseDecoder<Req> {

    protected final LinkedList<Decoder<Req>> head;

    private DecoderCombiner(LinkedList<Decoder<Req>> head) {
        this.head = head;
    }

    @Override
    public Mono<Req> decode(HttpServerRequest request, Class<Req> reqType) {
        return Flux.fromIterable(head)
                .map(decoder -> decoder.decode(request, reqType))
                .flatMap(item -> item)
                .collectList()
                .map(reqs -> mergeMultiEntry(reqs, reqType));
    }

    private static <T> T mergeMultiEntry(List<T> objs, Class<T> type) {
        if (CollectionUtil.isNotEmpty(objs)) {
            if (objs.size() == 1) {
                return objs.get(0);
            }

            Field[] fields = ReflectUtil.getFields(type);
            try {
                T obj = objs.get(0);
                for (Field field : fields) {
                    if (ReflectUtil.getFieldValue(obj, field) == null) {
                        for (int i = 1; i < objs.size(); i++) {
                            Object fv = ReflectUtil.getFieldValue(objs.get(i), field);
                            if (fv == null) continue;
                            ReflectUtil.setFieldValue(obj, field, fv);
                        }
                    }
                }
                return obj;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static <Req> Builder<Req> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        protected final LinkedList<Decoder<T>> head = new LinkedList<>();

        public Builder<T> next(Decoder<T> decoder) {
            head.addLast(decoder);
            return this;
        }

        public DecoderCombiner<T> build() {
            return new DecoderCombiner<>(head);
        }
    }
}
