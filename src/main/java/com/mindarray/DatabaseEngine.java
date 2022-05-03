package com.mindarray;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseEngine extends AbstractVerticle {

    boolean checkIp(JsonObject jsonObject) throws SQLException, ClassNotFoundException {

        Class.forName("com.mysql.cj.jdbc.Driver");

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/NMS", "root", "password");

        String query = "select * from Discovery where ip='" + jsonObject.getString("ip") + "'";

        ResultSet resultSet = con.createStatement().executeQuery(query);

        boolean ans = resultSet.next();

        con.close();

        return ans;

    }


    @Override
    public void start(Promise<Void> startPromise) throws Exception {


        vertx.eventBus().consumer("database.checkIp",reply->{

            JsonObject userData = (JsonObject) reply.body();

            try {

                boolean ans = checkIp(userData);

                JsonObject check = new JsonObject();

                if (ans) {

                    check.put("error","Already discovered");

                    reply.reply(check);

                }else{

                    reply.reply(check);

                }

            } catch (SQLException | ClassNotFoundException e) {
                reply.reply(e.getMessage());
            }


        });

        startPromise.complete();
    }
}
