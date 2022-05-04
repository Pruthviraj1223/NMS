package com.mindarray;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;

public class DatabaseEngine extends AbstractVerticle {

    boolean checkIp(JsonObject jsonObject) throws SQLException, ClassNotFoundException {

        Connection con = null;

        boolean ans = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/NMS", "root", "password");

            String query = "select * from Discovery where ip='" + jsonObject.getString("ip") + "'";

            ResultSet resultSet = con.createStatement().executeQuery(query);

            ans = resultSet.next();

        } catch (SQLException | ClassNotFoundException e){

        }
        finally {
            if(con!=null){
                con.close();
            }
        }

        return ans;

    }

     JsonObject insert(JsonObject jsonObject) throws SQLException, ClassNotFoundException {

        Connection con = null;

        JsonObject result = new JsonObject();

        try {

            if (!checkIp(jsonObject)) {

                Class.forName("com.mysql.cj.jdbc.Driver");

                con = DriverManager.getConnection("jdbc:mysql://localhost:3306/NMS", "root", "password");

                PreparedStatement preparedStatement = con.prepareStatement("insert into Discovery (port,name,password,community,version,ip,metricType) values (?,?,?,?,?,?,?)");

                preparedStatement.setInt(1, jsonObject.getInteger("port"));

                preparedStatement.setString(2, jsonObject.getString("name"));

                preparedStatement.setString(3, jsonObject.getString("password"));

                preparedStatement.setString(4, jsonObject.getString("community"));

                preparedStatement.setString(5, jsonObject.getString("version"));

                preparedStatement.setString(6, jsonObject.getString("ip"));

                preparedStatement.setString(7, jsonObject.getString("metricType"));

                preparedStatement.executeUpdate();

                result.put("Insertion","Success");

            } else {

                result.put("Insertion","Already exist");

            }
        }catch (SQLException | ClassNotFoundException e){
                result.put("error",e.getMessage());
        }

        finally {
            if(con!=null){
                con.close();
            }
        }
        return result;
     }


    void createTable() throws ClassNotFoundException, SQLException {

        Class.forName("com.mysql.cj.jdbc.Driver");

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/NMS", "root", "password");

        Statement stmt = con.createStatement();

        DatabaseMetaData dbm = con.getMetaData();

        ResultSet tables = dbm.getTables(null, null, "Discovery", null);

        ResultSet resultSet = dbm.getTables(null, null, "Metric", null);

        if (!tables.next()) {

            stmt.executeUpdate("create table Discovery (port int,ip varchar(255),name varchar(255),password varchar(255),community varchar(255),version varchar(255),metricType varchar(255))");

        }

        if (!resultSet.next()) {

            int a = stmt.executeUpdate("create table Metric (metricType varchar(255),counter varchar(255),time int)");

            HashMap<String, Integer> deviceAndPort = new HashMap<>();

            deviceAndPort.put("linux", 22);

            deviceAndPort.put("windows", 5985);

            HashMap<String, Integer> counterTime = new HashMap<>();

            counterTime.put("CPU", 60000);

            counterTime.put("Disk", 120000);

            counterTime.put("Process", 40000);

            counterTime.put("Memory", 50000);

            counterTime.put("SystemInfo", 200000);

            for (String key : deviceAndPort.keySet()) {

                PreparedStatement preparedStatement = con.prepareStatement("insert into Metric (metricType,counter,time) values (?,?,?)");

                for (String counter : counterTime.keySet()) {

                    int time = counterTime.get(counter);

                    preparedStatement.setString(1, key);

                    preparedStatement.setString(2, counter);

                    preparedStatement.setInt(3, time);

                    int aa = preparedStatement.executeUpdate();

                }

            }

            PreparedStatement preparedStatement = con.prepareStatement("insert into Metric (metricType,counter,time) values (?,?,?)");

            preparedStatement.setString(1,"networking");

            preparedStatement.setString(2,"systemInfo");

            preparedStatement.setInt(3,100000);

            preparedStatement.executeUpdate();

            preparedStatement.setString(1,"networking");

            preparedStatement.setString(2,"Interface");

            preparedStatement.setInt(3,20000);

            preparedStatement.executeUpdate();


        }
    }


    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        createTable();

        vertx.eventBus().consumer(Constants.DATABASE_CHECKIP,reply->{

            JsonObject userData = (JsonObject) reply.body();


                vertx.executeBlocking(handler -> {

                    JsonObject check = new JsonObject();

                    boolean ans = false;

                    try {
                        ans = checkIp(userData);

                    } catch (SQLException | ClassNotFoundException e) {
                        check.put("error",e.getMessage());
                    }

                    if (ans) {

                        check.put("status", "Already discovered");

                        reply.reply(check);

                    } else {

                        check.put("status", "Not discovered");

                        reply.reply(check);

                    }

                });

        });


        vertx.eventBus().consumer(Constants.DATABASE_INSERT,handler->{

            JsonObject jsonObject = new JsonObject(handler.body().toString());

            vertx.executeBlocking(req->{

                JsonObject result;

                try {

                    result = insert(jsonObject);

                    handler.reply(result);

                } catch (SQLException | ClassNotFoundException e) {

                    throw new RuntimeException(e);

                }

            });


        });

        startPromise.complete();
    }
}
