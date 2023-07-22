package six.eared.macaque.http.annotitions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Path {

    String value() default "";

    RequestMethod method() default RequestMethod.POST;
}
