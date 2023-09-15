package six.eared.macaque.agent.annotation;


import six.eared.macaque.common.type.FileType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HotSwapFileType {

    FileType fileType();
}
