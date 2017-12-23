package dong.lan.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/**
 * @author: 梁桂栋
 * time: 2017/12/23 _ 22:13.
 * e-mail: 760625325@qq.com
 * GitHub: https://github.com/donlan
 * description: dong.lan.library
 * @version: 1.0
 */
class ElevationView extends View {
    private int elevation = 0;
    private int elevationColor = Color.GRAY;
    private Paint mPoint;
    private Path roundPath;
    public ElevationView(Context context) {
        super(context);
        mPoint = new Paint();
        mPoint.setAntiAlias(true);
        setLayerType(LAYER_TYPE_SOFTWARE,null);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPoint.setShadowLayer(elevation, 0, 0, elevationColor);
        canvas.drawPath(roundPath,mPoint);
    }
}
