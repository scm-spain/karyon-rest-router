package scmspain.karyon.restrouter.core;

import com.google.inject.Singleton;
import scmspain.karyon.restrouter.exception.UnsupportedFormatException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Singleton
public class ParamTypeResolver<T> {

  private Map<Class, Function<String, T>> resolver = new HashMap<Class, Function<String, T>>() {{
    put(int.class, (val) -> (T) Integer.valueOf(val));
    put(double.class, (val) -> (T) Double.valueOf(val));
    put(boolean.class, (val) -> (T) Boolean.valueOf(val));
    put(long.class, (val) -> (T) Long.valueOf(val));
    put(short.class, (val) -> (T) Short.valueOf(val));
    put(float.class, (val) -> (T) Float.valueOf(val));

    put(String.class, (val) -> (T) val);
    put(Integer.class, (val) -> (T) Integer.valueOf(val));
    put(Double.class, (val) -> (T) Double.valueOf(val));
    put(Boolean.class, (val) -> (T) Boolean.valueOf(val));
    put(Long.class, (val) -> (T) Long.valueOf(val));
    put(Short.class, (val) -> (T) Short.valueOf(val));
    put(Float.class, (val) -> (T) Float.valueOf(val));
  }};

  public T resolveValueType(Class<T> classType, String value) throws UnsupportedFormatException {

    return Optional
      .ofNullable(resolver.get(classType))
      .map(casting -> casting.apply(value))
      .orElseThrow(() -> new UnsupportedFormatException(classType.getName()));
  }

}
