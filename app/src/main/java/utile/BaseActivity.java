package utile;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.administrator.materialmanagement.R;
import com.lzy.okgo.OkGo;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.lang.reflect.Field;


public abstract class BaseActivity extends AppCompatActivity {
    public Context context;
    public String TAG = getClass().getSimpleName();
    public Dialog dialog;
    public static final int LOADING_REFRESH = 2000;
    public static final int LOADING_MORE = 2000;
    public static final int DIALOG_DISMISS = 1500;

    /**
     * 是否退出的标识
     */
    public static boolean isExit = false;
    public static int send_time = 2000;
    //第一次进入页面
    public static boolean isFirstEnter;

    @Override
    public Resources getResources() {
        // 保持字体不受系统字体大小影响
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    /**
     * 点击外部隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();

            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }


    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时没必要隐藏
     *
     * @param v
     * @param event
     * @return
     */
    public static boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        StatusBarUtils.initStatusBarColor(this, R.color.colorPrimaryDark);
    }

    /**
     * 初始化数据
     */
    protected abstract void initData();

    /**
     * 禁止输入框输入空格
     */
    public void NoSpace(EditText editText, CharSequence str, int start) {
        if (str.toString().contains(" ")) {
            String[] strs = str.toString().split(" ");
            String str1 = "";
            for (int i = 0; i < strs.length; i++) {
                str1 += strs[i];
            }
            editText.setText(str1);
            editText.setSelection(start);
        }
    }

    /**
     * 通过文件名获取资源id 例子：getResId("icon", R.drawable.class);
     *
     * @param variableName
     * @param c
     * @return
     */
    public static int getResId(String variableName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //根据 Tag 取消请求
        OkGo.getInstance().cancelTag(TAG);
    }



    /**
     * 设置popwindow
     */
    public void setPopWindow(final TextView v, final String[] arr) {
        //获取屏幕宽度
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        //设置popupWindow的宽度，这几个数字是根据布局中的textView权重得出的
        int with = (int) ((metrics.widthPixels / 1) * 0.7 - 10);

        // 找到需要填充到pop的布局
        View view = LayoutInflater.from(this).inflate(R.layout.pop_list, null);
        // 根据此布局建立pop
        final PopupWindow popupWindow = new PopupWindow(view);
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        // popupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setWidth(with);

        //这样设置pop才可以缩回去
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);


        // 填充此布局上的列表
        ListView listView = (ListView) view.findViewById(R.id.pop_lv);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.pop_list_content, arr);
        listView.setAdapter(adapter);

        // 当listView受到点击时替换mTextView当前显示文本
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                v.setText(arr[arg2]);
                popupWindow.dismiss();
            }
        });
        // 当TextView受到点击时显示pop
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAsDropDown(v, 0, 10);
            }
        });
    }

    /**
     * 设置popwindow
     */
    public void setPopWindowFull(final TextView v, final String[] arr, final SmartRefreshLayout refreshLayout) {
        //获取屏幕宽度
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        //设置popupWindow的宽度，这几个数字是根据布局中的textView权重得出的
        int with = (int) (metrics.widthPixels / 1);

        // 找到需要填充到pop的布局
        View view = LayoutInflater.from(this).inflate(R.layout.pop_list_full, null);
        // 根据此布局建立pop
        final PopupWindow popupWindow = new PopupWindow(view);
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        // popupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setWidth(with);

        //这样设置pop才可以缩回去
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);


        // 填充此布局上的列表
        ListView listView = (ListView) view.findViewById(R.id.pop_lv_full);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.pop_list_content, arr);
        listView.setAdapter(adapter);

        // 当listView受到点击时替换mTextView当前显示文本
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                v.setText(arr[arg2]);
                refreshLayout.autoRefresh();
                popupWindow.dismiss();
            }
        });
        // 当TextView受到点击时显示pop
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAsDropDown(v, 0, 10);
            }
        });
    }


    /**
     * 设置popwindow
     */
    public void setPopWindowHalf(final TextView v, final String[] arr) {
        //获取屏幕宽度
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        //设置popupWindow的宽度，这几个数字是根据布局中的textView权重得出的
        int with = (int) (metrics.widthPixels / 3 * 2);

        // 找到需要填充到pop的布局
        View view = LayoutInflater.from(this).inflate(R.layout.pop_list_full, null);
        // 根据此布局建立pop
        final PopupWindow popupWindow = new PopupWindow(view);
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        // popupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setWidth(with);

        //这样设置pop才可以缩回去
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);


        // 填充此布局上的列表
        ListView listView = (ListView) view.findViewById(R.id.pop_lv_full);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.pop_list_content, arr);
        listView.setAdapter(adapter);

        // 当listView受到点击时替换mTextView当前显示文本
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                v.setText(arr[arg2]);
//                refreshLayout.autoRefresh();
                popupWindow.dismiss();
            }
        });
        // 当TextView受到点击时显示pop
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAsDropDown(v, 0, 10);
            }
        });
    }
}
