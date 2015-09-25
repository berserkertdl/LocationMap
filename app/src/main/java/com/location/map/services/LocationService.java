package com.location.map.services;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import com.amap.api.maps.LocationSource;
import com.location.map.activities.MainActivity;
import com.location.map.helper.utils.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocationService extends Service {

    private final static String TAG = LocationService.class.getSimpleName();

    private IBinder iBinder = new LocationServiceBinder();

    public LocationChangeListener onLocationChange;

    public interface LocationChangeListener{
        void onLocationChange(Location location);

        void onLocationDataChange(List<HashMap<String,String>> locals);
    }

    public void setOnLocationChange(LocationChangeListener onLocationChange){
        this.onLocationChange = onLocationChange;
    }

    public class LocationServiceBinder extends Binder{

        public LocationService getService(){
            return LocationService.this;
        }

    }

    public void getCurrentLocation(final String imei){
       new AsyncTask<String,Void,String>(){

           @Override
           protected String doInBackground(String... params) {
               StringBuffer result = new StringBuffer();
               try {
                   URL url = new URL("http://121.43.224.29:8080/PlaceServer/getCurrentLocation.do");
                   HttpURLConnection httpConnect = (HttpURLConnection) url.openConnection();
                   httpConnect.setDoInput(true); //设置输入采用字符流
                   httpConnect.setDoOutput(true); //设置输出采用字符流
                   httpConnect.setRequestMethod("POST");
                   httpConnect.setUseCaches(false);//设置缓存
                   httpConnect.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                   httpConnect.setRequestProperty("Charset", "UTF-8");

                   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(httpConnect.getOutputStream()));
                   writer.write("imei="+imei);

                   writer.flush();
                   writer.close();

                   BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnect.getInputStream()));
                   String readLine = null;

                   while ((readLine = reader.readLine()) != null) {
                       result.append(readLine);
                   }
                   reader.close();
                   L.i(TAG, "result : " + result.toString());
                   httpConnect.disconnect();
               }catch (Exception e){

               }
               return result+"";
           }

           @Override
           protected void onPostExecute(String s) {
               L.i(TAG," result : "+s);
               if("0".equals(s) ||onLocationChange ==null){
                   return;
               }

               SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

               JSONArray jsonArray = null;
               JSONObject item = null;
               try {
                   jsonArray = new JSONArray(s);
                   if(jsonArray==null){
                       return;
                   }
                   item = (JSONObject) jsonArray.get(0);
                   Location location = new Location((String) item.get("provider"));
                   location.setLatitude(Double.parseDouble(item.get("latitude").toString()));
                   location.setLongitude(Double.parseDouble(item.get("longitude").toString()));
                   location.setAccuracy(Float.parseFloat(item.get("accuracy").toString()));
                   String time_str = (String) item.get("time");
                   location.setTime(df.parse(time_str).getTime());
                   onLocationChange.onLocationChange(location);

               } catch (JSONException e) {
                   e.printStackTrace();
               } catch (ParseException e) {
                   e.printStackTrace();
               }

           }
       }.execute(imei);

    }


    public void getLocations(){
       new AsyncTask<Void,Void,String>(){

           @Override
           protected void onPreExecute() {
               super.onPreExecute();
           }

           @Override
           protected String doInBackground(Void... params) {
               StringBuffer result = new StringBuffer();
               try {
                   URL url = new URL("http://121.43.224.29:8080/PlaceServer/getLocationsBy.do");
                   HttpURLConnection httpConnect = (HttpURLConnection) url.openConnection();
                   httpConnect.setDoInput(true); //设置输入采用字符流
                   httpConnect.setDoOutput(true); //设置输出采用字符流
                   httpConnect.setRequestMethod("POST");
                   httpConnect.setUseCaches(false);//设置缓存
                   httpConnect.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                   httpConnect.setRequestProperty("Charset", "UTF-8");
                   BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnect.getInputStream()));
                   String readLine = null;
                   while ((readLine = reader.readLine()) != null) {
                       result.append(readLine);
                   }
                   reader.close();
                   JSONArray resultJson = new JSONArray(result.toString());
                   if(resultJson!=null&&resultJson.length()>0){
                       JSONObject resultCode = (JSONObject)resultJson.get(0);
                       if("1".equals( resultCode.get("resultCode"))){
                           JSONObject dataItem= null;
                           List<HashMap<String,String>> datas = new ArrayList<HashMap<String, String>>();
                           HashMap<String,String> item = null;
                           for (int i = 1; i < resultJson.length(); i++) {
                               dataItem = (JSONObject)resultJson.get(i);
                               item = new HashMap<String, String>();
                               item.put("imei",(String)dataItem.get("imei"));
                               item.put("provider",(String)dataItem.get("provider"));
                               item.put("accuracy",(String)dataItem.get("accuracy"));
                               item.put("time", (String) dataItem.get("time"));
                               item.put("latitude", (String) dataItem.get("latitude"));
                               item.put("longitude", (String) dataItem.get("longitude"));
                               datas.add(item);
                           }
                           onLocationChange.onLocationDataChange(datas);
                       }
                   }
                   L.i(TAG, "result : " + result.toString());
                   httpConnect.disconnect();
               }catch (Exception e){

               }
               return result+"";
           }

           @Override
           protected void onPostExecute(String s) {
               L.i(TAG," result : "+s);
               if("0".equals(s) ||onLocationChange ==null){
                   return;
               }

               SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

               JSONArray jsonArray = null;
               JSONObject item = null;
               try {
                   jsonArray = new JSONArray(s);
                   if(jsonArray==null){
                       return;
                   }
                   item = (JSONObject) jsonArray.get(0);
                   Location location = new Location((String) item.get("provider"));
                   location.setLatitude(Double.parseDouble(item.get("latitude").toString()));
                   location.setLongitude(Double.parseDouble(item.get("longitude").toString()));
                   location.setAccuracy(Float.parseFloat(item.get("accuracy").toString()));
                   String time_str = (String) item.get("time");
                   location.setTime(df.parse(time_str).getTime());
                   onLocationChange.onLocationChange(location);
               } catch (JSONException e) {
                   e.printStackTrace();
               } catch (ParseException e) {
                   e.printStackTrace();
               }

           }
       }.execute();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return iBinder;
    }

    private void send(final String path, final String args) {
        new Thread() {
            @Override
            public void run() {

                Message msg = new Message();
                msg.what = 1;
                Bundle bundle = new Bundle();
//                    bundle.putString("result", result.toString());
//                    msg.setData(bundle);

            }
        }.start();
    }

}
