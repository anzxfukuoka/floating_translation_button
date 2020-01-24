package jp.anzx.wap_translatr_client;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;

class SelectionView extends View{

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint;
    private Paint mPaint;
    Context context;
    private Paint circlePaint;
    private Path circlePath;

    float startX, startY;
    float endX, endY;

    private SelectionListener selectionListener;

    public SelectionView(Context c) {
        super(c);
        context = c;

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.CYAN);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.parseColor("#66ff00ff"));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
        mPaint.setTextSize(20);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath( circlePath,  circlePaint);
    }

    private void touch_start(float x, float y) {

        startX = x;
        startY = y;
    }

    private void touch_move(float x, float y) {

        endX = x;
        endY = y;

        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mCanvas.drawRoundRect(startX, startY, endX, endY, 10, 10, mPaint);

        mCanvas.drawText("x: " + x + ",y: " + y, x, y, mPaint);

    }

    private void touch_up() {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        invalidate();

        //float x = event.getX();
        //float y = event.getY();

        //real coords
        PointF p = new PointF(event.getX(), event.getY());
        View v = this;// your view
        View root = v.getRootView();
        while (v.getParent() instanceof View && v.getParent() != root) {
            p.y += v.getTop();
            p.x += v.getLeft();
            v = (View) v.getParent();
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(p.x, p.y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(p.x, p.y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();

                if(selectionListener != null){
                    selectionListener.onSelectionStop(startX, startY, endX, endY);
                }

                break;
        }
        return true;
    }

    public void setSelectionListener(SelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }
}