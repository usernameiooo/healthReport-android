package com.report.auto.main;


import com.report.auto.config.Environment;
import com.report.auto.config.User;
import com.report.auto.util.TxtHandler;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class ConfigManager {
    String pathname;

    public ConfigManager(String pathname) {
        this.pathname = pathname;
    }

    Set<User> users=new HashSet<>();
    Environment environment=null;
    public void getConfig(){
        TxtHandler.readLinesAndDoOperation(pathname, new TxtHandler.Operation<String>() {
            String str="";
            @Override
            public void doOperation(String value) {
                if(value.equals("User{")){
                    str=value+"\n";
                }else if(value.equals("}")){
                    if(str.startsWith("User{")){
                        User user=User.parseFromString(str);
                        if(User.check(user))
                        users.add(user);
                    }else if(str.startsWith("Environment{")){
                        Environment env = Environment.parseFromString(str);
                        if(Environment.check(env))environment=env;
                    }
                    str="";
                }else str=str+value+"\n";
            }
        });
    }
    public void saveConfig(){
        System.out.println("保存配置"+pathname);
        if(environment!=null)
        TxtHandler.write(pathname,environment.toString()+"\n",false);
        for(User user:users)
        TxtHandler.write(pathname,user.toString()+"\n",true);
    }
    public void setEnvironment(Environment env){
        this.environment=env;
    }
    /**确认配置无误，可以运行*/
    public boolean checkIfCanRun(){
        if(!Environment.check(environment)){
            System.out.println("全局设置为空，需要进行设置");
            return false;
        }
        if(users.size()==0){
            System.out.println("用户列表为空，需要添加用户");
            return false;
        }
        return true;
    }
    /**@return 是否是新增用户，而非重复添加（更新）
     * */
    public boolean addUser(User user){
       return users.add(user);
    }
    public void removeUser(User user){
        users.remove(user);
    }
    public String getPathname() {
        return pathname;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Set<User> getUsers() {
        return users;
    }
}
