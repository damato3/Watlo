package com.lowlightstudios.watlo.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatDrawableManager;

/**
 * Created by damato on 4/29/17.
 */

public class Utils {
    public final static int GROUND_WATER_RESULT = 10;
    public final static int NEWS_WATER_RESULT = 11;
    public final static int NEWS_WATER_NODE = 12;


    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static BitmapDrawable getBitmapDrawableFromVectorDrawable(Context context, int drawableId) {
        return new BitmapDrawable(context.getResources(), getBitmapFromVectorDrawable(context, drawableId));
    }
}
