package ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.example.administrator.materialmanagement.R;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import bean.JsonBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utile.BaseActivity;
import utile.DialogUtil;
import utile.GetJsonDataUtil;
import utile.PortIpAddress;
import utile.Regular;
import utile.SharedPrefsUtil;
import utile.ShowToast;

import static com.example.administrator.materialmanagement.Login.AGENT_CODE;
import static utile.PortIpAddress.CODE;
import static utile.PortIpAddress.MESSAGE;
import static utile.PortIpAddress.SUCCESS_CODE;

public class AgentRetailerDetail extends BaseActivity {
    @BindView(R.id.title_name)
    TextView title_name;
    @BindView(R.id.title_name_right)
    TextView title_name_right;
    @BindView(R.id.agent_retailer_yhm_ll)
    LinearLayout agent_retailer_yhm_ll;
    @BindView(R.id.agent_retailer_mm_ll)
    LinearLayout agent_retailer_mm_ll;
//    @BindView(R.id.agent_retailer_etv_bh)
//    EditText agent_retailer_etv_bh;
    @BindView(R.id.agent_retailer_etv_yhm)
    EditText agent_retailer_etv_yhm;
    @BindView(R.id.agent_retailer_etv_mm)
    EditText agent_retailer_etv_mm;
    @BindView(R.id.agent_retailer_etv_mc)
    EditText agent_retailer_etv_mc;
    @BindView(R.id.agent_retailer_etv_lxr)
    EditText agent_retailer_etv_lxr;
    @BindView(R.id.agent_retailer_etv_lxdh)
    EditText agent_retailer_etv_lxdh;
    @BindView(R.id.agent_retailer_etv_lx)
    TextView agent_retailer_etv_lx;
    @BindView(R.id.agent_retailer_lx_ll)
    LinearLayout agent_retailer_lx_ll;
    @BindView(R.id.agent_retailer_etv_sjjxs)
    EditText agent_retailer_etv_sjjxs;
    @BindView(R.id.agent_retailer_etv_sheng)
    TextView agent_retailer_etv_sheng;
    @BindView(R.id.agent_retailer_etv_shi)
    TextView agent_retailer_etv_shi;
    @BindView(R.id.agent_retailer_etv_xian)
    TextView agent_retailer_etv_xian;
    @BindView(R.id.shengshixian)
    LinearLayout shengshixian;
    @BindView(R.id.agent_retailer_etv_xxdz)
    EditText agent_retailer_etv_xxdz;
    @BindView(R.id.retailer_detail_sjjxs_ll)
    LinearLayout retailer_detail_sjjxs_ll;
    @BindView(R.id.agent_retailer_etv_bz)
    EditText agent_retailer_etv_bz;


    private String tag = "";
    private String dealersid = "";
    private String url = "";

    private static final int MSG_LOAD_DATA = 0x0001;
    private static final int MSG_LOAD_SUCCESS = 0x0002;
    private static final int MSG_LOAD_FAILED = 0x0003;
    private ArrayList<JsonBean> options1Items = new ArrayList<>();
    private ArrayList<ArrayList<String>> options2Items = new ArrayList<>();
    private ArrayList<ArrayList<ArrayList<String>>> options3Items = new ArrayList<>();
    private Thread thread;

