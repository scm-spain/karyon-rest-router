package scmspain.karyon.restrouter.transport.http;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import junit.framework.TestCase;
import netflix.karyon.transport.http.HttpKeyEvaluationContext;
import netflix.karyon.transport.http.QueryStringDecoder;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by victor.caldentey on 1/6/15.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpKeyEvaluationContext.class})

public class EnhancedRegexUriConstraintKeyTest extends TestCase {


    private EnhancedRegexUriConstraintKey eRegexUri;
    private HttpServerRequest mockRequest;
    private HttpKeyEvaluationContext mockContext;
    private HttpMethod mockMethod;
    private QueryStringDecoder mockQueryStringDecoder;

    @Before
    public void setUp(){

        this.mockRequest = PowerMockito.mock(HttpServerRequest.class);
        this.mockContext = PowerMockito.spy(Mockito.mock(HttpKeyEvaluationContext.class));
        this.mockMethod = Mockito.mock(HttpMethod.class);
        this.mockQueryStringDecoder = Mockito.mock(QueryStringDecoder.class);
    }

    public void testApply() throws Exception {

        String regexEnd = "\\/?$";
        String constraint = "/campaigns/([^/]+)\\/?$";
        String verb = "GET";
        Mockito.when(this.mockRequest.getUri()).thenReturn("/campaigns/1/");
        Mockito.when(this.mockRequest.getHttpMethod()).thenReturn(this.mockMethod);
        Mockito.when(this.mockMethod.name()).thenReturn("GET");
        //PowerMockito.doReturn(mockQueryStringDecoder).when(this.mockContext,"getOrCreateQueryStringDecoder",mockRequest);
        //PowerMockito.doReturn("/campaigns/1/").when(this.mockContext, "getRequestUriPath",mockRequest);


        //this.eRegexUri = new EnhancedRegexUriConstraintKey(constraint,verb);

        //assertTrue(this.eRegexUri.apply(this.mockRequest, this.mockContext));


    }
}