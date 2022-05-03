package com.mindarray;

import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

public class Utils {

    public static JsonObject validation(JsonObject jsonObject){

        JsonObject result = new JsonObject();

        ArrayList<String> listOfErrors = new ArrayList<>();

        if(!jsonObject.containsKey("metricType")){
            listOfErrors.add("metricType field is not available");
        }
        if(!jsonObject.containsKey("ip")){
            listOfErrors.add("ip is not available");
        }
        if(!jsonObject.containsKey("name")){
            listOfErrors.add("name is not available");
        }
        if(!jsonObject.containsKey("password")){
            listOfErrors.add("password is not available");
        }

        if(listOfErrors.isEmpty()) {
            result.put("status", "success");
        }else {
            result.put("status", "fail");
            result.put("error", listOfErrors);
        }
        return result;

    }

    public static JsonObject checkPort(){

        return new JsonObject();
    }

    public static JsonObject ping(JsonObject jsonObject) throws IOException {

        JsonObject error = new JsonObject();

        ArrayList<String> commands = new ArrayList<>();

        commands.add("fping");

        commands.add("-q");

        commands.add("-c");

        commands.add("3");

        commands.add(jsonObject.getString("ip"));

        ProcessBuilder processBuilder = new ProcessBuilder(commands);

        processBuilder.redirectErrorStream(true); // It must be before the staring of process

        Process process;

        try {

            process = processBuilder.start();

        } catch (IOException e) {

            throw new RuntimeException(e);
        }

        InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());

        var bufferedReader = new BufferedReader(inputStreamReader);

        String output;

        output = bufferedReader.readLine();

        String []arr = output.split(":")[1].split("=")[1].split(",")[0].split("/");

        String loss = arr[2].substring(0,arr[2].length()-1);

        boolean ans =  loss.equalsIgnoreCase("0");

        if(ans){
            error.put("status","success");
        }else{
            error.put("status","fail");
        }

        return error;

    }

    public static JsonObject plugin(JsonObject jsonObject){

        return new JsonObject();
    }

}
