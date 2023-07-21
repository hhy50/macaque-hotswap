package six.eared.macaque.http.annotitions;

public @interface Path {

    String value() default "";

    RequestMethod method() default RequestMethod.POST;
}
