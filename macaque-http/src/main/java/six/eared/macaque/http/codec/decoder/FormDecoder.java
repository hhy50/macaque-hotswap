package six.eared.macaque.http.codec.decoder;

import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import six.eared.macaque.common.util.Pair;
import six.eared.macaque.common.util.ReflectUtil;
import six.eared.macaque.http.request.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;

public class FormDecoder<Req> extends BaseDecoder<Req> {

    @Override
    public Mono<Req> decode(HttpServerRequest request, Class<Req> reqType) {
        if (request.method() == HttpMethod.POST) {
            return request.receiveForm(builder -> builder.maxInMemorySize(256))
                    .map(item -> {
                        try {
                            return new Pair<>(item.getName(), item.get());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collectList()
                    .map((list) -> {
                        Req reqObj = newReqObject(reqType);
                        loop:
                        for (Field field : ReflectUtil.getFields(reqType)) {
                            for (Pair<String, byte[]> params : list) {
                                if (params.getFirst().equals(field.getName())) {
                                    if (field.getType() == MultipartFile.class) {
                                        ReflectUtil.setFieldValue(reqObj, field, new MultipartFile(params.getSecond()));
                                    } else {
                                        ReflectUtil.setFieldValue(reqObj, field, new String(params.getSecond()));
                                    }
                                    continue loop;
                                }
                            }
                        }
                        return reqObj;
                    });
        }
        return Mono.empty();
    }
}
