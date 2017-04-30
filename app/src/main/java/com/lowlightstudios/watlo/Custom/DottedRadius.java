package com.lowlightstudios.watlo.Custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.lowlightstudios.watlo.R;
import com.lowlightstudios.watlo.core.Utils;

/**
 * Created by damato on 4/29/17.
 */

public class DottedRadius extends View {
    private final int color;
    private int width;
    private int height;
    private float radius;
    private Paint p;
    private BitmapDrawable pin;

    public final static float DASH_INTERVAL = 1.0f;
    private int pinXOffset;
    private int pinYOffset;

    public DottedRadius(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.DottedCircleView, 0, 0);
        color = a.getColor(R.styleable.DottedCircleView_circleColor, getResources().getColor(R.color.colorPrimary));
        radius = a.getInteger(R.styleable.DottedCircleView_radius, 0);
        a.recycle();

        setup();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);

        if (radius == 0) {
            radius = Math.min(width, height) / 2 - (int) p.getStrokeWidth();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(width / 2, height / 2, radius, p);
        //draw the map marker in middle of the circle
        canvas.drawBitmap(pin.getBitmap(), (width / 2) - pinXOffset, (height / 2) - pinYOffset, null);
        invalidate();
    }

    private void setup() {
        p = new Paint();
        p.setColor(getResources().getColor(R.color.colorPrimaryDark));
        p.setStrokeWidth(
                getResources().getDimension(R.dimen.dotted_circle_stroke_width));
        DashPathEffect dashPath = new DashPathEffect(new float[]{DASH_INTERVAL,
                DASH_INTERVAL}, (float) 1.0);
        p.setPathEffect(dashPath);
        p.setStyle(Paint.Style.STROKE);
        pin = Utils.getBitmapDrawableFromVectorDrawable(getContext(), R.drawable.ic_centerdot);
        pinXOffset = pin.getIntrinsicWidth() / 2;
        pinYOffset = pin.getIntrinsicHeight() / 2;
    }

    public float getCircleRadius() {
        return radius;
    }
}
