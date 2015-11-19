package scmspain.karyon.restrouter.dummy;

import rx.Observable;
import scmspain.karyon.restrouter.annotation.Endpoint;
import scmspain.karyon.restrouter.annotation.Path;

import javax.ws.rs.HttpMethod;

/**
 * Created by ruben.perez on 16/11/15.
 */
@Endpoint
public class DummyController {

    @Path(value = "/dummy/{id}", method = HttpMethod.GET)
    public Observable<Void> method() {

        return Observable.empty();
    }

}
