package com.mindarray;


import io.vertx.core.json.JsonObject;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import java.io.BufferedReader;

import java.io.IOException;

import java.io.InputStreamReader;

import java.util.ArrayList;

import java.util.Base64;


public class Utils {

    static final Logger LOG = LoggerFactory.getLogger(Utils.class.getName());


    public static JsonObject validation(JsonObject userData){

        JsonObject result = new JsonObject();

        ArrayList<String> listOfErrors = new ArrayList<>();





        if(userData.containsKey(Constants.METRIC_TYPE)){

            if(userData.getString(Constants.METRIC_TYPE).isEmpty()){

                listOfErrors.add("metric type is empty");

            }
            else{

                userData.put(Constants.METRIC_TYPE,userData.getString(Constants.METRIC_TYPE).trim());

                if(userData.getString(Constants.METRIC_TYPE).equalsIgnoreCase("linux") || userData.getString(Constants.METRIC_TYPE).equalsIgnoreCase("windows")) {

                    if(!userData.containsKey(Constants.NAME)) {

                        listOfErrors.add("name is not available");

                    }else{

                        userData.put(Constants.NAME,userData.getString(Constants.NAME).trim());

                        if(userData.getString(Constants.NAME).isEmpty()){

                            listOfErrors.add("name is empty");

                        }

                    }

                    if(!userData.containsKey(Constants.PASSWORD)){

                        listOfErrors.add("password is not available");

                    }else{

                        userData.put(Constants.PASSWORD,userData.getString(Constants.PASSWORD).trim());

                        if(userData.getString(Constants.PASSWORD).isEmpty()){

                            listOfErrors.add("password is empty");

                        }

                    }
                }

                else if(userData.getString(Constants.METRIC_TYPE).equalsIgnoreCase("networking") ){

                    if(!userData.containsKey(Constants.COMMUNITY)) {

                        listOfErrors.add("community is not available");

                    }else{

                        userData.put(Constants.COMMUNITY,userData.getString(Constants.COMMUNITY).trim());

                        if(userData.getString(Constants.COMMUNITY).isEmpty()){

                            listOfErrors.add("community is empty");

                        }

                    }

                    if(!userData.containsKey(Constants.VERSION)){

                        listOfErrors.add("version is not available");

                    }else{

                        userData.put(Constants.VERSION,userData.getString(Constants.VERSION).trim());

                        if(userData.getString(Constants.VERSION).isEmpty()){

                            listOfErrors.add("version is empty");

                        }

                    }

                }
            }

        }

        if(!userData.containsKey(Constants.IP_ADDRESS)){

            listOfErrors.add("ip is not available");

        }
        else{

            userData.put(Constants.IP_ADDRESS,userData.getString(Constants.IP_ADDRESS).trim());

            if(userData.getString(Constants.IP_ADDRESS).isEmpty()){

                listOfErrors.add("ip is empty");

            }

        }

        if(!userData.containsKey(Constants.PORT)){

            listOfErrors.add("port is not available");

        }else{

            if(userData.getString(Constants.PORT).isEmpty()){

                listOfErrors.add("port is empty");

            }

        }


        if(listOfErrors.isEmpty()) {

            result.put(Constants.STATUS, Constants.SUCCESS);

        }else {

            result.put(Constants.STATUS, Constants.FAIL);

            result.put(Constants.ERROR, listOfErrors);

        }

        return result;

    }

    public static JsonObject ping(JsonObject entries) throws IOException {

        JsonObject error = new JsonObject();

        ArrayList<String> commands = new ArrayList<>();

        commands.add("fping");

        commands.add("-q");

        commands.add("-c");

        commands.add("3");

        commands.add(entries.getString(Constants.IP_ADDRESS));

        ProcessBuilder processBuilder = new ProcessBuilder(commands);

        processBuilder.redirectErrorStream(true); // It must be before the staring of process

        Process process = null;

        try {

            process = processBuilder.start();

        } catch (IOException e) {

            LOG.debug("Error : {} ", e.getMessage());

        }

        assert process != null;
        InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());

        var bufferedReader = new BufferedReader(inputStreamReader);

        String output;

        output = bufferedReader.readLine();

        if(output!=null){

            String []packetData = output.split(":")[1].split("=")[1].split(",")[0].split("/");

            String packetLoss = packetData[2].substring(0,packetData[2].length()-1);

            boolean answer =  packetLoss.equalsIgnoreCase("0");

            if(answer){

                error.put(Constants.PING,Constants.SUCCESS);

            }else{

                error.put(Constants.PING,Constants.FAIL);

            }

        }else{

            error.put(Constants.PING,Constants.FAIL);


        }
        return error;


    }

    public static JsonObject plugin(JsonObject userData) throws IOException {

        JsonObject response = null;

        userData.put("category","discovery");

        String encodedString = Base64.getEncoder().encodeToString(userData.toString().getBytes());

        userData.remove("category");

        ProcessBuilder processBuilder = new ProcessBuilder().command("/home/pruthviraj/NMS/plugin.exe",encodedString);

        String output = "";

        BufferedReader bufferedReader = null;

        Process process = null;

        try {

            processBuilder.redirectErrorStream(true);

            process = processBuilder.start();

            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream()); //read the output

            bufferedReader = new BufferedReader(inputStreamReader);

            output = bufferedReader.readLine();

            process.waitFor();

            if (output != null) {

                  response = new JsonObject(output);
                 
                
            }else{

                 response = new JsonObject();

                response.put(Constants.ERROR,"Output is null");

            }


        } catch (Exception exception) {

            LOG.debug("Error : {} " , exception.getMessage());

        }

        finally {

            if(bufferedReader!=null){

                bufferedReader.close();
            }

            if(process!=null){

                process.destroy();

            }

        }


        return response;

    }

}
