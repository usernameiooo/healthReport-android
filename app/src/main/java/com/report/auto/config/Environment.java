package com.report.auto.config;


import com.report.auto.analyse.StringUtil;

/**用来发送消息的邮箱*/
public class Environment {
    String account;
    String authCode;//授权码
    int time;

    @Override
    public String toString() {
        return "Environment{" +
                "\naccount='" + account + '\'' +
                "\n, authCode='" + authCode + '\'' +
                "\n, time=" + time +
                "\n}";
    }

    public static Environment parseFromString(String str){
        try {
        Environment e=new Environment();
        e.account= StringUtil.getBetween(str,"account='","'");
        e.authCode=StringUtil.getBetween(str,", authCode='","'");
        e.time= e.parseInt(StringUtil.getBetween(str,", time=","\n"),10);
        if(check(e))
        return e;
        }catch (Exception e){
        }
        return null;
    }
    /**邮箱或授权码为空表示不通过邮箱发送消息*/
    public static boolean check(Environment e){
        if(e==null)return false;
        if(e.time<2)e.time=2;
        if(e.time>59)e.time=59;
        return true;
    }
    private int parseInt(String str,int def){
        try {
            return Integer.parseInt(str);
        }catch (Exception e){
            return def;
        }
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public void setTime(int time) {
        if(time<2)time=2;
        this.time = time;
    }

    public String getAccount() {
        return account;
    }

    public int getTime() {
        return time;
    }

    public String getAuthCode() {
        return authCode;
    }
    /**是否发送邮件*/
    public boolean isSendMail(){
        if(account==null||authCode==null)return false;
        if(account.isEmpty()||authCode.isEmpty())return false;
        return true;
    }
}
