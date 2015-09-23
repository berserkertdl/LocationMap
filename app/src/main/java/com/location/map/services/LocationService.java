package com.location.map.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
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

public class LocationService extends Service {

    private final static String TAG = LocationService.class.getSimpleName();

    private IBinder iBinder = new LocationServiceBinder();

    public LocationChangeListener onLocationChange;

    public interface LocationChangeListener{
        void onLocationChange(Location location);
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
