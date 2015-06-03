# karyon-rest-router
Module for manage endpoint REST in Karyon framework

## Getting Binaries

Binaries and dependency information for Maven, Ivy, Gradle and others can be found at http://search.maven.org.

Example of Gradle:

```groovy
dependencies {
       compile 'com.scmspain.karyon:karyon-rest-router:1.0'   
}
```

## Using the module in your server

### 1- Include the module

```java
@Modules(include = {
    ShutdownModule.class,
    KaryonWebAdminModule.class,
    KaryonEurekaModule.class,
    AppServer.KaryonRestRouterModuleImpl.class,
    //KaryonServoModule.class
})
```

### 2- Extends KaryonRestRouterModule and override configureServer and configure to put your configurations.

```java
public interface AppServer {
    class KaryonRestRouterModuleImpl extends KaryonRestRouterModule{
```

```java
        @Override
        protected void configureServer() {
            //Configure your server!
            bind(AuthenticationManagerInterface.class).to(AuthenticationManager.class);
            interceptorSupport().forUri("/*").intercept(LoggingInterceptor.class);
            //interceptorSupport().forUri("/*").interceptIn(AuthenticationInterceptor.class);

            int port = properties.getIntProperty("server.port", 8080).get();
            int threads = properties.getIntProperty("server.threads",50).get();
            server().port(port).threadPoolSize(threads);

        }
        @Override
        public void configure()
        {
            //Configure your lifecycle of your objects!
            bind(CampaignRepositoryInterface.class).to(DynamoDBCampaignRepository.class);
            bind(CampaignController.class).asEagerSingleton();
            bind(DynamoDBConfig.class).asEagerSingleton();
            bind(DynamoDBConnector.class).asEagerSingleton();
            bind(GsonService.class).asEagerSingleton();

            super.configure();
        }
```

Finally, you will have something like this:

```java
@ArchaiusBootstrap()
@KaryonBootstrap(name = "AppServer", healthcheck = HealthCheck.class)
@Singleton
@Modules(include = {
    ShutdownModule.class,
    KaryonWebAdminModule.class,
    KaryonEurekaModule.class,
    AppServer.KaryonRestRouterModuleImpl.class,
    //KaryonServoModule.class
})
public interface AppServer {
    class KaryonRestRouterModuleImpl extends KaryonRestRouterModule{

        private DynamicPropertyFactory properties = DynamicPropertyFactory.getInstance();

        @Override
        protected void configureServer() {
            //Configure your server!
            bind(AuthenticationManagerInterface.class).to(AuthenticationManager.class);
            interceptorSupport().forUri("/*").intercept(LoggingInterceptor.class);
            //interceptorSupport().forUri("/*").interceptIn(AuthenticationInterceptor.class);

            int port = properties.getIntProperty("server.port", 8080).get();
            int threads = properties.getIntProperty("server.threads",50).get();
            server().port(port).threadPoolSize(threads);

        }
        @Override
        public void configure()
        {
            //Configure your lifecycle of your objects!
            bind(CampaignRepositoryInterface.class).to(DynamoDBCampaignRepository.class);
            bind(CampaignController.class).asEagerSingleton();
            bind(DynamoDBConfig.class).asEagerSingleton();
            bind(DynamoDBConnector.class).asEagerSingleton();
            bind(GsonService.class).asEagerSingleton();

            super.configure();
        }
    }
}
```

### 3- Setting package name to find endpoints REST

Set this property in your .properties files

```
com.scmspain.karyon.rest.property.packages = com.example.forlayos.minglanillas
```

Example of endpoint and path annotations

```java
package com.example.forlayos.minglanillas;

@Singleton
@Endpoint
public class CampaignController {

    private CampaignRepositoryInterface repository;
    private GsonService gsonService;

    public CampaignController(){

    }

    @Inject
    public CampaignController(CampaignRepositoryInterface repository, GsonService gson)
    {
        this.repository = repository;
        this.gsonService = gson;

    }

    @Path(value = "/campaigns/{id}", method = HttpMethod.GET)
    public Observable<Void> getCampaignsResource(HttpServerResponse<ByteBuf> response, Map<String,String> pathParams) {
        String id = pathParams.get("id");
       //Code...
       
    }
    @Path(value = "/campaigns", method = HttpMethod.POST )
    public Observable<Void> postCampaignsResource(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        
       //Code...
    }
    @Path(value = "/campaigns/{id}", method = HttpMethod.PUT)
    public Observable<Void> putCampaignsResource(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response, Map<String,String> pathParams) {
        String id = pathParams.get("id");
       //Code...
       

    }
    @Path(value = "/campaigns/{id}", method = HttpMethod.DELETE)
    public Observable<Void> deleteCampaignsResource(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response, Map<String,String> pathParams) {

        String id = pathParams.get("id");
       //Code...

    }




}

```
