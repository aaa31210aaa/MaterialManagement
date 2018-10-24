package tabfragment;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.administrator.materialmanagement.Login;
import com.example.administrator.materialmanagement.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import ui.About;
import ui.ModifyPwd;
import utile.AppUtils;
import utile.BaseFragment;
import utile.CheckVersion;
import utile.PermissionUtil;
import utile.SharedPrefsUtil;

import static utile.PermissionUtil.STORAGE_REQUESTCODE;

/**
 * A simple {@link Fragment} subclass.
 */
public class Mine extends BaseFragment implements EasyPermissions.PermissionCallbacks {
    private View view;
    @BindView(R.id.title_name)
    TextView title_name;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.modify_pwd)
    RelativeLayout modify_pwd;
    @BindView(R.id.mine_name)
    TextView mine_name;
    @BindView(R.id.mine_version_check)
    RelativeLayout mine_version_check;
    @BindView(R.id.version_check_tv)
    TextView version_check_tv;
    @BindView(R.id.mine_about)
    RelativeLayout mine_about;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;


    public Mine() {
        // Required empty public constructor
    }


    @Override
    public View makeView() {
        view = View.inflate(getActivity(), R.layout.fragment_mine, null);
        //绑定fragment
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void loadData() {
        back.setVisibility(View.GONE);
        title_name.setText("我的");
        version_check_tv.setText("当前版本：V" + AppUtils.getVersionName(getActivity()));
        mine_name.setText(SharedPrefsUtil.getValue(getActivity(), "userInfo", "username", ""));
        sp = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        editor = sp.edit();
    }

    @OnClick(R.id.modify_pwd)
    void ModifyPwd() {
        Intent intent = new Intent(getActivity(), ModifyPwd.class);
        intent.putExtra("tag", "modify");
        startActivity(intent);
    }


    @OnClick(R.id.mine_cancellation_rl)
    void Cancellation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.mine_cancellation_dialog_title);
        builder.setMessage(R.string.mine_cancellation_dialog_content);
        builder.setPositiveButton(R.string.mine_cancellation_dialog_btn2, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.remove("ISCHECK");
                editor.remove("PWDISCHECK");
                editor.remove("USER_NAME");
                editor.remove("PWD");
                editor.commit();
                Intent logoutIntent = new Intent(getActivity(), Login.class);
                logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(logoutIntent);
            }
        });

        builder.setNegativeButton(R.string.mine_cancellation_dialog_btn1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }


    /**
     * 检查版本更新
     */
    @OnClick(R.id.mine_version_check)
    void CheckVersion() {
        checkWritePermissions();
//        CheckVersion checkVersion = new CheckVersion();
//        checkVersion.CheckVersions(getActivity(), TAG);
    }


    /**
     * 检测读写权限
     */
    @AfterPermissionGranted(PermissionUtil.STORAGE_REQUESTCODE)
    private void checkWritePermissions() {
        String[] perms = {PermissionUtil.WriteFilePermission};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {//有权限
            CheckVersion checkVersion = new CheckVersion();
            checkVersion.CheckVersions(getActivity(), TAG);
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

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @OnClick(R.id.mine_about)
    void About() {
        Intent intent = new Intent(getActivity(), About.class);
        intent.putExtra("tag", "modify");
        startActivity(intent);
    }

}
