package com.techkip.bebadriver.request;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hillarie on 12/7/2017.
 */
public class Connector {
    String FinalHttpData = "";
    String Result ;
    BufferedWriter bufferedWriter ;
    OutputStream outputStream ;
    BufferedReader bufferedReader ;
    StringBuilder stringBuilder = new StringBuilder();
    URL url;



    public static HttpURLConnection connect(String urlAddress)
    {
        try {

            URL url=new URL(urlAddress);
            HttpURLConnection con= (HttpURLConnection) url.openConnection();

            //CON PROPS
            con.setRequestMethod("GET");
            con.setConnectTimeout(40000);
            con.setReadTimeout(40000);
            con.setDoInput(true);

            return con;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    public String connect(HashMap<String, String> Data, String urlAddress )
    {
        try {
            URL url=new URL(urlAddress);
            HttpURLConnection con= (HttpURLConnection) url.openConnection();

            //CON PROPS
            con.setRequestMethod("GET");
            con.setConnectTimeout(40000);
            con.setReadTimeout(40000);
            con.setDoInput(true);
            con.setDoInput(true);

            con.setDoOutput(true);

            outputStream = con.getOutputStream();
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            bufferedWriter.write(FinalDataParse(Data));
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {

                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                FinalHttpData = bufferedReader.readLine();
            }
            else {
                FinalHttpData = "Something Went  Wrong";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return FinalHttpData;
    }
    public String FinalDataParse(HashMap<String,String> hashMap2) throws UnsupportedEncodingException {

        for(Map.Entry<String,String> map_entry : hashMap2.entrySet()){

            stringBuilder.append("&");
            stringBuilder.append(URLEncoder.encode(map_entry.getKey(), "UTF-8"));
            stringBuilder.append("=");
            stringBuilder.append(URLEncoder.encode(map_entry.getValue(), "UTF-8"));

        }

        Result = stringBuilder.toString();

        return Result ;
    }


}
