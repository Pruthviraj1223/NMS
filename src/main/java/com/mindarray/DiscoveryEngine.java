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

        vertx.eventBus().consumer(Constants.DISCOVERY_ADDRESS,handler->{

            JsonObject userData = new JsonObject(handler.body().toString());

            JsonObject result = Utils.validation(userData);

            if(!result.containsKey("error")){

                vertx.eventBus().request(Constants.DATABASE_CHECKIP,userData,req->{

                    if(req.succeeded()){

                        JsonObject check = new JsonObject(req.result().body().toString());

                        if(check.getString("status").equalsIgnoreCase("Not discovered")){

                            vertx.<JsonObject>executeBlocking(res->{
                                try {

                                    JsonObject ping = Utils.ping(userData);

                                    if(ping.getString("ping").equalsIgnoreCase("success")){

                                        JsonObject resultData = Utils.plugin(userData);

                                        if(resultData.getString("status").equalsIgnoreCase("success")){

                                            res.complete(userData);

                                        }else{

                                            res.fail(resultData.toString());

                                        }

                                    }else{

                                        res.fail(ping.toString());
                                    }

                                } catch (IOException e) {

                                    throw new RuntimeException(e);

                                }

                            },false,resHandler ->
                            {

                                if(resHandler.succeeded()) {

                                    JsonObject user = resHandler.result();

                                    vertx.eventBus().request(Constants.DATABASE_INSERT, user, data -> {

                                        JsonObject outcome = (JsonObject) data.result().body();

                                        handler.reply(outcome);


                                    });

                                }else{

                                    handler.reply(resHandler.cause().toString());

                                }
                            });

                        }else{

                            handler.reply(check);

                        }

                    }else{

                        handler.reply(req.cause().toString());

                    }
                });

            }else{

                handler.reply(result);

            }

        });

        startPromise.complete();
    }
}
