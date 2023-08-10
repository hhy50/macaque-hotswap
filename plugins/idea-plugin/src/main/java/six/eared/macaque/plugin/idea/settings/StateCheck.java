package six.eared.macaque.plugin.idea.settings;

import com.intellij.util.ReflectionUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

public interface StateCheck {

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Required {

    }


    default boolean checkRequired() {
        Field[] declaredFields = this.getClass().getDeclaredFields();

        for (Field field : declaredFields) {
            Required required = field.getDeclaredAnnotation(Required.class);
            if (required != null) {
                Object fieldValue = ReflectionUtil.getFieldValue(field, this);
                if (fieldValue == null) {
                    return false;
                }
            }
        }
        return true;
    }
}
