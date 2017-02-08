package com.drinker.applock.applist.glide;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.drawable.DrawableResource;

import java.io.IOException;

/**
 * Created by liuzhuolin on 2016/11/4.
 */

public class AppIconDecoder implements ResourceDecoder<Drawable, Drawable> {
    @Override
    public Resource<Drawable> decode(Drawable source, int width, int height) throws IOException {
        return new DrawableResource<Drawable>(source) {
            @Override
            public int getSize() {
                if (drawable instanceof BitmapDrawable) {
                    return getByteSize(((BitmapDrawable)drawable).getBitmap());
                } else {
                    return 1;
                }
            }

            @Override
            public void recycle() {

            }

            private int getByteSize(Bitmap bitmap) {
                int count = 0;
                if (bitmap != null) {
                    if (Build.VERSION.SDK_INT >= 12) {
                        count = bitmap.getByteCount();
                    } else {
                        count = bitmap.getRowBytes() * bitmap.getHeight();
                    }
                }
                return count;
            }
        };
    }

    @Override
    public String getId() {
        return "";
    }
}
