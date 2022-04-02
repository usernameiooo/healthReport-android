package com.report.auto.client;


import com.report.auto.analyse.StringUtil;

import java.util.List;

public class Client {
    SourceCode sourceCode = new SourceCode();
    private String get(String url,boolean canJump){
       /* try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        StringBuffer result = sourceCode.getSourceCodeInGet(url, canJump);
        if(result==null)return null;
        return result.toString();
    }
    private String get(String url){
        return get(url,true);
    }
    private String post(String url,String para){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        StringBuffer result = sourceCode.getSourceCodeInPost(url, para);
        if(result==null)return null;
        return result.toString();
    }
    /**提取form中的表单域的name和value，形成如同url get 参数的字符串*/
    private String makeUpFormValues(String form){
            /*<input type="hidden" name="did" value="1" />
            <input type="hidden" name="door" value="" />
            <input type="hidden" name="men6" value="a" />
            <input type="hidden" name="ptopid" value="s4ADE1BFB948B41E6BF7C113B416C0637">
            <input type="hidden" name="sid" value="220303113241906870">*/
        String format="";
        List<String> tags = StringUtil.getSpiltList(form, "<", ">");
        for(String tag:tags){
            tag=tag.trim();
            if(tag.startsWith("input")){
                String name = StringUtil.getBetween(tag, "name=\"", "\"");
                String value=StringUtil.getBetween(tag,"value=\"", "\"");
                String type=StringUtil.getBetween(tag,"type=\"","\"");
                if("radio".equals(type)&&!tag.contains("checked"))continue;
                if("button".equals(type))continue;
                format+=("&"+name+"="+value);
            }else if(tag.startsWith("select")){
                String name=StringUtil.getBetween(tag,"name=\"","\"");
                String select=StringUtil.getBetween(form,tag,"</select>");
                List<String> options = StringUtil.getSpiltList(select, "<option", "</option>");
                for(String option:options){
                    if(option.contains("selected")){
                        String value = StringUtil.getBetween(option, "value=\"", "\"");
                        if(!value.equals("")) format+=("&"+name+"="+value);
                    }
                }
            }
        }
        if(!format.equals(""))format=format.substring(1);
        return format;
    }
    String startUrl="https://jksb.v.zzu.edu.cn/";
    public static final String REPORT_FAIL_START="上报失败:";
    public static final String REPORT_SUCCESS_START="上报成功:";
    public String reportHealth(String account,String password) {
         //1.打开网址
        info("get "+startUrl,"打开网址");
        String login = get(startUrl);
        if(login==null)return REPORT_FAIL_START+"打开网址失败";
        String longinFormPage = StringUtil.getBetween(login, "<iframe name=\"my_toprr\" src=\"", "\" marginwidth=\"0\"");
        //2.获取登录表单(属于第1步“打开网址”的内嵌部分)
        info("get "+longinFormPage,"获取登录表单");
        String loginFormPageHtml = get(longinFormPage);
        if(loginFormPageHtml==null)return REPORT_FAIL_START+"获取登录表单失败";
        String loginActionUrl = StringUtil.getBetween(loginFormPageHtml, "<form method=\"POST\" action=\"", "\" name=\"myform52\">");
        //3.点击进入健康上报平台按钮,登录
        info("post "+loginActionUrl,"登录");
        String afterLoginHtml = post(loginActionUrl, "uid="+account+"&upw="+password);
        if(afterLoginHtml==null)return REPORT_FAIL_START+"登录失败";
        String redirection=StringUtil.getBetween(afterLoginHtml,"{if (num == 0){parent.window.location=\"","\"}}");
        //4.根据js引导重定向到登录后的页面，“师生员工个人健康上报主页”
        info("get "+redirection,"重定向");
        String redirectionHtml=get(redirection);
        if(redirectionHtml==null)return REPORT_FAIL_START+"登录后重定向失败";
        String iframeUrl=StringUtil.getBetween(redirectionHtml,"<iframe name=\"zzj_top_6s\" id=\"zzj_top_6s\" src=\"","\" marginwidth=\"0\"");
        //获取“师生员工个人健康上报主页”的内嵌部分
        info("get "+iframeUrl,"获取主页");
        String iframeUrlHtml=get(iframeUrl);
        if (iframeUrlHtml==null)return REPORT_FAIL_START+"获取主页失败";
        String reportActionformParas = StringUtil.getBetween(iframeUrlHtml, "</iframe>\n" +
                "</div>", "</form>");
        reportActionformParas=makeUpFormValues(reportActionformParas);
        String reportActionUrl=StringUtil.getBetween(iframeUrlHtml,"<form method=\"POST\" name=\"myform52\" action=\"","\">");
        //点击“本人填报”按钮
        info("post "+reportActionUrl,"打开上报页面");
        String reportActionHtml = post(reportActionUrl, reportActionformParas);
        if (reportActionHtml==null)return REPORT_FAIL_START+"打开上报页面失败";
        String realReportActionUrl=StringUtil.getBetween(reportActionHtml," name=\"myform52\" action=\"","\">");
        String realReportFrom=StringUtil.getBetween(reportActionHtml,"<div style=\"width:100%;min-height:126px;line-height:21px;font-size:14px;color:#333\">",
                "</form>\n" +
                        "<script Language=\"javascript\">");
        //获取之前的填报数据
        String realReportParas=makeUpFormValues(realReportFrom);
        // realReportParas=realReportParas.replace("&memo22=河南.郑州市*","");
        if(!realReportParas.contains("myvs_13=g"))realReportParas=realReportParas+"&myvs_13=g";
        //点击“确认按钮”，提交填报数据
        info("post "+realReportActionUrl,"上报");
        String result = post(realReportActionUrl, realReportParas);
        if(result==null)return REPORT_FAIL_START+"提交数据失败";
        if(result.contains("感谢你今日上报健康状况"))return REPORT_SUCCESS_START+"成功上报健康状况";
        else if(result.contains("今日您已经填报过了"))return REPORT_SUCCESS_START+"已经上报过";
        else return REPORT_FAIL_START+"提交数据失败";
    }
    boolean showInfo=true;
    void info(String ...info){
        if(showInfo) {
            for(String s:info)
            System.out.print(s+" ");
        }
        System.out.println();
    }
}
