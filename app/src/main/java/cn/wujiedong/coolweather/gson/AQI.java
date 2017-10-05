package cn.wujiedong.coolweather.gson;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/10/3.
 */

public class AQI implements Serializable {

    private AQICity city;

    public AQICity getCity() {
        return city;
    }

    public void setCity(AQICity city) {
        this.city = city;
    }

    public class AQICity{
        public String aqi;
        public String pm25;

        public String getAqi() {
            return aqi;
        }

        public void setAqi(String aqi) {
            this.aqi = aqi;
        }

        public String getPm25() {
            return pm25;
        }

        public void setPm25(String pm25) {
            this.pm25 = pm25;
        }
    }
}
