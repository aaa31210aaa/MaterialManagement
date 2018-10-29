package com.example.administrator.materialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;

import java.util.HashMap;
import java.util.Map;

import bean.UserInfo;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ui.ForgetPwd;
import utile.AppUtils;
import utile.BaseActivity;
import utile.DialogUtil;
import utile.JsonCallback;
import utile.PortIpAddress;
import utile.SharedPrefsUtil;
import utile.ShowToast;

public class Login extends BaseActivity {
    public static String ONE_CODE = "1"; //一级角色
    public static String SALE_CODE = "2"; //销售角色
    public static String AGENT_CODE = "3"; //经销商角色
    private Map<String, Integer> codeMap;
    @BindView(R.id.edit_username)
    EditText edit_username;
    @BindView(R.id.edit_password)
    EditText edit_password;
    @BindView(R.id.saveAccountCb)
    CheckBox saveAccountCb;
    @BindView(R.id.savePwdCb)
    CheckBox savePwdCb;
    @BindView(R.id.forget_pwd)
    TextView forget_pwd;
    @BindView(R.id.login_username_clear)
    ImageButton login_username_clear;
    @BindView(R.id.login_pwd_hidden)
    CheckBox login_pwd_hidden;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initData();
    }

    @Override
    protected void initData() {
        AppUtils.PwdNoSpace(edit_username);
        AppUtils.PwdNoSpace(edit_password);
        edit_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edit_username.getText().length() > 0) {
                    login_username_clear.setVisibility(View.VISIBLE);
                } else {
                    login_username_clear.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        login_pwd_hidden.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
//                    edit_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    edit_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    edit_password.setSelection(edit_password.getText().toString().length());
                } else {
//                  edit_password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    edit_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    edit_password.setSelection(edit_password.getText().toString().length());
                }
            }
        });


        SaveAccount();
        SavePwd();
        codeMap = new HashMap<>();
    }

    /**
     * 设置记住账号
     */
    private void SaveAccount() {
        //判断记住账号选框的状态
        if (SharedPrefsUtil.getValue(this, "userInfo", "ISCHECK", false)) {
            //设置默认是记住账号状态
            saveAccountCb.setChecked(true);

            edit_username.setText(SharedPrefsUtil.getValue(this, "userInfo", "USER_NAME", ""));
            if (edit_username.getText().toString().trim().equals("")) {
                edit_username.requestFocus();
            } else {
                edit_password.requestFocus();
            }
        }

        //监听保存账号的选择框
        saveAccountCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (saveAccountCb.isChecked()) {
                    SharedPrefsUtil.putValue(Login.this, "userInfo", "ISCHECK", true);
                } else {
                    SharedPrefsUtil.putValue(Login.this, "userInfo", "ISCHECK", false);
                }
            }
        });
    }

    /**
     * 设置记住密码
     */
    private void SavePwd() {
        //判断记住密码选框的状态
        if (SharedPrefsUtil.getValue(this, "userInfo", "PWDISCHECK", false)) {
            savePwdCb.setChecked(true);
            edit_password.setText(SharedPrefsUtil.getValue(this, "userInfo", "PWD", ""));

            if (edit_password.getText().toString().trim().equals("")) {
                edit_password.requestFocus();
            } else {
                edit_username.requestFocus();
            }
        }

        //监听保存密码的选框
        savePwdCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (savePwdCb.isChecked()) {
                    SharedPrefsUtil.putValue(Login.this, "userInfo", "PWDISCHECK", true);
                } else {
                    SharedPrefsUtil.putValue(Login.this, "userInfo", "PWDISCHECK", false);
                }
            }
        });
    }


    @OnClick(R.id.btn_login)
    void LoginBtn() {
        if (edit_username.getText().toString().trim().equals("") || edit_password.getText().toString().trim().equals("")) {
            ShowToast.showShort(this, R.string.account_pwd_no_empty);
        } else {
            dialog = DialogUtil.createLoadingDialog(Login.this, R.string.loading_write);
            //避免过快  没有加载效果
            handler.sendEmptyMessageDelayed(1, 1500);
        }
//        if (edit_username.getText().toString().trim().equals("") || edit_password.getText().toString().trim().equals("")) {
//            ShowToast.showShort(this, R.string.account_pwd_no_empty);
//        } else if (edit_username.getText().toString().equals("admin1") && edit_password.getText().toString().equals("123")) {
//            Intent intent = new Intent(this, MainActivity.class);
//            SharedPrefsUtil.putValue(Login.this, "userType", "code", ONE_CODE);
//            if (SharedPrefsUtil.getValue(Login.this, "userInfo", "ISCHECK", true))
//                SharedPrefsUtil.putValue(Login.this, "userInfo", "USER_NAME", edit_username.getText().toString());
//            if (SharedPrefsUtil.getValue(Login.this, "userInfo", "PWDISCHECK", true))
//                SharedPrefsUtil.putValue(Login.this, "userInfo", "PWD", edit_password.getText().toString());
//            startActivity(intent);
//            finish();
//        } else if (edit_username.getText().toString().equals("admin2") && edit_password.getText().toString().equals("123")) {
//            Intent intent = new Intent(this, MainActivity.class);
//            SharedPrefsUtil.putValue(Login.this, "userType", "code", SALE_CODE);
//            if (SharedPrefsUtil.getValue(Login.this, "userInfo", "ISCHECK", true))
//                SharedPrefsUtil.putValue(Login.this, "userInfo", "USER_NAME", edit_username.getText().toString());
//            if (SharedPrefsUtil.getValue(Login.this, "userInfo", "PWDISCHECK", true))
//                SharedPrefsUtil.putValue(Login.this, "userInfo", "PWD", edit_password.getText().toString());
//            startActivity(intent);
//            finish();
//        } else if (edit_username.getText().toString().equals("admin3") && edit_password.getText().toString().equals("123")) {
//            Intent intent = new Intent(this, MainActivity.class);
//            SharedPrefsUtil.putValue(Login.this, "userType", "code", AGENT_CODE);
//            if (SharedPrefsUtil.getValue(Login.this, "userInfo", "ISCHECK", true))
//                SharedPrefsUtil.putValue(Login.this, "userInfo", "USER_NAME", edit_username.getText().toString());
//            if (SharedPrefsUtil.getValue(Login.this, "userInfo", "PWDISCHECK", true))
//                SharedPrefsUtil.putValue(Login.this, "userInfo", "PWD", edit_password.getText().toString());
//            startActivity(intent);
//            finish();
//        } else {
//            ShowToast.showShort(this, R.string.account_pwd_err);
//        }
    }

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

    public Handler handler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 0:
                    isExit = false;
                    break;
                case 1:
                    mConnect();
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 调用登陆接口
     */
    private void mConnect() {
        OkGo.<UserInfo>get(PortIpAddress.Login())
                .tag(this)
                .params("loginname", edit_username.getText().toString())
                .params("loginpwd", edit_password.getText().toString())
                .execute(new JsonCallback<UserInfo>(UserInfo.class) {
                    @Override
                    public void onSuccess(Response<UserInfo> response) {
                        String tag = response.body().getSuccess();
                        String erro = response.body().getErrormessage();
                        String token = response.body().getAccess_token();
                        String username = response.body().getUsername();
                        String userid = response.body().getUserid();
                        String usertype = response.body().getUsertype();

                        SharedPrefsUtil.putValue(Login.this, "userInfo", "user_token", token);
                        SharedPrefsUtil.putValue(Login.this, "userInfo", "username", username);
                        SharedPrefsUtil.putValue(Login.this, "userInfo", "userpwd", edit_password.getText().toString());
                        SharedPrefsUtil.putValue(Login.this, "userInfo", "userid", userid);
                        //一级角色为1，销售角色为2，代理商角色为3
                        SharedPrefsUtil.putValue(Login.this, "userInfo", "usertype", usertype);

                        if (tag.equals(PortIpAddress.SUCCESS_CODE)) {
                            if (SharedPrefsUtil.getValue(Login.this, "userInfo", "ISCHECK", true)) {
                                SharedPrefsUtil.putValue(Login.this, "userInfo", "USER_NAME", edit_username.getText().toString());
                            }

                            if (SharedPrefsUtil.getValue(Login.this, "userInfo", "PWDISCHECK", true)) {
                                SharedPrefsUtil.putValue(Login.this, "userInfo", "PWD", edit_password.getText().toString());
                            }
                            startActivity(new Intent(Login.this, MainActivity.class));
                            finish();
                        } else {
                            ShowToast.showShort(Login.this, erro);
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(Response<UserInfo> response) {
                        super.onError(response);
                        dialog.dismiss();
                        ShowToast.showShort(Login.this, R.string.connect_err);
                    }
                });

    }


    /**
     * 忘记密码
     */
    @OnClick(R.id.forget_pwd)
    void ForgetPwd() {
        startActivity(new Intent(this, ForgetPwd.class));
    }


    /**
     * 清空账号栏
     */
    @OnClick(R.id.login_username_clear)
    void ClearAccount() {
        edit_username.setText("");
    }

    private boolean isChecked = false;

    /**
     * 显示隐藏密码
     */
//    @OnClick(R.id.login_pwd_hidden)
//    void PwdHidden(){
//
//    }

}
