package com.blackman.ehviewer.client;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hejinkun on 2017/12/12.
 */

public class LiFanNetTools {
    private static String url;
    private static List<Map<String,Object>> dataList;
    private static KJDayHotsListener dataListener;

    // 判断是否有可用的网络连接
    public static boolean isNetworkAvailable(Activity activity)
    {
        Context context = activity.getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        else
        {   // 获取所有NetworkInfo对象
            NetworkInfo[] networkInfo = cm.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0)
            {
                for (int i = 0; i < networkInfo.length; i++)
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;  // 存在可用的网络连接
            }
        }
        return false;
    }

    public static boolean getHotDays(Activity activity,KJDayHotsListener listener){
        if (isNetworkAvailable(activity)){
            url = KJUrl.liFanShaoNv;
            dataListener = listener;
            new Thread(runnable).start();
            return true;
        }else {
            return false;
        }
    }

    private static void handleHotDayData(Document doc){
        // 获取tbody元素下的所有tr元素
        Elements elements = doc.select("div.c_inner");
        Elements todayHots = elements.get(1).select("li");
        for(Element element : todayHots) {
//                String companyName = element.getElementsByTag("company").text();
            String companyName = element.select("img").attr("alt");
            String time = element.select("img").attr("src");
            String address = element.select("a.pic").attr("href");

            Map<String, Object> map = new HashMap<>();
            map.put("company", companyName);
            map.put("time", time);
            map.put("address", address);
            if (dataList == null){
                dataList = new ArrayList<>();
            }
            dataList.add(map);
        }
    }

    private static void handleShaoNv(Document doc){
        // 获取tbody元素下的所有tr元素
        Elements elements = doc.select("ul.abc2");
        Elements todayHots = elements.first().select("li");
        for(Element element : todayHots) {
            String name = element.select("b").text();
            String imgsrc = element.select("img").attr("src");
            String href = element.select("a.abc1").attr("href");

//            Map<String, Object> map = new HashMap<>();
//            map.put("company", companyName);
//            map.put("time", time);
//            map.put("address", address);
//            if (dataList == null){
//                dataList = new ArrayList<>();
//            }
//            dataList.add(map);
        }
    }


    private static Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Connection conn = Jsoup.connect(url);
            // 修改http包中的header,伪装成浏览器进行抓取
            conn.header("User-Agent", "Opera/9.80 (Android 6.1.0; Linux; Opera Mobi/build-1107180945; U; en-GB) Presto/2.8.149 Version/11.10");
            Document doc = null;
            try {
                doc = conn.get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (doc == null){
                return;
            }
//            handleHotDayData(doc);
            handleShaoNv(doc);
            // 执行完毕后给handler发送一个空消息
            handler.sendEmptyMessage(0);
        }
    };

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 收到消息后执行handler
            dataListener.getHotDays(dataList);
        }
    };
}