    //类型
    private String[] lx_arr;
    private Map<String, String> lx_map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_retailer_detail);
        ButterKnife.bind(this);
        initData();
    }

    @OnClick(R.id.back)
    void Back() {
        finish();
    }

    /**
     * 提交
     */
    @OnClick(R.id.title_name_right)
    void Submit() {
        if (tag.equals("add") && agent_retailer_etv_yhm.getText().toString().trim().equals("")) {
            ShowToast.showShort(this, "请填写用户名");
        } else if (tag.equals("add") && agent_retailer_etv_mm.getText().toString().trim().equals("")) {
            ShowToast.showShort(this, "请填写用户密码");
        } else if (agent_retailer_etv_mc.getText().toString().trim().equals("")) {
            ShowToast.showShort(this, "请填写名称");
        } else if (!SharedPrefsUtil.getValue(this, "userInfo", "usertype", "").equals(AGENT_CODE) && agent_retailer_etv_lxr.getText().toString().trim().equals("")) {
            ShowToast.showShort(this, "请填写联系人");
        } else if (!Regular.isChinaPhoneLegal(agent_retailer_etv_lxdh.getText().toString()) && !agent_retailer_etv_lxdh.getText().toString().equals("")) {
            ShowToast.showShort(this, "请填写正确的电话号码");
        } else {
            OkGo.<String>get(url)
                    .tag(TAG)
                    .params("loginuserid", SharedPrefsUtil.getValue(this, "userInfo", "userid", ""))
                    .params(PortIpAddress.Bean + "dealersid", dealersid)
                    .params(PortIpAddress.Bean + "loginname", agent_retailer_etv_yhm.getText().toString())
                    .params(PortIpAddress.Bean + "loginpwd", agent_retailer_etv_mm.getText().toString())
                    .params(PortIpAddress.Bean + "dealersname", agent_retailer_etv_mc.getText().toString())
                    .params(PortIpAddress.Bean + "tasktype", lx_map.get(agent_retailer_etv_lx.getText().toString()))
                    .params(PortIpAddress.Bean + "provinces", agent_retailer_etv_sheng.getText().toString())
                    .params(PortIpAddress.Bean + "city", agent_retailer_etv_shi.getText().toString())
                    .params(PortIpAddress.Bean + "county", agent_retailer_etv_xian.getText().toString())
                    .params(PortIpAddress.Bean + "address", agent_retailer_etv_xxdz.getText().toString())
                    .params(PortIpAddress.Bean + "linkman", agent_retailer_etv_lxr.getText().toString())
                    .params(PortIpAddress.Bean + "phone", agent_retailer_etv_lxdh.getText().toString())
                    .params(PortIpAddress.Bean + "memo", agent_retailer_etv_bz.getText().toString())
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            Log.e(TAG, response.body().toString());
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                String err = jsonObject.getString(MESSAGE);

                                if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
                                    if (tag.equals("modify")) {
                                        ShowToast.showShort(AgentRetailerDetail.this, "修改成功");
                                    } else {
                                        ShowToast.showShort(AgentRetailerDetail.this, "新增成功");
                                    }
                                    setResult(RESULT_OK);
                                    finish();
                                } else {
                                    ShowToast.showShort(AgentRetailerDetail.this, err);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            ShowToast.showShort(AgentRetailerDetail.this, R.string.connect_err);
                        }
                    });
        }
    }


    @Override
    protected void initData() {
        Intent intent = getIntent();
        tag = intent.getStringExtra("tag");
        title_name_right.setText("提交");
        lx_map = new HashMap();
        lx_arr = getResources().getStringArray(R.array.ck_type);

        lx_map.put(lx_arr[0], "JXSLX001");
        lx_map.put(lx_arr[1], "JXSLX002");
//        if (SharedPrefsUtil.getValue(this, "userType", "code", "").equals(AGENT_CODE)) {
//            title_name.setText("我的零售商");
//        } else {
//            title_name.setText("我的代理商");
//            retailer_detail_sjjxs_ll.setVisibility(View.GONE);
//        }

        title_name.setText("我的经销商");

        if (SharedPrefsUtil.getValue(this, "userInfo", "usertype", "").equals(AGENT_CODE)) {
            agent_retailer_lx_ll.setVisibility(View.GONE);
        }

        if (tag.equals("modify")) {
            url = PortIpAddress.ModifyDlsJxs();
            dealersid = intent.getStringExtra("bean.dealersid");
            agent_retailer_yhm_ll.setVisibility(View.GONE);
            agent_retailer_mm_ll.setVisibility(View.GONE);
            mHandler.sendEmptyMessage(MSG_LOAD_DATA);
            OneInfoDetail();
        } else {
            url = PortIpAddress.AddDlsJxs();
            if (SharedPrefsUtil.getValue(this, "userInfo", "usertype", "").equals(AGENT_CODE)) {
                shengshixian.setVisibility(View.GONE);
            }
            agent_retailer_etv_lx.setText(lx_arr[0]);
            mHandler.sendEmptyMessage(MSG_LOAD_DATA);
        }

        //设置popwindow
        setPopWindowHalf(agent_retailer_etv_lx, lx_arr);
    }


    /**
     * 一条信息的详情
     */
    private void OneInfoDetail() {
        dialog = DialogUtil.createLoadingDialog(AgentRetailerDetail.this, R.string.loading);
        OkGo.<String>get(PortIpAddress.DlsJxsDetail())
                .tag(TAG)
                .params("loginuserid", SharedPrefsUtil.getValue(this, "userInfo", "userid", ""))
                .params("bean.dealersid", dealersid)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            String err = jsonObject.getString(MESSAGE);
                            if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
                                agent_retailer_etv_mc.setText(jsonObject.getString("bean.dealersname"));
                                agent_retailer_etv_lxr.setText(jsonObject.getString("bean.linkman"));
                                agent_retailer_etv_lxdh.setText(jsonObject.getString("bean.phone"));
                                agent_retailer_etv_lx.setText(getKey(lx_map, jsonObject.getString("bean.tasktype")));
                                agent_retailer_etv_sheng.setText(jsonObject.getString("bean.provinces"));
                                agent_retailer_etv_shi.setText(jsonObject.getString("bean.city"));
                                agent_retailer_etv_xian.setText(jsonObject.getString("bean.county"));
                                agent_retailer_etv_xxdz.setText(jsonObject.getString("bean.address"));
                                agent_retailer_etv_bz.setText(jsonObject.getString("bean.memo"));
                            } else {
                                ShowToast.showShort(AgentRetailerDetail.this, err);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        dialog.dismiss();
                        ShowToast.showShort(AgentRetailerDetail.this, R.string.connect_err);
                    }
                });
    }


    public static String getKey(Map<String, String> map, String value) {
        String key = null;
        //Map,HashMap并没有实现Iteratable接口.不能用于增强for循环.
        for (String getKey : map.keySet()) {
            if (map.get(getKey).equals(value)) {
                key = getKey;
            }
        }
        return key;
        //这个key肯定是最后一个满足该条件的key.
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_DATA:
                    if (thread == null) {//如果已创建就不再重新创建子线程了
                        thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // 子线程中解析省市区数据
                                initJsonData();
                            }
                        });
                        thread.start();
                    }
                    break;
                case MSG_LOAD_SUCCESS:
                    if (tag.equals("add")) {
                        String initSheng = options1Items.get(0).getName();
                        String initShi = options2Items.get(0).get(0);
                        String initXian = options3Items.get(0).get(0).get(0);
                        agent_retailer_etv_sheng.setText(initSheng);
                        agent_retailer_etv_shi.setText(initShi);
                        agent_retailer_etv_xian.setText(initXian);
                    }
                    break;

                case MSG_LOAD_FAILED:
                    ShowToast.showShort(AgentRetailerDetail.this, "解析失败");
                    break;
            }
        }
    };


    /**
     * 省市县选择器
     */
    @OnClick(R.id.shengshixian)
    void ShengShiXian() {
        showPickerView();
    }

    private int index1 = 0;
    private int index2 = 0;
    private int index3 = 0;

    private void showPickerView() {
        // 弹出选择器
        OptionsPickerView pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                String sheng = options1Items.get(options1).getPickerViewText();
                String shi = options2Items.get(options1).get(options2);
                String xian = options3Items.get(options1).get(options2).get(options3);
                agent_retailer_etv_sheng.setText(sheng);
                agent_retailer_etv_shi.setText(shi);
                agent_retailer_etv_xian.setText(xian);
                index1 = options1;
                index2 = options2;
                index3 = options3;
            }
        })

                .setTitleText("省、市、县选择")
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK) //设置选中项文字颜色
                .setContentTextSize(20)
                .build();

        /*pvOptions.setPicker(options1Items);//一级选择器
        pvOptions.setPicker(options1Items, options2Items);//二级选择器*/
        pvOptions.setPicker(options1Items, options2Items, options3Items);//三级选择器
        pvOptions.setSelectOptions(index1, index2, index3);
        pvOptions.show();
    }


    /**
     * assets 目录下的Json文件，可自行替换文件
     * 关键逻辑在于循环体
     */
    private void initJsonData() {
        //解析数据
        String JsonData = new GetJsonDataUtil().getJson(this, "province.json");//获取assets目录下的json文件数据

        ArrayList<JsonBean> jsonBean = parseData(JsonData);//用Gson 转成实体

        /**
         * 添加省份数据
         *
         * 注意：如果是添加的JavaBean实体，则实体类需要实现 IPickerViewData 接口，
         * PickerView会通过getPickerViewText方法获取字符串显示出来。
         */
        options1Items = jsonBean;

        for (int i = 0; i < jsonBean.size(); i++) {//遍历省份
            ArrayList<String> CityList = new ArrayList<>();//该省的城市列表（第二级）
            ArrayList<ArrayList<String>> Province_AreaList = new ArrayList<>();//该省的所有地区列表（第三极）

            for (int c = 0; c < jsonBean.get(i).getCityList().size(); c++) {//遍历该省份的所有城市
                String CityName = jsonBean.get(i).getCityList().get(c).getName();
                CityList.add(CityName);//添加城市
                ArrayList<String> City_AreaList = new ArrayList<>();//该城市的所有地区列表

                //如果无地区数据，建议添加空字符串，防止数据为null 导致三个选项长度不匹配造成崩溃
                if (jsonBean.get(i).getCityList().get(c).getArea() == null
                        || jsonBean.get(i).getCityList().get(c).getArea().size() == 0) {
                    City_AreaList.add("");
                } else {
                    City_AreaList.addAll(jsonBean.get(i).getCityList().get(c).getArea());
                }
                Province_AreaList.add(City_AreaList);//添加该省所有地区数据
            }

            /**
             * 添加城市数据
             */
            options2Items.add(CityList);
            /**
             * 添加地区数据
             */
            options3Items.add(Province_AreaList);
        }
        mHandler.sendEmptyMessage(MSG_LOAD_SUCCESS);
    }


    public ArrayList<JsonBean> parseData(String result) {
        //Gson 解析
        ArrayList<JsonBean> detail = new ArrayList<>();
        try {
            JSONArray data = new JSONArray(result);
            Gson gson = new Gson();
            for (int i = 0; i < data.length(); i++) {
                JsonBean entity = gson.fromJson(data.optJSONObject(i).toString(), JsonBean.class);
                detail.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(MSG_LOAD_FAILED);
        }
        return detail;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
