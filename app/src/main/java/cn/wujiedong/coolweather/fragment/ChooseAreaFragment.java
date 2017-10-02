package cn.wujiedong.coolweather.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.litepal.crud.DataSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import cn.wujiedong.coolweather.R;
import cn.wujiedong.coolweather.db.City;
import cn.wujiedong.coolweather.db.County;
import cn.wujiedong.coolweather.db.Province;
import cn.wujiedong.coolweather.util.CollectionUtil;
import cn.wujiedong.coolweather.util.HttpUtil;
import cn.wujiedong.coolweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/10/2.
 */

public class ChooseAreaFragment extends Fragment {

    public static final String BASE_AREA_URL = "http://guolin.tech/api/china";

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    /**
     * 服务器数据请求类型:省数据请求
     */
    public static final String REQUEST_PROVINCE_TYPE = "province";
    /**
     * 服务器数据请求类型:市数据请求
     */
    public static final String REQUEST_CITY_TYPE = "city";
    /**
     * 服务器数据请求类型:县数据请求
     */
    public static final String REQUEST_COUNTY_TYPE = "county";

    private TextView titleTextView;
    private Button topBackbutton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ProgressDialog progressDialog;

    private List<Province> provinces;
    private List<City> citys;
    private List<County> countys;
    private List<String> datas = new ArrayList<String>();//ListView中组装的数据
    private Province selectedProvince;//选中省分
    private City selectedCity;//选中城市
    private int currentLevel;//当前选中级别

    /**
     * 初始化UI组件
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        topBackbutton = (Button) view.findViewById(R.id.topBackbutton);
        listView = (ListView) view.findViewById(R.id.listView);

        adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,datas);
        listView.setAdapter(adapter);
        return view;
    }

    /**
     * 进行各种事件的绑定
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //绑定listView单击时间
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (currentLevel){
                    case LEVEL_PROVINCE://如果点击的是省级数据,那下面查询的是市级数据
                        selectedProvince = provinces.get(position);
                        queryCitys();
                        break;
                    case LEVEL_CITY://如果点击的是市级数据,那下面查询的是县级数据
                        selectedCity = citys.get(position);
                        queryCountys();
                        break;
                    case LEVEL_COUNTY:

                        break;
                }
            }
        });

        //绑定返回按钮单击事件
        topBackbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentLevel){
                    case LEVEL_CITY:
                        queryProvices();
                        break;
                    case LEVEL_COUNTY:
                        queryCitys();
                        break;
                }
            }
        });

        //首次进入加载省级数据,同时确认currentLevel的值是LEVEL_PROVINCE
        queryProvices();
    }

    /**
     * 查询省数据,优先本地,然后数据库
     */
    private void queryProvices(){
        titleTextView.setText("中国");
        topBackbutton.setVisibility(View.GONE);
        provinces = DataSupport.findAll(Province.class);
        if(CollectionUtil.checkNotEmpty(provinces)){
            datas.clear();
            for (Province province : provinces) {
                datas.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            queryFromServer(BASE_AREA_URL,REQUEST_PROVINCE_TYPE);
        }

    }


    /**
     * 查询市数据,优先本地,然后数据库
     */
    private void queryCitys(){
        titleTextView.setText(selectedProvince.getProvinceName());
        topBackbutton.setVisibility(View.VISIBLE);
        citys = DataSupport.where("provinceId = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(CollectionUtil.checkNotEmpty(citys)){
            datas.clear();
            for (City city : citys) {
                datas.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            queryFromServer(BASE_AREA_URL+"/"+selectedProvince.getProvinceCode(),REQUEST_CITY_TYPE);
        }

    }


    /**
     * 查询县数据,优先本地,然后数据库
     */
    private void queryCountys(){
        titleTextView.setText(selectedCity.getCityName());
        topBackbutton.setVisibility(View.VISIBLE);
        countys = DataSupport.where("cityId = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(CollectionUtil.checkNotEmpty(countys)){
            datas.clear();
            for (County county : countys) {
                datas.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            queryFromServer(BASE_AREA_URL+"/"+selectedProvince.getProvinceCode()+"/"+ selectedCity.getCityCode(),REQUEST_COUNTY_TYPE);
        }
    }


    /**
     * 根据url地址和类型,查询服务器中的数据,并保存本地之后,再进行查询
     * @param address
     * @param type
     */
    private void queryFromServer(String address, final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                switch (type){
                    case REQUEST_PROVINCE_TYPE:
                        result = Utility.handlerProvinceResponse(responseText);
                        break;
                    case REQUEST_CITY_TYPE:
                        result = Utility.handlerCityResponse(responseText,selectedProvince.getId());
                        break;
                    case REQUEST_COUNTY_TYPE:
                        result = Utility.handlerCountyResponse(responseText,selectedCity.getId());
                        break;
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            switch (type){
                                case REQUEST_PROVINCE_TYPE:
                                    queryProvices();
                                    break;
                                case REQUEST_CITY_TYPE:
                                    queryCitys();
                                    break;
                                case REQUEST_COUNTY_TYPE:
                                    queryCountys();
                                    break;
                            }
                        }
                    });

                }else{
                    Toast.makeText(getContext(),"服务器参数解析异常",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 显示弹出进度对话框
     */
    private void showProgressDialog(){
        if(null == progressDialog){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("数据加载中...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭弹出进度对话框
     */
    private void closeProgressDialog(){
        if(null != progressDialog){
            progressDialog.dismiss();
        }
    }
}
