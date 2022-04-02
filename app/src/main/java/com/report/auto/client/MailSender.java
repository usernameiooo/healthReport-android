package com.report.auto.client;

import com.sun.mail.util.MailSSLSocketFactory;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.GeneralSecurityException;
import java.util.Properties;

public class MailSender {
    String sender;
    String auth;

    public MailSender(String sender, String auth) {
        this.sender = sender;
        this.auth = auth;
    }

    /**发送QQ邮件
         * @param sender 发送方的邮箱
         * @param auth qq邮箱中申请的16位授权码
         * @param to 接收人邮箱
         * @param title 邮件标题
         * @param content 邮件内容
         * */
        public static void sendMail(String sender,String auth,String to,String title,String content) throws GeneralSecurityException, javax.mail.MessagingException {
            //创建一个配置文件并保存
            Properties properties = new Properties();
            properties.setProperty("mail.host","smtp.qq.com");
            properties.setProperty("mail.transport.protocol","smtp");
            properties.setProperty("mail.smtp.auth","true");
            //QQ存在一个特性设置SSL加密
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.ssl.socketFactory", sf);

            //创建一个session对象
            Session session = Session.getDefaultInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(sender,auth);
                }
            });
            //关闭debug模式
            session.setDebug(false);
            //获取连接对象
            Transport transport = session.getTransport();
            //连接服务器
            transport.connect("smtp.qq.com",sender,auth);
            //创建邮件对象
            MimeMessage mimeMessage = new MimeMessage(session);
            //邮件发送人
            mimeMessage.setFrom(new InternetAddress(sender));
            //邮件接收人
            mimeMessage.setRecipient(Message.RecipientType.TO,new InternetAddress(to));
            //邮件标题
            mimeMessage.setSubject(title);
            //邮件内容
            mimeMessage.setContent(content,"text/html;charset=UTF-8");
            //发送邮件
            transport.sendMessage(mimeMessage,mimeMessage.getAllRecipients());
            //关闭连接
            transport.close();
        }
        public static void  sendMailInThread(String sender,String auth,String to,String title,String content){
            new Thread(()->{
                try {
                    sendMail(sender,auth,to,title,content);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        public void  sendMailInThread(String to,String title,String content){
            sendMailInThread(this.sender,this.auth,to,title,content);
        }
}
