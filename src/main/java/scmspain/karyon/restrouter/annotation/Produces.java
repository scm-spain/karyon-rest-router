package scmspain.karyon.restrouter.annotation;

import scmspain.karyon.restrouter.serializer.Configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotation for {@link Path} methods, that will set the list of allowed media types to be
 * serialized.
 * It will fail during initialization if the media types configured using this
 * annotation doesn't have an appropriate serializer.
 * {@link scmspain.karyon.restrouter.KaryonRestRouterModule#setConfiguration(Configuration)}
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Produces {
  /**
   * @return List of media types
   */
  String[] value() default {};
}
