package com.stuffaboutcode.bluedot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.view.MotionEventCompat;

public class MonImage extends androidx.appcompat.widget.AppCompatImageView {
    protected Paint paint = new Paint();
    protected float[] lastTouchDownXY = new float[2];
    protected float[] get_pos = new float[6];
    protected boolean action_up = false;
    protected Context context;
    float x1, x2, y1, y2, x3, y3;
    private Segment segment;

    public MonImage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                x1 = event.getX();
                y1 = event.getY();
                Toast.makeText(context, "point de départ", Toast.LENGTH_LONG).show();
                invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                x2 = event.getX();
                y2 = event.getY();
                Toast.makeText(context, "point d'arrivée", Toast.LENGTH_LONG).show();
                invalidate();
                action_up = true;
                break;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
       if (action_up==true) {
        canvas.drawLine(x1, y1, x2, y2, paint);

        action_up = false;
       }
    }
}




/* segment.list.add(x1);
            segment.list.add(x2);
            segment.list.add(y1);
            segment.list.add(y2);

          for (int i = 3; ; i++) {
               canvas.drawLine(segment.list.get(i-3), segment.list.get(i-2),segment.list.get(i-1) ,segment.list.get(i), paint);}
            */