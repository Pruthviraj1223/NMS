package com.mindarray;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DiscoveryEngine extends AbstractVerticle {

    final Logger LOG = LoggerFactory.getLogger(DiscoveryEngine.class.getName());

    @Override
    public void start(Promise<Void> startPromise)  {

        vertx.eventBus().consumer(Constants.DISCOVERYADDRESS,handler->{

            JsonObject userData = new JsonObject(handler.body().toString());

            JsonObject result = Utils.validation(userData);

            if(!result.containsKey("error")){

                vertx.eventBus().request(Constants.DATABASECHECKIP,userData,req->{

                    if(req.succeeded()){

                        JsonObject check = new JsonObject(req.result().body().toString());

                        if(!check.containsKey("error")){

                            vertx.<JsonObject>executeBlocking(res->{
                                try {

                                    Utils.ping(userData);

                                    // if successfull

                                    Utils.plugin(userData);

                                    res.complete(userData);

                                } catch (IOException e) {

                                    throw new RuntimeException(e);
                                }
                            },false,resHandler ->
                            {



                            }));



                        }else{
                            handler.reply(check);
                        }

                    }else{
                        handler.reply(req.result().body();

                    }
                });


            }else{

                handler.reply(result);

            }

        });

        startPromise.complete();
    }
}
