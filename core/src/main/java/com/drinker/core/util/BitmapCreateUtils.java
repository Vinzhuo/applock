package com.drinker.core.util;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * Created by zhuolin on 15-11-11.
 */
public class BitmapCreateUtils {

    private static final String TAG = "BitmapCreateUtils";
    private static final String WALLPAPER = "wallpaper";
    private static final String SCALEDWALLPAPER = "scaled_wallpaper";
    private static final String BLURWALLPAPER = "blur_wallpaper";

    private static LruCache<String, Bitmap> lruCache = null;

    private static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }
        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } catch (Exception e) {
        }
        return bitmap;
    }

    public static Bitmap decode(Context context, View imageView, int resid) {
        if (context == null || resid <= 0) {
            return null;
        }
        try {
            Bitmap bitmap = getCacheBitmap(String.valueOf(resid));
            if (bitmap != null) {
                return bitmap;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(context.getResources(), resid, options);
            int width = getWidth(imageView);
            if (width <= 0) {
                width = options.outWidth;
            }
            int height = getHeight(imageView);
            if (height <= 0) {
                height = options.outHeight;
            }
            bitmap = decodeResources(context, options, resid, width * height);
            if (bitmap == null) {
                Drawable drawable = context.getResources().getDrawable(resid);
                bitmap = drawableToBitmap(drawable);
            }
            if (bitmap != null) {
                putCacheBitmap(String.valueOf(resid), bitmap);
            }
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    // pixes width * height
    public static Bitmap decode(Context context, int pixels, int resid) {
        if (context == null || resid <= 0) {
            return null;
        }
        Bitmap bitmap = getCacheBitmap(String.valueOf(resid));
        if (bitmap != null) {
            return bitmap;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resid, options);
        bitmap = decodeResources(context, options, resid, pixels);
        if (bitmap != null) {
            putCacheBitmap(String.valueOf(resid), bitmap);
        }
        return bitmap;
    }

    private static Bitmap decodeResources(Context context, BitmapFactory.Options options, int resid, int pixels) {
        int simpleSize = computeSampleSize(options, -1, pixels);
        options.inJustDecodeBounds = false;
        if (Build.VERSION.SDK_INT < 15) {
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        } else {
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        }
        if (Build.VERSION.SDK_INT <= 10) {
            try {
                Class cls = options.getClass();
                Field field = cls.getDeclaredField("inNativeAlloc");
//                field.setAccessible(true);
                field.set(options, true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        options.inPurgeable = true;
        options.inInputShareable = true;
        if (simpleSize > 1) {
            options.inSampleSize = simpleSize;
        }
        InputStream is = context.getResources().openRawResource(resid);
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
        return bitmap;
    }

    private static Bitmap decodeFile(BitmapFactory.Options options, String path, int pixels) {
        int simpleSize = computeSampleSize(options, -1, pixels);
        options.inJustDecodeBounds = false;
        if (Build.VERSION.SDK_INT < 15) {
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        } else {
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        }
        if (Build.VERSION.SDK_INT <= 10) {
            try {
                Class cls = options.getClass();
                Field field = cls.getDeclaredField("inNativeAlloc");
//                field.setAccessible(true);
                field.set(options, true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        options.inPurgeable = true;
        options.inInputShareable = true;
        if (simpleSize > 1) {
            options.inSampleSize = simpleSize;
        }
        Bitmap bitmap =  BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    private static synchronized Bitmap getCacheBitmap(String key) {
        Bitmap bitmap = getLruCache().get(key);
        return bitmap;
    }

    private static synchronized void putCacheBitmap(String key, Bitmap bitmap) {
        getLruCache().put(key, bitmap);
    }

    public static Bitmap getWallpaper(Context context) {
        Bitmap wallpaper = BitmapCreateUtils.getCacheBitmap(BitmapCreateUtils.WALLPAPER);
        if (wallpaper == null) {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
            Drawable wallpaperDrawable = wallpaperManager.getDrawable();
            Bitmap origin = drawableToBitmap(wallpaperDrawable);
            if (origin != null) {
                putCacheBitmap(WALLPAPER, origin);
            }
            return origin;
        } else {
            return wallpaper;
        }
    }
    public static Bitmap getScaledWallpaper(Activity context) {
        Bitmap scaledWallpaper = BitmapCreateUtils.getCacheBitmap(BitmapCreateUtils.SCALEDWALLPAPER);
        if (scaledWallpaper == null) {
            Rect frame = new Rect();
            context.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            int statusBarHeight = frame.top;
            int navigationBarHeight = 0;
            Resources rs = context.getResources();
            int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
            if (id > 0) {
                navigationBarHeight = rs.getDimensionPixelSize(id);
            }
            Bitmap origin = getWallpaper(context);
            if (origin != null) {
                Bitmap bm = Bitmap.createBitmap(origin,0,statusBarHeight,origin.getWidth(),origin.getHeight()-statusBarHeight-navigationBarHeight);
                if (bm != null) {
                    putCacheBitmap(SCALEDWALLPAPER, bm);
                    return bm;
                }
            }
            return null;
        } else {
            return scaledWallpaper;
        }
    }
    public static Bitmap getBlurWallpaper(Context context) {
        Bitmap bitmap = getCacheBitmap(BLURWALLPAPER);
        if (bitmap == null) {
            Bitmap bm = getWallpaper(context);
            if (bm != null) {
                int radius = 4;
                int factor = 40;
                Bitmap scaled_bm = Bitmap.createScaledBitmap(bm, bm.getWidth() / factor, bm.getHeight() / factor, false);
                if (scaled_bm != null) {
                    Bitmap blur_bm = FastBlur.doBlur(scaled_bm, radius, false);
                    if (blur_bm != null) {
                        putCacheBitmap(BLURWALLPAPER, blur_bm);
                        return blur_bm;
                    }
                }
            }
            return null;
        } else {
            return bitmap;
        }
    }

    private static LruCache<String, Bitmap> getLruCache() {
        if (lruCache == null) {
            // LruCache通过构造函数传入缓存值，以KB为单位。
            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            // 使用最大可用内存值的1/8作为缓存的大小。
            int cacheSize = maxMemory / 9;
            lruCache = new LruCache(cacheSize);
        }
        return lruCache;
    }

    //pixels width * height
    public static Bitmap decode(Context context, int pixels, File file) {
        if (context == null || file == null) {
            return null;
        }
        String path = file.getAbsolutePath();
        Bitmap bitmap = getCacheBitmap(path);
        if (bitmap != null) {
            return bitmap;
        }
        if (!file.exists()) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        bitmap = decodeFile(options, path, pixels);
        if (bitmap != null) {
            putCacheBitmap(path, bitmap);
        }
        return bitmap;
    }

    public static Bitmap decode(Context context, View view, File file) {
        if (context == null || file == null) {
            return null;
        }
        try {
            String path = file.getAbsolutePath();
            Bitmap bitmap = getCacheBitmap(path);
            if (bitmap != null) {
                return bitmap;
            }
            if (!file.exists()) {
                return null;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            int width = getWidth(view);
            if (width <= 0) {
                width = options.outWidth;
            }
            int height = getHeight(view);
            if (height <= 0) {
                height = options.outHeight;
            }
            bitmap = decodeFile(options, path, width * height);
            if (bitmap != null) {
                putCacheBitmap(path, bitmap);
            }
            return bitmap;
        }catch(Exception e){
            return null;
        }
    }

    private static int getWidth(View imageView) {
        if (imageView == null) {
            return 0;
        }
        int width = imageView.getWidth();
        if (width <= 0) {
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            if(params != null) {
                width = params.width;
            }
            if (width <= 0) {
                width = getImageViewFieldValue(imageView, "mMaxWidth");
            }
        }
        if (width <= 0) {
            width = 0;
        }
        return width;
    }

    private static int getHeight(View imageView) {
        if (imageView == null) {
            return 0;
        }
        int height = imageView.getHeight();
        if (height <= 0) {
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            if(params != null) {
                height = params.height;
            }
            if (height <= 0) {
                height = getImageViewFieldValue(imageView, "mMaxHeight");
            }
        }
        if (height <= 0) {
            height = 0;
        }
        return height;
    }

    private static int getImageViewFieldValue(Object object, String fieldName) {
        int value = 0;

        try {
            Field e = ImageView.class.getDeclaredField(fieldName);
            e.setAccessible(true);
            int fieldValue = ((Integer)e.get(object)).intValue();
            if(fieldValue > 0 && fieldValue < 2147483647) {
                value = fieldValue;
            }
        } catch (Exception e) {
        }

        return value;
    }

    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        try{
            double w = options.outWidth;
            double h = options.outHeight;
            int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
            int upperBound = (minSideLength == -1) ? 128 :(int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
            if (upperBound < lowerBound) {
                // return the larger one when there is no overlapping zone.
                return lowerBound;
            }
            if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
                return 1;
            } else if (minSideLength == -1) {
                return lowerBound;
            } else {
                return upperBound;
            }
        }catch(Exception ex){
            return 0;
        }
    }

}
