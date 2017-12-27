package com.blackman.ehviewer.client.data;

import com.blackman.ehviewer.client.APPConfig;

import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hejinkun on 2017/12/19.
 */

public class DateTools {

    public static boolean ifExpire(Date startDate,String type){
        Date endDate = getEndDateBy(startDate,type);
        Date nowDate = getWebsiteDatetime();
        if (endDate != null && nowDate != null){
            return (nowDate.getTime() > endDate.getTime());
        }else {
            return false;
        }

    }

    public static Date getEndDateBy(Date startDate,String type){
        if (type.equals("m")){
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.add(Calendar.MONTH, +1);
            APPConfig.endDate = cal.getTime();
//            Log.d("kobehjk", "getEndDateBy: "+ date);
            return cal.getTime();
        }else if (type.equals("e")) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, +100);
            APPConfig.endDate = cal.getTime();
            return cal.getTime();
        }else {
            return null;
        }
    }

    public static String dateToString(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = simpleDateFormat.format(date);
        return dateString;
    }


        public static Date getWebsiteDatetime() {
            try {

                    URL url = new URL("http://www.baidu.com");// 获取url对象
                    URLConnection uc = url.openConnection();// 获取生成连接对象
                    uc.connect();// 发出连接请求
                    long ld = uc.getDate();// 读取网站日期时间
                    Date date = new Date(ld);// 转化为时间对象

                    return date;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }

        public static Date stringToDate(String time){
            Date tempDate = null;
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                tempDate = sdf1.parse(time);
            }catch (Exception e1){

            }
            return tempDate;
        }


}
