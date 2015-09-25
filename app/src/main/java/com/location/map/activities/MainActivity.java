package com.location.map.activities;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.location.map.R;
import com.location.map.helper.utils.L;
import com.location.map.services.LocationService;
import com.location.map.services.LocationService.LocationChangeListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

public class MainActivity extends AppCompatActivity implements LocationSource,LocationChangeListener,View.OnClickListener,AMapLocationListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    private MapView mapView;

    private AMap aMap;

    private ImageButton mylocatoinBtn;

    public OnLocationChangedListener onLocationChangedListener;

    private LocationService locationService;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private HashMap<String,MarkerOptions> markers = new HashMap<String,MarkerOptions>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);// 不显示程序的标题栏
        setContentView(R.layout.activity_main);
        mapView = (MapView)findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mylocatoinBtn = (ImageButton)findViewById(R.id.mylocal_btn);
        mylocatoinBtn.setOnClickListener(this);
        initDrawer();
        bindService(new Intent(this, LocationService.class), serviceConnect, BIND_AUTO_CREATE);
        initMap();
        String sha1 = sHA1(this);
        L.i(TAG,sha1);
        initData();

    }

    private ListView localListView;

    private SimpleAdapter simpleAdapter;

    private List<HashMap<String,String>> locals = new ArrayList<HashMap<String,String>>() ;

    private void initDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        // 實作 drawer toggle 並放入 toolbar
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        localListView = (ListView) findViewById(R.id.local_list);
        localListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                L.i(TAG,"item_click: " + position);
                HashMap<String,String> item = ( HashMap<String,String>)parent.getItemAtPosition(position);
                MarkerOptions markerOptions = createMarkerOptions(item);
                aMap.addMarker(markerOptions);
                markers.put(item.get("imei"), markerOptions);
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.getPosition(), 20));  //更改可是区域中心      zoom  4-20  缩放级别
                aMap.clear();
                Collection<MarkerOptions> c =  markers.values();
                for(Iterator<MarkerOptions> it = c.iterator();it.hasNext();){
                    aMap.addMarker(it.next());
                }
                mDrawerLayout.closeDrawers();
//                mDrawerToggle.
            }
        });
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        simpleAdapter = new SimpleAdapter(this, locals, R.layout.drawer_menu_list_item, getResources().getStringArray(R.array.item_titles), new int[]{R.id.item_imei, R.id.item_provider, R.id.item_accuracy, R.id.item_time});
        localListView.setAdapter(simpleAdapter);
    }

    private MarkerOptions createMarkerOptions( HashMap<String,String> item){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(Double.parseDouble(item.get("latitude")), Double.parseDouble(item.get("longitude"))));
        markerOptions.title(item.get("imei")).snippet(item.get("time"));
        markerOptions.perspective(true);
        if(markers!=null){
            int size = markers.size();
            switch (size){
                case 0:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.b_poi_1_hl));
                    break;
                case 1:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.b_poi_2_hl));
                    break;
                case 2:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.b_poi_3_hl));
                    break;
                case 3:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.b_poi_4_hl));
                    break;
                case 4:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.b_poi_5_hl));
                    break;
                case 5:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.b_poi_6_hl));
                    break;
                case 6:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.b_poi_7_hl));
                    break;
                case 7:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.b_poi_8_hl));
                    break;
                case 8:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.b_poi_9_hl));
                    break;
                case 9:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.b_poi_10_hl));
                    break;
                default:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.b_poi_hl));
                    break;
            }
        }
        return markerOptions;
    }

    private void initData(){
//        locationService.getLocations();
    }

    public static String sHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);

            byte[] cert = info.signatures[0].toByteArray();

            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            return hexString.toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void initMap(){
        if(aMap==null){
            aMap = mapView.getMap();
            setUpMap();
        }
    }
    private LocationManagerProxy mAMapLocationManager;

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);


    }



    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        switch (id){
            case android.R.id.home:
                if(mDrawerLayout.isDrawerOpen(findViewById(R.id.drawer_view))){
                    mDrawerLayout.closeDrawer(findViewById(R.id.drawer_view));
                 }else{
                    mDrawerLayout.openDrawer(findViewById(R.id.drawer_view));
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        this.onLocationChangedListener = onLocationChangedListener;

        if (mAMapLocationManager == null) {
            mAMapLocationManager = LocationManagerProxy.getInstance(this);
            //此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            //注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
            //在定位结束后，在合适的生命周期调用destroy()方法
            //其中如果间隔时间为-1，则定位只定一次
            mAMapLocationManager.requestLocationData(
                    LocationProviderProxy.AMapNetwork, 60*1000, 10, this);
        }

    }

    @Override
    public void deactivate() {
        onLocationChangedListener = null;

    }

    @Override
    public void onLocationChange(Location location) {
        onLocationChangedListener.onLocationChanged(location);
        removeProgressDialog();
    }

    @Override
    public void onLocationDataChange(List<HashMap<String,String>> locals) {
        this.locals.removeAll(this.locals);
        this.locals.addAll(locals);
        if(simpleAdapter!=null)
            simpleAdapter.notifyDataSetChanged();

        removeProgressDialog();

    }


    private final ServiceConnection serviceConnect = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            locationService =(LocationService)((LocationService.LocationServiceBinder)service).getService();
            showProgressDialog("loading...");
            locationService.setOnLocationChange(MainActivity.this);
            locationService.getLocations();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id){
            case R.id.mylocal_btn:
                showProgressDialog("loading...");
                if(locationService==null)
                    return;
                locationService.getCurrentLocation("357458040515669");
                locationService.getLocations();
                break;

        }

    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (onLocationChangedListener != null && onLocationChangedListener != null) {
            if (aMapLocation.getAMapException().getErrorCode() == 0) {
                onLocationChangedListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            }
        }
        removeProgressDialog();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    private ProgressDialog progDialog = null;

    private void showProgressDialog(String msg){
        if(progDialog==null){
            progDialog = new ProgressDialog(this);
            progDialog.setMessage(msg);
        }
        progDialog.show();
    }

    private void removeProgressDialog(){
        if(progDialog!=null){
            progDialog.dismiss();
            progDialog = null;
        }
    }
}
