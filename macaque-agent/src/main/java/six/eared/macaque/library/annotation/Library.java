package six.eared.macaque.library.annotation;

import six.eared.macaque.library.hook.HotswapHook;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Library {

    String name() default "";

    Class<? extends HotswapHook>[] hooks() default {};
}
