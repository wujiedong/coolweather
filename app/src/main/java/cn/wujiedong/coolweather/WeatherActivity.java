package cn.wujiedong.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;

import cn.wujiedong.coolweather.gson.AQI;
import cn.wujiedong.coolweather.gson.Basic;
import cn.wujiedong.coolweather.gson.Forecast;
import cn.wujiedong.coolweather.gson.Now;
import cn.wujiedong.coolweather.gson.Suggestion;
import cn.wujiedong.coolweather.gson.Weather;
import cn.wujiedong.coolweather.util.CollectionUtil;
import cn.wujiedong.coolweather.util.HttpUtil;
import cn.wujiedong.coolweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 天气显示activity
 * (1) 初始化UI组件
 * (2) 查询天气情况,优先本地其次网络,查询之后,将数据按照key-value的形式存在放本地
 * (3) 在UI组建中展示对应的查询数据
 */
public class WeatherActivity extends AppCompatActivity {

    private TextView cityTextView,timeTextView,tempTextView,infoTextView,aqiTextView,pmTextView,comfortTextView,carWashTextView,sportTextView;
    private ScrollView weatherLayout;
    private LinearLayout forecastLayout;
    private ImageView bingPicImageView,moreImageView;
    public SwipeRefreshLayout weatherSwipeRefresh;
    public DrawerLayout weatherDrawerLayout;
    private String mWeatherId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        initView();
        initWeatherData();
        initOnclickListener();

    }

    private void initView(){
        cityTextView = (TextView) findViewById(R.id.cityTextView);
        timeTextView = (TextView) findViewById(R.id.timeTextView);
        tempTextView = (TextView) findViewById(R.id.tempTextView);
        infoTextView = (TextView) findViewById(R.id.infoTextView);
        aqiTextView = (TextView) findViewById(R.id.aqiTextView);
        pmTextView = (TextView) findViewById(R.id.pmTextView);
        comfortTextView = (TextView) findViewById(R.id.comfortTextView);
        carWashTextView = (TextView) findViewById(R.id.carWashTextView);
        sportTextView = (TextView) findViewById(R.id.sportTextView);
        forecastLayout = (LinearLayout) findViewById(R.id.forecastLayout);
        weatherLayout = (ScrollView) findViewById(R.id.weatherLayout);
        bingPicImageView = (ImageView) findViewById(R.id.bingPicImageView);
        moreImageView = (ImageView) findViewById(R.id.moreImageView);

        //加载bing搜索引擎中的图片
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
        String bingPic = sharedPreferences.getString("bingPic", null);
        if(null != bingPic){
            Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImageView);
        }else{
            loadBingPic();
        }

        weatherSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.weatherSwipeRefresh);
        weatherDrawerLayout = (DrawerLayout) findViewById(R.id.weatherDrawerLayout);
    }

    private void loadBingPic(){
        String url = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putString("bingPic",bingPic);
                edit.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImageView);
                    }
                });
            }
        });
    }

    private void initOnclickListener() {
        MyOnClickListener myOnClickListener = new MyOnClickListener();
        moreImageView.setOnClickListener(myOnClickListener);
    }

    /**
     * 将weather中的值放到UI中去
     * (1) 获取到shareprefences对象,优先本地数据,其次服务器数据
     */
    private void initWeatherData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
        String weatherStr = sharedPreferences.getString("weather", null);
        if(TextUtils.isEmpty(weatherStr)){//走服务器
            mWeatherId = getIntent().getStringExtra("weatherId");
            weatherLayout.setVisibility(View.VISIBLE);
            requestWeather(mWeatherId);
        }else{//走本地
            Weather weather = Utility.handlerWeatherResponse(weatherStr);
            showWeatherInfo(weather);
            mWeatherId = weather.getBasic().getWeatherId();
        }
        weatherSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
    }

    public void requestWeather(final String weatherId){
        String url = "https://free-api.heweather.com/v5/weather?city="+weatherId+"&key=c0ac893d69fe477bb134f5380abedd8f";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"网络请求失败",Toast.LENGTH_SHORT).show();
                        weatherSwipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String weatherStr = response.body().string();
                final Weather weather = Utility.handlerWeatherResponse(weatherStr);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String status = weather.getStatus();
                        if("ok".equals(status) && null != weather){//切换到主线程中,将服务器数据存储到本地
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                            SharedPreferences.Editor edit = sharedPreferences.edit();
                            edit.putString("weather",weatherStr);
                            edit.apply();
                            mWeatherId = weather.getBasic().getWeatherId();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气失败",Toast.LENGTH_SHORT).show();
                        }
                        weatherSwipeRefresh.setRefreshing(false);
                    }
                });


            }
        });

    }

    /**
     * 根据Weather对象中数据,向UI中填充数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather){
        Basic basic = weather.getBasic();
        if(null != basic){
            cityTextView.setText(basic.getCityName().toString());
            timeTextView.setText(basic.getUpdate().getUpdateTime().toString());
        }

        Now now = weather.getNow();
        if(null != now){
            tempTextView.setText(now.getTemperature().toString() + "°C");
            infoTextView.setText(now.getMore().getInfo().toString());
        }
        AQI aqi = weather.getAqi();
        if(null != aqi){
            AQI.AQICity city = aqi.getCity();
            if(null != city){
                aqiTextView.setText(city.getAqi().toString());
                pmTextView.setText(city.getPm25().toString());
            }
        }


        Suggestion suggestion = weather.getSuggestion();
        if(null != suggestion){
            comfortTextView.setText("舒适度 : "+ suggestion.getComfort().getInfo());
            carWashTextView.setText("洗车指数 : " +suggestion.getCarWash().getInfo());
            sportTextView.setText("运动指数 : " + suggestion.getSport().getInfo());
        }


        List<Forecast> forecasts = weather.getForecasts();
        if(CollectionUtil.checkNotEmpty(forecasts)){
            forecastLayout.removeAllViews();
            for (Forecast forecast : forecasts) {
                View view = LayoutInflater.from(WeatherActivity.this).inflate(R.layout.forecast_item, forecastLayout, false);
                TextView forecastTimeTextView = (TextView) view.findViewById(R.id.forecastTimeTextView);
                TextView forecastInfoTextView = (TextView) view.findViewById(R.id.forecastInfoTextView);
                TextView forecastMaxTempTextView = (TextView) view.findViewById(R.id.forecastMaxTempTextView);
                TextView forecastMinTempTextView = (TextView) view.findViewById(R.id.forecastMinTempTextView);
                forecastTimeTextView.setText(forecast.getDate());
                forecastInfoTextView.setText(forecast.getMore().getInfo());
                forecastMaxTempTextView.setText(forecast.getTemperature().getMax());
                forecastMinTempTextView.setText(forecast.getTemperature().getMin());
                forecastLayout.addView(view);
            }
        }
        weatherLayout.setVisibility(View.VISIBLE);
    }

    class MyOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.moreImageView:
                    weatherDrawerLayout.openDrawer(GravityCompat.START);
                    break;
            }
        }
    }
}
