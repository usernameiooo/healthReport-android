package com.report.auto;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.report.auto.client.Client;
import com.report.auto.client.MailSender;
import com.report.auto.config.User;
import com.report.auto.main.ConfigManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ReportService extends Service {
    public static void setConfigManager(ConfigManager configManager) {
        ReportService.configManager = configManager;
        mailSender=new MailSender(configManager.getEnvironment().getAccount(),
                configManager.getEnvironment().getAuthCode());
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timer=new Timer();
        run();
        System.out.println("service "+this.toString()+" start");
    }
    public void run(){
        runEveryDay(configManager.getEnvironment().getTime());
    }
    static Client client=new Client();
    static MailSender mailSender;
    private static ConfigManager configManager=null;
    public void runOnceForUser(User user){
        String result=null;
        System.out.println("为"+user.getAccount()+
                (user.getName()!=null?"("+user.getName()+")":"")
                +"上报");
        try {
            result= client.reportHealth(user.getAccount(), user.getPassword());
        }catch (Exception e){
            e.printStackTrace();
            result=Client.REPORT_FAIL_START+"上报健康状况失败";
        }
        System.out.println(result);
        if(result.startsWith(Client.REPORT_FAIL_START))result+="，请手动上报";
        notifyReportResult("为"+user.getAccount()+"上报，",result);
        if(configManager.getEnvironment().isSendMail()&&user.isNotifyByMail())
        mailSender.sendMailInThread(user.getMail(),result,result);
    }
    private static final long PERIOD_DAY = 24*60*60*1000;
    private static Timer timer;
    public void runEveryDay(int min){
        Date date=new Date();
        date.setHours(0);
        date.setMinutes(min);
        date.setSeconds(1);
        if(date.before(new Date()))date.setTime(date.getTime()+PERIOD_DAY);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for(User user: configManager.getUsers()){
                    runOnceForUser(user);
                    notifyRunningState("健康上报 运行中...","下次上报将在"+smf(date));
                }
            }
        },10);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for(User user: configManager.getUsers()){
                    runOnceForUser(user);
                }
                date.setTime(date.getTime()+PERIOD_DAY);
                System.out.println("下次上报将在"+smf(date));
            }
        },date,PERIOD_DAY);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(shouldNotifyRunning())
                notifyRunningState("健康上报 运行中...","下次上报将在"+smf(date));
            }
        },100,10*60*1000);
    }
    private String smf(Date date){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
    /**每天1点到22点之间不再通知运行状态*/
    private boolean shouldNotifyRunning(){
        Date date=new Date();
        int hours = date.getHours();
        if(hours>=1&&hours<22)return false;
        return true;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        System.out.println("service "+this.toString()+" stop");
    }
    private void notifyReportResult(String title,String content){
        sendNotification(REPORT_RESULT_CHANNEL_ID,(title+content+System.currentTimeMillis()).hashCode(),title,content);
    }
    private void notifyRunningState(String title,String content){
        sendNotification(SERVICE_STATE_CHANNEL_ID,1333,title,content);

    }
    static String REPORT_RESULT_CHANNEL_ID="REPORT_RESULT_CHANNEL";
    static String SERVICE_STATE_CHANNEL_ID="SERVICE_STATE_CHANNEL";
    private void sendNotification(String channelId,int id,String title,String content){
        createChannel(REPORT_RESULT_CHANNEL_ID,"健康上报结果");
        createChannel(SERVICE_STATE_CHANNEL_ID,"程序运行情况");
        Context context=getApplicationContext();
        //和application通道的channedId名字要一样
        //设置TaskStackBuilder,点击通知栏跳转到指定页面，点击返回。返回到的页面
        //设置点击状态栏跳转的地方,详情页
        Intent intent = new Intent(context, MainActivity.class);
        //解决PendingIntent的extra数据不准确问题
        //注意下面的最后一个参数用PendingIntent.FLAG_UPDATE_CURRENT,否则传参有问题
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //通知管理类
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //设置通知属性，创建通知build，(创建channel通道已经在MApplication里面声明了)
        //使用默认的震动
        Notification notification = new NotificationCompat.Builder(context,channelId)
                .setContentTitle(title)//设置标题
                .setContentText(content)//消息内容
                .setWhen(System.currentTimeMillis())//发送时间
                .setSmallIcon(R.mipmap.ic_launcher_round) //设置图标
                .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)//设置默认的提示音，振动方式，灯光
                .setContentIntent(pendingIntent)//传值跳转的内容
                .setAutoCancel(true)//点击通知时是否自动消失，需要重点注意的是，setAutoCancel需要和setContentIntent一起使用，否则无效。
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.app_icon))
//                .setFullScreenIntent(pendingIntent,true)//悬挂式通知栏。但是在oppo8.0试了。通知会自动跳到intent的页面。网上说sdk设为29就没事了。android10.0测了不会自己跳转
                .build();
        notification.defaults = Notification.DEFAULT_ALL ;//设置为默认的声音
        //发送通知
        manager.notify(id, notification);
    }
    /**
     * 创建通道
     */
    private void createChannel(String id,String cName){
        NotificationManager  mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // 通知渠道的id
        // 用户可以看到的通知渠道的名字.
        // 用户可以看到的通知渠道的描述
        // String description = getString(R.string.channel_description);
        NotificationChannel channel = null;
        //Android8.0要求设置通知渠道
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(id, cName, NotificationManager.IMPORTANCE_HIGH);
            // 配置通知渠道的属性
            //        mChannel.setDescription(description);
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            // 设置通知出现时的震动（如果 android 设备支持的话）
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            //最后在notificationmanager中创建该通知渠道
            mNotificationManager.createNotificationChannel(channel);
        }
    }


}
