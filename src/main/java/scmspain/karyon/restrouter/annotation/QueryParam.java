package scmspain.karyon.restrouter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryParam {
  public static final String DEFAULT_VALUE = "";

  String value();

  String defaultValue() default DEFAULT_VALUE;

  boolean required() default false;
}
