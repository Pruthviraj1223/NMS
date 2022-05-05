package com.mindarray;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;

public class Utils {

    static final Logger LOG = LoggerFactory.getLogger(Utils.class.getName());

    public static JsonObject validation(JsonObject jsonObject){

        JsonObject result = new JsonObject();

        ArrayList<String> listOfErrors = new ArrayList<>();

        if(!jsonObject.containsKey(Constants.METRIC_TYPE)){

            listOfErrors.add("metricType field is not available");

        }

        if(jsonObject.containsKey(Constants.METRIC_TYPE)){

            if(jsonObject.getString(Constants.METRIC_TYPE).equalsIgnoreCase("linux") || jsonObject.getString(Constants.METRIC_TYPE).equalsIgnoreCase("windows")) {

                if(!jsonObject.containsKey(Constants.NAME)) {

                    listOfErrors.add("name is not available");

                }

                if(!jsonObject.containsKey(Constants.PASSWORD)){

                    listOfErrors.add("password is not available");

                }
            }

            else if(jsonObject.getString(Constants.METRIC_TYPE).equalsIgnoreCase("networking") ){

                if(!jsonObject.containsKey(Constants.COMMUNITY)) {

                    listOfErrors.add("community is not available");

                }

                if(!jsonObject.containsKey(Constants.VERSION)){

                    listOfErrors.add("version is not available");

                }

            }

        }

        if(!jsonObject.containsKey(Constants.IP_ADDRESS)){

            listOfErrors.add("ip is not available");

        }

        if(!jsonObject.containsKey(Constants.PORT)){

            listOfErrors.add("port is not available");

        }


        if(listOfErrors.isEmpty()) {

            result.put("status", "success");

        }else {

            result.put("status", "fail");

            result.put("error", listOfErrors);

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

        if(output==null){

            error.put("error","No data from ping");

        }

        assert output != null;
        String []arr = output.split(":")[1].split("=")[1].split(",")[0].split("/");

        String loss = arr[2].substring(0,arr[2].length()-1);

        boolean ans =  loss.equalsIgnoreCase("0");

        if(ans){

            error.put("ping","success");

        }else{

            error.put("ping","fail");

        }

        return error;

    }

    public static JsonObject plugin(JsonObject user){

        JsonObject result = new JsonObject();

        user.put("category","discovery");

        String encoded = Base64.getEncoder().encodeToString(user.toString().getBytes());

        user.remove("category");

        ProcessBuilder processBuilder = new ProcessBuilder().command("/home/pruthviraj/NMS/plugin.exe",encoded);

        String output = "";

        try {

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream()); //read the output

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            output = bufferedReader.readLine();

            process.waitFor();

            bufferedReader.close();

            process.destroy();

        } catch (IOException | InterruptedException e) {

            LOG.debug("Error : {} " + e.getMessage());

        }

        if (output != null) {

            if(output.equalsIgnoreCase("success")){

                result.put("status","success");

            }else{

                result.put("status","fail");

                result.put("error",output);
            }
            return  result;

        }else{

            result.put("error","Output is null");

            return result;

        }

    }

}
