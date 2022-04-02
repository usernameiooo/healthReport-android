package com.report.auto;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.report.auto.client.MailSender;
import com.report.auto.config.User;
import com.report.auto.main.ConfigManager;

import java.io.File;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    //权限==================================
    private static final int REQUEST_CODE = 1024;
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 先判断有没有权限
            if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "获取失败", Toast.LENGTH_SHORT).show();
            }
        }
        String basePath = getBasePath();
        File dir=new File(basePath);
        if(!dir.exists())dir.mkdirs();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
            } else {
                Toast.makeText(this, "存储权限获取失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isIgnoring;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestIgnoreBatteryOptimizations() {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //权限==================================
    //view=================================
    TextView globalSettingView;
    LinearLayout userList;
    Button btnAddUser;
    private void initView(){
        globalSettingView=findViewById(R.id.global_setting);
        userList=findViewById(R.id.user_list);
        btnAddUser=findViewById(R.id.add_user);
        globalSettingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogForSetting();
            }
        });
        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogForNewUser();
            }
        });
    }
    private void dialogForSetting(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(this.getBaseContext()).inflate(R.layout.global_setting_dialog,null,false);
        EditText edt_mail = (EditText) view.findViewById(R.id.sender_mail);
        EditText edt_auth_code = (EditText) view.findViewById(R.id.auth_code);
        EditText edt_report_time = (EditText) view.findViewById(R.id.report_time);
        com.report.auto.config.Environment environment = configManager.getEnvironment();
        if(environment !=null){
            edt_mail.setText(environment.getAccount());
            edt_auth_code.setText(environment.getAuthCode());
            edt_report_time.setText(environment.getTime()+"");
        }
        dialog.setView(view);
        dialog.setNegativeButton("取消",null);
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String senderMail=(edt_mail).getText().toString();
                String mailAuthCode=(edt_auth_code).getText().toString();
                String time=edt_report_time.getText().toString();
                int time_=5;
                try {
                    time_=Integer.parseInt(time);
                    if(time_<=1)time_=2;
                }catch (Exception e){
                }
                if(time_>=60)time_=59;
                com.report.auto.config.Environment env=new com.report.auto.config.Environment();
                env.setAccount(senderMail);
                env.setTime(time_);
                env.setAuthCode(mailAuthCode);
                if(com.report.auto.config.Environment.check(env)){
                  configManager.setEnvironment(env);
                  setSettingInfoView(env);
                  configManager.saveConfig();
                  serviceRestart();
                }else {
                    toast("设置错误，不会保存");
                }
            }
        });
        dialog.show();
    }
    private void setSettingInfoView(com.report.auto.config.Environment env){
        globalSettingView.setText("发送消息的邮箱："+env.getAccount()+"\n" +
                "邮箱授权码："+env.getAuthCode()+"\n" +
                "上报时间：0:"+env.getTime());
    }
    private void dialogForNewUser(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(this.getBaseContext()).inflate(R.layout.add_user_dialog,null,false);
        dialog.setView(view);
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name=((EditText)view.findViewById(R.id.user_name)).getText().toString();
                String account=((EditText)view.findViewById(R.id.user_account)).getText().toString();
                String password=((EditText)view.findViewById(R.id.user_password)).getText().toString();
                String mail=((EditText)view.findViewById(R.id.user_mail)).getText().toString();
                User user=new User();
                user.setName(name);
                user.setMail(mail);
                user.setAccount(account);
                user.setPassword(password);
                if(User.check(user)){
                    if(!configManager.addUser(user)){
                        toast("学号重复，替换原来的用户");
                        refreshUserViews();
                    } else {
                        addUserView(user);
                    }
                    configManager.saveConfig();
                    serviceRestart();
                }else {
                    toast("设置错误，不会保存");
                }
            }
        });
        dialog.setNegativeButton("取消",null);
        dialog.show();
    }
    private void addUserView(User user){
        TextView tv=new TextView(this);
        tv.setText(
                user.getAccount()+" "+(user.getName()==null?"":user.getName())+
                "\n--------------------");
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogToEditUser(user);
            }
        });
        userList.addView(tv,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
    private void refreshUserViews(){
        userList.removeAllViews();
        Set<User> users = configManager.getUsers();
       for(User user:users){
           addUserView(user);
       }
    }
    private void dialogToEditUser(User user){
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(this.getBaseContext()).inflate(R.layout.edit_user_dialog,null,false);
        EditText edt_name=view.findViewById(R.id.user_name);
        EditText edt_account=view.findViewById(R.id.user_account);
        EditText edt_password=view.findViewById(R.id.user_password);
        EditText edt_mail=view.findViewById(R.id.user_mail);
        edt_name.setText(user.getName());
        edt_account.setText(user.getAccount());
        edt_mail.setText(user.getMail());
        edt_password.setText(user.getPassword());
        dialog.setView(view);
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name=edt_name.getText().toString();
                String account=edt_account.getText().toString();
                String password=edt_password.getText().toString();
                String mail=edt_mail.getText().toString();
                User nUser=new User();
                nUser.setName(name);
                nUser.setMail(mail);
                nUser.setAccount(account);
                nUser.setPassword(password);
                if(User.check(nUser)){
                    configManager.removeUser(user);
                    configManager.addUser(nUser);
                    refreshUserViews();
                    configManager.saveConfig();
                    toast("更改已保存");
                    serviceRestart();
                }else {
                    toast("设置错误，不会保存");
                }
            }
        });
        dialog.setNegativeButton("取消",null);
        dialog.show();
    }
    //view=================================
    public String getBasePath(){
        return Environment.getExternalStorageDirectory().getAbsolutePath()+"/健康上报";
    }
    public String getConfigFilePathname(){
        return getBasePath()+"/健康上报配置.txt";
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        requestPermission();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!isIgnoringBatteryOptimizations())requestIgnoreBatteryOptimizations();
        }
        System.out.println(getBasePath());
        File file=new File(getConfigFilePathname());
        if(file.exists()){
            System.out.println("读取配置文件");
            configManager.getConfig();
            setSettingInfoView(configManager.getEnvironment());
            refreshUserViews();
            if (configManager.checkIfCanRun()) {
                //确认配置，开始运行
                serviceStart();
            }else {
                //配置有误或不全
            }
        }else {
            System.out.println("未找到配置文件，需要进行设置");
        }
    }
    ConfigManager configManager=new ConfigManager(getConfigFilePathname());

    public void serviceStart(){
        if (!configManager.checkIfCanRun()){
            Toast.makeText(MainActivity.this,"配置不全或错误，不能运行",Toast.LENGTH_SHORT).show();
            return;
        }
        ReportService.setConfigManager(configManager);
        startService(new Intent(this,ReportService.class));
        Toast.makeText(MainActivity.this,"服务已启动",Toast.LENGTH_SHORT).show();
    }
    public void serviceStop(){
        stopService(new Intent(this,ReportService.class));
    }
    public void serviceRestart(){
        Toast.makeText(MainActivity.this,"服务重启中",Toast.LENGTH_SHORT).show();
        serviceStop();
        serviceStart();
    }
    private void toast(String info){
        Toast.makeText(MainActivity.this,info,Toast.LENGTH_SHORT).show();
    }
}