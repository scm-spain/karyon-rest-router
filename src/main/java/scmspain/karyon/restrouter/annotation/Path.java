package scmspain.karyon.restrouter.annotation;


import io.netty.handler.codec.http.HttpMethod;
import scmspain.karyon.restrouter.auth.AuthenticationService;
import scmspain.karyon.restrouter.auth.NoAuthenticationServiceImpl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by ramonriusgrasset on 17/02/15.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Path {

    String value();

    String method();

    Class<? extends AuthenticationService> authentication() default NoAuthenticationServiceImpl.class;

}
