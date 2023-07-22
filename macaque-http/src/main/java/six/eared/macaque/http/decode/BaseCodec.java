package six.eared.macaque.http.decode;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class BaseCodec<Req, Res> implements Codec<Req, Res> {

    @SuppressWarnings("unchecked")
    protected Class<Req> getReqType() {
        ParameterizedType superGenericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type[] types = superGenericSuperclass.getActualTypeArguments();
        try {
            return (Class<Req>) Class.forName(types[0].getTypeName());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected Class<Res> getResType() {
        ParameterizedType superGenericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type[] types = superGenericSuperclass.getActualTypeArguments();
        try {
            return (Class<Res>) Class.forName(types[1].getTypeName());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
