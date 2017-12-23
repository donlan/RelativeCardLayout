/*
 *   Copyright 2016, donlan(梁桂栋)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *   Email me: stonelavender@hotmail.com
 */

package dong.lan.library;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;


/**
 * Created by 梁桂栋 on 17-3-25 ： 下午3:55.
 * Email:       760625325@qq.com
 * GitHub:      github.com/donlan
 * description: SmartTrip
 */

public class RelativeCarView extends RelativeLayout {

    private static final String TAG = RelativeCarView.class.getSimpleName();
    private int elevation = 0;
    private int elevationColor = Color.GRAY;
    private int backgroundColor = Color.WHITE;
    private int radius = 0;
    private int marginX = 0;
    private int marginY = 0;
    private int strokeWidht = 0;
    private int strokeColor = Color.TRANSPARENT;
    private boolean isAnim = false;
    private Paint paint;
    private RectF bound;
    private int startColor;
    private int centerColor;
    private int endColor;
    private boolean isUseGradient = false;
    private float[] radii = new float[8];
    private Path mClipPath;
    private Region mAreaRegion;
    private Paint elevationPaint;

    public RelativeCarView(Context context) {
        this(context, null);
    }

    public RelativeCarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RelativeCarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.RelativeCarView);
            backgroundColor = ta.getColor(R.styleable.RelativeCarView_rcv_backgroundColor, Color.WHITE);
            radius = ta.getDimensionPixelSize(R.styleable.RelativeCarView_rcv_radius, 0);
            elevation = ta.getDimensionPixelSize(R.styleable.RelativeCarView_rcv_elevation, 0);
            elevationColor = ta.getColor(R.styleable.RelativeCarView_rcv_elevationColor, Color.GRAY);
            isAnim = ta.getBoolean(R.styleable.RelativeCarView_rcv_anim, false);
            marginX = ta.getDimensionPixelSize(R.styleable.RelativeCarView_rcv_marginX, marginX);
            marginY = ta.getDimensionPixelSize(R.styleable.RelativeCarView_rcv_marginY, marginY);
            startColor = ta.getColor(R.styleable.RelativeCarView_rcv_gradientStartColor, backgroundColor);
            centerColor = ta.getColor(R.styleable.RelativeCarView_rcv_gradientCenterColor, backgroundColor);
            endColor = ta.getColor(R.styleable.RelativeCarView_rcv_gradientEndColor, backgroundColor);
            isUseGradient = ta.getBoolean(R.styleable.RelativeCarView_rcv_userGradient, false);
            strokeWidht = (int) ta.getDimension(R.styleable.RelativeCarView_rcv_stroke_width, 0);
            strokeColor = ta.getColor(R.styleable.RelativeCarView_rcv_stroke_color, Color.TRANSPARENT);
            int roundCornerTopLeft = ta.getDimensionPixelSize(
                    R.styleable.RelativeCarView_rcv_corner_top_left, radius);
            int roundCornerTopRight = ta.getDimensionPixelSize(
                    R.styleable.RelativeCarView_rcv_corner_top_right, radius);
            int roundCornerBottomLeft = ta.getDimensionPixelSize(
                    R.styleable.RelativeCarView_rcv_corner_bottom_left, radius);
            int roundCornerBottomRight = ta.getDimensionPixelSize(
                    R.styleable.RelativeCarView_rcv_corner_bottom_right, radius);

            radii[0] = roundCornerTopLeft;
            radii[1] = roundCornerTopLeft;

            radii[2] = roundCornerTopRight;
            radii[3] = roundCornerTopRight;

            radii[4] = roundCornerBottomRight;
            radii[5] = roundCornerBottomRight;

            radii[6] = roundCornerBottomLeft;
            radii[7] = roundCornerBottomLeft;


            ta.recycle();
        }

        mClipPath = new Path();
        mAreaRegion = new Region();

        bound = new RectF(0, 0, 0, 0);
        paint = new Paint();
        elevationPaint = new Paint();
        elevationPaint.setAntiAlias(true);
        paint.setAntiAlias(true);
        elevationPaint.setShadowLayer(elevation, 0, 0, elevationColor);

        if (elevation > 0) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }


    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        super.setBackgroundColor(backgroundColor);
    }

    public void setRadius(int radius) {
        this.radius = radius;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (elevation <= 0) {
            bound.left = getPaddingLeft() + marginX;
            bound.top = getPaddingTop() + marginY;
            bound.right = w - getPaddingRight() - marginX;
            bound.bottom = h - getPaddingBottom() - marginY;
        } else {
            bound.left = marginX+elevation;
            bound.top = marginY+elevation;
            bound.right = w-marginX - elevation;
            bound.bottom = w - marginY - elevation;
        }

        mClipPath.reset();
        mClipPath.addRoundRect(bound, radii, Path.Direction.CW);
        Region clip = new Region((int) bound.left, (int) bound.top,
                (int) bound.right, (int) bound.bottom);
        mAreaRegion.setPath(mClipPath, clip);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        /**
         * 测试情况：Android 4.4
         * setLayerType 为 LAYER_TYPE_SOFTWARE 时 PorterDuffXfermode会失效
         * 需要绘制Shader 需要在 LAYER_TYPE_SOFTWARE的情况下才有效
         */

        if (elevation > 0) {
            paint.setColor(Color.TRANSPARENT);
            elevationPaint.setStyle(Paint.Style.FILL);
            canvas.drawPath(mClipPath, elevationPaint);
        }
        if (paint.getShader() == null && isUseGradient) {
            LinearGradient gradient = new LinearGradient(0, getHeight() / 2, getWidth(), getHeight() / 2,
                    new int[]{startColor, centerColor, endColor},
                    new float[]{0, 0.5f, 1},
                    Shader.TileMode.CLAMP);
            paint.setShader(gradient);
        }
        paint.setColor(backgroundColor);
        canvas.drawPath(mClipPath, paint);
        canvas.saveLayer(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), null, Canvas
                .ALL_SAVE_FLAG);
        super.dispatchDraw(canvas);
        if (strokeWidht > 0) {
            paint.setShader(null);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
            paint.setStrokeWidth(strokeWidht);
            paint.setColor(strokeColor);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mClipPath, paint);
        }

        if (elevation <= 0) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(mClipPath, paint);
        }
        canvas.restore();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!mAreaRegion.contains((int) event.getX(), (int) event.getY())) {
            return false;
        }
        if (isAnim && event.getAction() == MotionEvent.ACTION_DOWN) {
            ObjectAnimator.ofFloat(this, "scaleX", 1.0f, 0.9f, 1.0f).setDuration(200).start();
            ObjectAnimator.ofFloat(this, "scaleY", 1.0f, 0.9f, 1.0f).setDuration(200).start();
            ObjectAnimator.ofFloat(this, "alpha", 1.0f, 0.5f, 1.0f).setDuration(200).start();
        }
        return super.dispatchTouchEvent(event);
    }

}
