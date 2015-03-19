package com.asa.phonegap.BaiduGeolocation;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

/**
 * A phonegap plugin to get the geolocation from Baidu Map SDK.
 * Adapted from https://github.com/DoubleSpout/phonegap_baidu_sdk_location.
 *
 * @Author https://github.com/DoubleSpout (Original version using Baidu Map SDK 4.0, Thanks!)
 * @Author https://github.com/darkgeek (Bump the Baidu Map SDK to 4.2 and fix bugs)
 */ 
public class BaiduGeolocation extends CordovaPlugin {
    
    private static final String GET_GEOLOCATION_ACTION = "getCurrentPosition";
    private static final String STOP_ACTION = "stop";
    public CallbackContext callbackContext;
    public LocationClient locationClient = null;
    public BDLocationListener myListener = null;
    public JSONObject jsonObj = new JSONObject();
    private static final Map<Integer, String> ERROR_MESSAGE_MAP = new HashMap<Integer, String>();
    private static final String DEFAULT_ERROR_MESSAGE = "服务端定位失败";

    static {
        ERROR_MESSAGE_MAP.put(61, "GPS定位结果");
        ERROR_MESSAGE_MAP.put(62, "扫描整合定位依据失败。此时定位结果无效");
        ERROR_MESSAGE_MAP.put(63, "网络异常，没有成功向服务器发起请求。此时定位结果无效");
        ERROR_MESSAGE_MAP.put(65, "定位缓存的结果");
        ERROR_MESSAGE_MAP.put(66, "离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果");
        ERROR_MESSAGE_MAP.put(67, "离线定位失败。通过requestOfflineLocaiton调用时对应的返回结果");
        ERROR_MESSAGE_MAP.put(68, "网络连接失败时，查找本地离线定位时对应的返回结果。");
        ERROR_MESSAGE_MAP.put(161, "表示网络定位结果");
    };

    public String getErrorMessage(int locationType) {
        String result = ERROR_MESSAGE_MAP.get(locationType);
        if (result == null) {
            result = DEFAULT_ERROR_MESSAGE;
        }
        return result;
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        boolean result = false;
        setCallbackContext(callbackContext);
        if (GET_GEOLOCATION_ACTION.equals(action)) {
            locationClient = new LocationClient(cordova.getActivity());
            myListener = new MyLocationListener();
            locationClient.registerLocationListener(myListener);
            LocationClientOption option = new LocationClientOption();

            
  
            //option.setCoorType("gcj02");// 设置返回国家测绘局加密后的坐标
            //option.setProdName("AsaApp");	// 设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
			option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式
			option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
			// option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
			option.setIsNeedAddress(true);//返回的定位结果包含地址信息
			// option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
			option.setOpenGps(true); // 是否打开gps进行定位
			
            locationClient.setLocOption(option);

            Log.d("BaiduGeolocation", "Start Geolocation service.");    
            locationClient.start(); // 开启百度地图SDK
            locationClient.requestLocation();// 发起请求

            result = true;
        }else if(STOP_ACTION.equals(action)){
			locationClient.stop();
            Log.d("BaiduGeolocation", "User Stop Geolocation service.");    
		}else {
            callbackContext.error(PluginResult.Status.INVALID_ACTION.toString());
        }

        return result;
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return;
            try {
                JSONObject coords = new JSONObject();
                coords.put("latitude", location.getLatitude());
                coords.put("longitude", location.getLongitude());
                coords.put("radius", location.getRadius());
                jsonObj.put("coords", coords);
                int locationType = location.getLocType();
                jsonObj.put("locationType", locationType);
                jsonObj.put("code", locationType);
                jsonObj.put("message", getErrorMessage(locationType));
                switch (location.getLocType()) {
                    case BDLocation.TypeGpsLocation:
                        coords.put("speed", location.getSpeed());
                        coords.put("altitude", location.getAltitude());
                        jsonObj.put("SatelliteNumber",
                                location.getSatelliteNumber());
                        break;
                    case BDLocation.TypeNetWorkLocation:
                        jsonObj.put("addr", location.getAddrStr());
                        break;
                }				
                Log.d("BaiduGeolocation", "run: " + jsonObj.toString());
                callbackContext.success(jsonObj);
            } catch (JSONException e) {
                callbackContext.error(e.getMessage());
            }
            
           
        }

        public void onReceivePoi(BDLocation poiLocation) {
            // TODO 
        }
    }

    @Override
    public void onDestroy() {
        if (locationClient != null && locationClient.isStarted()) {
            locationClient.stop();
            locationClient = null;
            Log.d("BaiduGeolocation", "Destroy Geolocation service.");    
        }
        super.onDestroy();
    }

    public CallbackContext getCallbackContext() {
        return callbackContext;
    }

    public void setCallbackContext(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }
}
