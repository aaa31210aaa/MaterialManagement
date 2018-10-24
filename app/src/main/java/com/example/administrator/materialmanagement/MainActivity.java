package com.example.administrator.materialmanagement;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;

import adapter.ViewPagerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import tabfragment.Mine;
import tabfragment.Workbench;
import utile.BaseActivity;
import utile.CheckVersion;
import utile.NoScrollViewPager;
import utile.PermissionUtil;
import utile.ShowToast;

import static utile.PermissionUtil.STORAGE_REQUESTCODE;

public class MainActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {
    @BindView(R.id.main_viewpager)
    NoScrollViewPager main_viewpager;
    @BindView(R.id.main_tablayout)
    TabLayout main_tablayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initData();
    }

    @Override
    protected void initData() {
        //设置tab
        SetTab();
//        AndPermission.with(this)
//                .requestCode(STORAGE_REQUESTCODE)
//                .permission(PermissionUtil.WriteFilePermission).send();
        checkWritePermissions();
    }

    /**
     * 检测读写权限
     */
    @AfterPermissionGranted(PermissionUtil.STORAGE_REQUESTCODE)
    private void checkWritePermissions() {
        String[] perms = {PermissionUtil.WriteFilePermission};
        if (EasyPermissions.hasPermissions(this, perms)) {//有权限
            CheckVersion checkVersion = new CheckVersion();
            checkVersion.CheckVersions(this, TAG);
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.permission_external_storage),
                    STORAGE_REQUESTCODE, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * 请求权限成功。
     *
     * @param requestCode
     * @param list
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted
//        CheckVersion checkVersion = new CheckVersion();
//        checkVersion.CheckVersions(this, TAG);
    }

    /**
     * 请求权限失败。
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setTitle(R.string.permission_title)
                    .setRationale(R.string.permission_tips)
                    .build()
                    .show();
        }
    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        AndPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults, listener);
//    }

//    //权限回调
//    private PermissionListener listener = new PermissionListener() {
//        @Override
//        public void onSucceed(int requestCode) {
//            if (requestCode == STORAGE_REQUESTCODE) {
//                //检查版本
//                CheckVersion checkVersion = new CheckVersion();
//                checkVersion.CheckVersions(MainActivity.this, TAG);
//            }
//        }
//
//        @Override
//        public void onFailed(int requestCode) {
//            if (requestCode == STORAGE_REQUESTCODE) {
//                ShowToast.showShort(MainActivity.this, "拒绝权限会导致某些功能不可用。");
//            }
//        }
//    };


    Handler handler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 0:
                    isExit = false;
                    break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 延迟发送退出
     */
    private void exit() {
        if (!isExit) {
            isExit = true;
            ShowToast.showShort(this, R.string.click_agin);
            // 利用handler延迟发送更改状态信息
            handler.sendEmptyMessageDelayed(0, send_time);
        } else {
            finish();
            System.exit(0);
        }
    }


    private void SetTab() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new Workbench());
        fragments.add(new Mine());
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments, new String[]{"工作台", "我的"});

        main_viewpager.setOffscreenPageLimit(1);

        main_viewpager.setAdapter(adapter);
        //关联图文
        main_tablayout.setupWithViewPager(main_viewpager);
        main_tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                main_viewpager.setCurrentItem(tab.getPosition(), false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        for (int i = 0; i < main_tablayout.getTabCount(); i++) {
            TabLayout.Tab tab = main_tablayout.getTabAt(i);
            Drawable d = null;
            switch (i) {
                case 0:
                    d = ContextCompat.getDrawable(this, R.drawable.workbench_tab);
                    break;
                case 1:
                    d = ContextCompat.getDrawable(this, R.drawable.mine_tab);
                    break;
            }
            tab.setIcon(d);
        }
    }
}
