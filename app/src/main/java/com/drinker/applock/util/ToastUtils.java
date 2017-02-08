package com.drinker.applock.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.drinker.applock.R;


/**
 * Created by zhuolin on 15-9-11.
 */
public class ToastUtils {
    static Toast toast;
    public static void showShortToast(Context context, String testStr) {
        if (context != null) {
            Toast.makeText(context, testStr, Toast.LENGTH_SHORT).show();
        }
    }
    public static void showShortToast(Context context, int testid) {

        if (context != null) {
            if(toast == null){
                toast = Toast.makeText(context, testid, Toast.LENGTH_SHORT);
                toast.show();
            }else{
                toast.setText(testid);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }

        }
    }

    public static void showShortToast(Context context, int icon, int testid) {
        if (context != null) {
            Toast toast = new Toast(context);
            LayoutInflater inflate = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflate.inflate(R.layout.toast_ly, null);
            TextView tv = (TextView)v.findViewById(R.id.toast_message);
            ImageView imageView = (ImageView) v.findViewById(R.id.toast_icon);
            imageView.setImageResource(icon);
            tv.setText(testid);
            toast.setView(v);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public static void showLongToast(Context context, String testStr) {
        if (context != null) {
            Toast.makeText(context, testStr, Toast.LENGTH_LONG).show();
        }
    }
    public static void showLongToast(Context context, int testid) {
        if (context != null) {
            Toast.makeText(context, testid, Toast.LENGTH_LONG).show();
        }
    }
}
