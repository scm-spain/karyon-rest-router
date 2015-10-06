package scmspain.karyon.restrouter.serializer;

import rx.Observable;

import java.util.List;

public interface Serializer {

  Observable<Void> serialize(Object obj, String contentType);

  List<String> getSupportedContentTypes();
}
