package scmspain.karyon.restrouter.core;

import com.google.inject.Singleton;
import org.reflections.Reflections;

import java.util.Set;

@Singleton
public class ResourceLoader {

  public Set<Class<?>> find(String packageName, Class annotation) {
    Reflections reflections = new Reflections(packageName);
    return reflections.getTypesAnnotatedWith(annotation);
  }
}
