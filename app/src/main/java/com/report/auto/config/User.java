package com.report.auto.config;



import com.report.auto.analyse.StringUtil;

import java.util.Objects;

public class User {
   String account;
   String password;
   String name;
   String mail;
    @Override
    public String toString() {
        return "User{" +
                "\naccount='" + account + '\'' +
                "\n, password='" + password + '\'' +
                "\n, name='" + name + '\'' +
                "\n, mail='" + mail + '\'' +
                "\n}";
    }
    public static User parseFromString(String str){
        try {
            User user = new User();
            user.account = StringUtil.getBetween(str, "account='", "'");
            user.password = StringUtil.getBetween(str, ", password='", "'");
            user.name = StringUtil.getBetween(str, ", name='", "'");
            user.mail = StringUtil.getBetween(str, ", mail='", "'");
            if (check(user))return user;
        }catch (Exception e){}
        return null;
    }
    /**mail为空时不使用邮箱通知*/
    public static boolean check(User user){
        if(user==null)return false;
        if(user.account==null||user.password==null)return false;
        if(user.account.isEmpty()||user.password.isEmpty())return false;
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(account, user.account);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getAccount() {
        return account;
    }

    public String getMail() {
        return mail;
    }
    /**是否用邮箱通知*/
    public boolean isNotifyByMail(){
        return mail != null && !mail.isEmpty();
    }
}
