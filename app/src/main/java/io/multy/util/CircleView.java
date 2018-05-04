package io.multy.util;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;

import io.multy.R;

public class CircleView extends View {

    boolean isColorSetted = false;
    Paint paint = new Paint();
    Bitmap btcBitmap;

    public CircleView(Context context) {
        super(context);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void generatePaintColor() {
        if (!isColorSetted) {
            paint.setColor(getRandomColor());
            invalidate();

            isColorSetted = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isColorSetted) {
            btcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_btc);
            float radius = getMeasuredWidth() / 2;
            canvas.drawCircle(radius, radius, radius, paint);
            canvas.drawBitmap(btcBitmap, radius - btcBitmap.getWidth() / 2, radius - btcBitmap.getHeight() / 2, null);
        }


    }

    private int getRandomColor() {
        int returnColor = Color.BLACK;
        int arrayId = getResources().getIdentifier("material_colors", "array", getContext().getPackageName());

        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.BLACK);
            colors.recycle();
        }
        return  returnColor;
    }

}
