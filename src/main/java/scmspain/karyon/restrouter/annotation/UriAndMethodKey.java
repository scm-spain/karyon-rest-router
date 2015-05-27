package scmspain.karyon.restrouter.annotation;

import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import netflix.karyon.transport.http.HttpInterceptorKey;
import netflix.karyon.transport.http.HttpKeyEvaluationContext;
import netflix.karyon.transport.http.ServletStyleUriConstraintKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ramonriusgrasset on 18/02/15.
 */
public class UriAndMethodKey <I> implements HttpInterceptorKey<I> {

    private static final Logger logger = LoggerFactory.getLogger(UriAndMethodKey.class);

    private final HttpMethod method;
    private final ServletStyleUriConstraintKey uri;


    public UriAndMethodKey(String uri, HttpMethod method) {
        if (null == method) {
            throw new NullPointerException("HTTP method in the interceptor constraint can not be null.");
        }
        if (null == uri) {
            throw new NullPointerException("URI in the interceptor constraint can not be null.");
        }
        this.uri = new ServletStyleUriConstraintKey(uri, null);
        this.method = method;
    }

    @Override
    public String toString() {
        return "UriAndMethodKey{uri: " + uri + "method=" + method + '}';
    }

    @Override
    public boolean apply(HttpServerRequest<I> request, HttpKeyEvaluationContext context) {
        boolean matches = request.getHttpMethod().equals(method);
        if (logger.isDebugEnabled()) {
            logger.debug("Result for HTTP method constraint for method {} and required method {} : {}",
                    request.getHttpMethod(), method, matches);
        }

        return matches && this.uri.apply(request, context);
    }
}
