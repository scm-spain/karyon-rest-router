package scmspain.karyon.restrouter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryParam {
    public static final String DEFAULT_VALUE =  "\\n\\t\\t\\n\\t\\t\\n\\ue000\\ue001\\ue002\\n\\t\\t\\t\\t\\n";
    String value();

    String defaultValue() default DEFAULT_VALUE;

    boolean required() default false;
}
