# karyon-rest-router
Module for manage endpoint REST in Karyon framework

## Setting package name to find endpoints REST

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
