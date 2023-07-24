package six.eared.macaque.http.codec.decoder;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.HttpData;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import six.eared.macaque.common.util.ReflectUtil;
import six.eared.macaque.http.request.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;

public class FormDecoder<Req> extends BaseDecoder<Req> {

    @Override
    public Mono<Req> decode(HttpServerRequest request, Class<Req> reqType) {
        if (request.method() == HttpMethod.POST) {
            return request.receiveForm()
                    .collectList()
                    .handle((list, sink) -> {
                        Req reqObj = newReqObject(reqType);
                        loop:
                        for (Field field : ReflectUtil.getFields(reqType)) {
                            for (HttpData httpData : list) {
                                if (httpData.getName().equals(field.getName())) {
                                    try {
                                        if (field.getType() == MultipartFile.class) {
                                            ReflectUtil.setFieldValue(reqObj, field, new MultipartFile(httpData.get()));
                                        } else {
                                            ReflectUtil.setFieldValue(reqObj, field, httpData.getString());
                                        }
                                        continue loop;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        sink.next(reqObj);
                    });
        }
        return Mono.empty();
    }
}
