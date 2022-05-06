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

                vertx.eventBus().request(Constants.DATABASE_CHECKIP,userData,response->{

                    if(response.succeeded()){

                        JsonObject check = (JsonObject) response.result().body();

                        if(check.getString("status").equalsIgnoreCase("Not discovered")){

                            vertx.<JsonObject>executeBlocking(request -> {
                                try {

                                    JsonObject ping = Utils.ping(userData);

                                    if(ping.getString("ping").equalsIgnoreCase("success")){

                                        JsonObject resultData = Utils.plugin(userData);

                                        if(resultData.getString("status").equalsIgnoreCase("success")){

                                            request.complete(userData);

                                        }else{

                                            request.fail(resultData.toString());

                                        }

                                    }else{

                                        request.fail(ping.toString());
                                    }

                                } catch (IOException ioException) {

                                    LOG.debug("Error : {} ", ioException.getMessage());

                                }

                            },false,resHandler ->
                            {

                                if(resHandler.succeeded()) {

                                    JsonObject user = resHandler.result();

                                    vertx.eventBus().request(Constants.DATABASE_INSERT, user, data -> {

                                        if(data.succeeded()){

                                            if(data.result().body()!=null){

                                                JsonObject outcome = (JsonObject) data.result().body();

                                                handler.reply(outcome);

                                            }else{

                                                handler.reply(new JsonObject().put("status","null response"));

                                            }



                                        }else{

                                            handler.reply(data.cause().toString());

                                        }

                                    });

                                }else{

                                    handler.reply(resHandler.cause().toString());

                                }

                            });

                        }else{

                            handler.reply(check);

                        }

                    }else{

                        handler.reply(response.cause().toString());

                    }

                });

            }else{

                handler.reply(result);

            }

        });

        startPromise.complete();
    }
}
