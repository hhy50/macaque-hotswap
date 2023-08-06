package six.eared.macaque.plugin.idea.common;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class GenericTyped<T> {

    @SuppressWarnings("unchecked")
    protected Class<T> getType() {
        ParameterizedType superGenericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type[] types = superGenericSuperclass.getActualTypeArguments();
        try {
            return (Class<T>) Class.forName(types[0].getTypeName());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
