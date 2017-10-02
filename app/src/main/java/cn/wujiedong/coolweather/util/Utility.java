package cn.wujiedong.coolweather.util;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.wujiedong.coolweather.db.City;
import cn.wujiedong.coolweather.db.County;
import cn.wujiedong.coolweather.db.Province;

/**
 * Created by Administrator on 2017/10/2.
 */

public class Utility {

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
}
