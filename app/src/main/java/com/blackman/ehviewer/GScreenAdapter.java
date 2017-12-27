package com.blackman.ehviewer;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * Created by hejinkun on 2017/12/20.
 */

public final class GScreenAdapter {
        public float density;
        private static final GScreenAdapter screenAdapter = new GScreenAdapter();
        private GScreenAdapter()
        {

        }

        public static GScreenAdapter instance()
        {
            return screenAdapter;
        }

        public int dip2px(float dpValue)
        {
            return (int)(dpValue * density + 0.5f) ;
        }

        public int px2dip(float pxValue)
        {
            return (int)(pxValue / density + 0.5f) ;
        }

        public int getScreenWidth(Activity activity){
            DisplayMetrics metric = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
            return  metric.widthPixels; // 屏幕宽度（像素）
        }
        public int getScreenHeight(Activity activity){
            DisplayMetrics metric = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
            return  metric.heightPixels; // 屏幕宽度（像素）
        }


}
