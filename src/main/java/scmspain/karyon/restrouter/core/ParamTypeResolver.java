package scmspain.karyon.restrouter.core;

import com.google.inject.Singleton;

@Singleton
public class ParamTypeResolver<T> {


  public T resolveValueType(Class<T> classType, String value){
    if (classType.isAssignableFrom(String.class)) {
      return (T)value;
    } else if (classType.isAssignableFrom(int.class) || classType.isAssignableFrom(Integer.class)) {
      return (T)Integer.valueOf(value);
    } else if (classType.isAssignableFrom(double.class) || classType.isAssignableFrom(Double.class)) {
      return (T)Double.valueOf(value);
    } else if (classType.isAssignableFrom(boolean.class) || classType.isAssignableFrom(Boolean.class)) {
      return (T)Boolean.valueOf(value);
    }
    return null;
  }

}
