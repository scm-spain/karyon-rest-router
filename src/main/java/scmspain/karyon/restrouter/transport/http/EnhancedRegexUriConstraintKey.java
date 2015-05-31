package scmspain.karyon.restrouter.transport.http;

import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import netflix.karyon.transport.http.HttpKeyEvaluationContext;
import netflix.karyon.transport.http.RegexUriConstraintKey;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by victor.caldentey on 31/5/15.
 */
public class EnhancedRegexUriConstraintKey<I> extends RegexUriConstraintKey<I> {
    private String verb;
    public EnhancedRegexUriConstraintKey(String constraint, String verb) {
        super(constraint);
        if (null == verb) {
            throw new NullPointerException("Verb can not be null.");
        }
        this.verb = verb;
    }

    @Override
    public String toString() {
        return "EnhancedRegexUriConstraintKey{verb=" + verb + '}' ;
    }

    @Override
    public boolean apply(HttpServerRequest<I> request, HttpKeyEvaluationContext context) {
        if( super.apply(request, context) ){
            return request.getHttpMethod().name().equals(this.verb);
        }
        return false;
    }
}
