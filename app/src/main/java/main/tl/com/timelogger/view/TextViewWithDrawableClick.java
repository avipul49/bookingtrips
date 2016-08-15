package main.tl.com.timelogger.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by vipulmittal on 30/06/16.
 */
public class TextViewWithDrawableClick extends TextView {

    private Drawable drawableRight;

    int actionX, actionY;

    private DrawableClickListener clickListener;

    public TextViewWithDrawableClick(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewWithDrawableClick(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top,
                                     Drawable right, Drawable bottom) {
        if (right != null) {
            drawableRight = right;
        }
        super.setCompoundDrawables(left, top, right, bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Rect bounds;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            actionX = (int) event.getX();
            actionY = (int) event.getY();

            if (drawableRight != null) {
                bounds = drawableRight.getBounds();

                int x, y;
                int extraTapArea = 13;

                x = (int) (actionX + extraTapArea);
                y = (int) (actionY - extraTapArea);

                x = getWidth() - x;

                if (x <= 0) {
                    x += extraTapArea;
                }

                if (y <= 0)
                    y = actionY;

                if (bounds.contains(x, y) && clickListener != null) {
                    clickListener
                            .onClick(DrawableClickListener.DrawablePosition.RIGHT);
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    return false;
                }
                return super.onTouchEvent(event);
            }

        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void finalize() throws Throwable {
        drawableRight = null;
        super.finalize();
    }

    public void setDrawableClickListener(DrawableClickListener listener) {
        this.clickListener = listener;
    }

    public interface DrawableClickListener {
        enum DrawablePosition {TOP, BOTTOM, LEFT, RIGHT}

        void onClick(DrawablePosition target);
    }
}