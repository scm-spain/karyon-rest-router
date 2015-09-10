package scmspain.karyon.restrouter.core;

import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Singleton
public class ParamTypeResolver<T> {

  private Map<Class, Function<String, T>> resolver= new HashMap<>();

  ParamTypeResolver() {

    resolver.put(int.class, (val) -> (T) Integer.valueOf(val));
    resolver.put(double.class, (val) -> (T) Double.valueOf(val));
    resolver.put(boolean.class, (val) -> (T) Boolean.valueOf(val));
    resolver.put(long.class, (val) -> (T) Long.valueOf(val));
    resolver.put(short.class, (val) -> (T) Short.valueOf(val));
    resolver.put(float.class, (val) -> (T) Float.valueOf(val));

    resolver.put(String.class, (val) -> (T) val);
    resolver.put(Integer.class, (val) -> (T) Integer.valueOf(val));
    resolver.put(Double.class, (val) -> (T) Double.valueOf(val));
    resolver.put(Boolean.class, (val) -> (T) Boolean.valueOf(val));
    resolver.put(Long.class, (val) -> (T) Long.valueOf(val));
    resolver.put(Short.class, (val) -> (T) Short.valueOf(val));
    resolver.put(Float.class, (val) -> (T) Float.valueOf(val));
  }

  public T resolveValueType(Class<T> classType, String value){

    return Optional
      .ofNullable(resolver.get(classType))
      .map(casting -> casting.apply(value))
      .orElseThrow(() -> new IllegalArgumentException(classType.getName() + " is not supported."));
  }

}
