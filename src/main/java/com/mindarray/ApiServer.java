package com.mindarray;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiServer extends AbstractVerticle {

    static final Logger LOG = LoggerFactory.getLogger(ApiServer.class.getName());

    @Override
    public void start(Promise<Void> startPromise)  {

        Router router = Router.router(vertx);

        HttpServer httpServer = vertx.createHttpServer();

        router.route().handler(BodyHandler.create());

        router.post(Constants.DISCOVERY). handler(handler->{

            JsonObject userData = handler.getBodyAsJson();

            vertx.eventBus().request(Constants.DISCOVERYADDRESS,userData,response->{

                if(response.succeeded()) {

                    handler.response().end(response.result().body().toString());

                }else{

                    handler.response().end("Failed");

                }

            });

        });

        httpServer.requestHandler(router).listen(8080).onComplete(handler->{

            if(handler.succeeded()){

                LOG.debug("Server created ..");

            }else{

                LOG.debug("Server failed ..");

            }

        });

        startPromise.complete();

    }
}
