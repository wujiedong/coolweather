package cn.wujiedong.coolweather.util;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.wujiedong.coolweather.db.City;
import cn.wujiedong.coolweather.db.County;
import cn.wujiedong.coolweather.db.Province;
import cn.wujiedong.coolweather.gson.Weather;

/**
 * Created by Administrator on 2017/10/2.
 */

public class Utility {

    /**
     * 解析服务器省级数据
     * @param response
     * @return
     */
    public static boolean handlerProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray provinces = new JSONArray(response);
                for (int i = 0;i<provinces.length();i++){
                    JSONObject jsonObject = provinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.setProvinceName(jsonObject.getString("name"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析服务器市级数据
     * @param response
     * @return
     */
    public static boolean handlerCityResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray citys = new JSONArray(response);
                for (int i = 0;i<citys.length();i++){
                    JSONObject jsonObject = citys.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(jsonObject.getInt("id"));
                    city.setCityName(jsonObject.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析服务器县级数据
     * @param response
     * @return
     */
    public static boolean handlerCountyResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray countys = new JSONArray(response);
                for (int i = 0;i<countys.length();i++){
                    JSONObject jsonObject = countys.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(jsonObject.getString("name"));
                    county.setWeatherId(jsonObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析服务器天气数据
     * @param response
     * @return
     */
    public static Weather handlerWeatherResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONObject jsonObject = new JSONObject(response);//{}的数据使用JSONObject对象接受
                JSONArray weather = jsonObject.getJSONArray("HeWeather5");//[]的数据使用JSONArray对象接受
                String weatherStr = weather.getJSONObject(0).toString();
                return new Gson().fromJson(weatherStr,Weather.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }
}
