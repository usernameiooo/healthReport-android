# healthReport-android
android版的健康上报程序，自动进行郑州大学防疫平台的健康上报。
仅作为辅助使用。如有异常情况，需要如实上报<br/>
使用URLConnection进行网络操作，登录平台进行健康上报；通过QQ邮箱发送上报结果。在Service中使用Timer进行定时任务，实现每天自动上报。不断发送任务栏通知表明运行状态<br/>
需要权限：网络、存储、清理任务白名单<br/>
在应用中进行配置后，生成配置文件，路径为手机内部存储/健康上报/健康上报配置.txt。<br/>
电脑版 https://github.com/usernameiooo/healthReport

v1.1更新:
  - 修改配置文件路径为/data/user/0/com.report.auto/files/健康上报/健康上报配置.txt，防止其他程序访问。卸载后删除配置文件
  - 可以设置不发送邮件通知
  - 应用运行状态的状态栏通知频率变为10分钟一次，并且1点到22点之间不通知
