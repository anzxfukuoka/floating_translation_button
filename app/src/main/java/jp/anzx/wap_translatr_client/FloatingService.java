package jp.anzx.wap_translatr_client;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class FloatingService extends Service {
    public static final String TAG = "WAPT_FloatingService";

    private WindowManager mWindowManager;
    private ImageView image;// floating btn

    private SelectionView selectionView;

    @SuppressLint("ClickableViewAccessibility")
    public void onCreate() {
        super.onCreate();

        mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

        image = new ImageView(this);
        image.setImageResource(R.drawable.ic_launcher_background);

        selectionView = new SelectionView(this);


        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        //floating btn params
        final LayoutParams paramsF = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG, //LayoutParams.TYPE_PHONE,
                LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        paramsF.gravity = Gravity.TOP | Gravity.LEFT;
        paramsF.x=0;
        paramsF.y=100;

        //selection view params
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();

        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        window.getDefaultDisplay().getRealMetrics(metrics);

        final LayoutParams selectionParams = new WindowManager.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                LAYOUT_FLAG,
                LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        paramsF.gravity = Gravity.TOP | Gravity.LEFT;
        paramsF.x=0;
        paramsF.y=0;


        image.setOnTouchListener(new View.OnTouchListener() {
            WindowManager.LayoutParams paramsT = paramsF;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        initialX = paramsF.x;
                        initialY = paramsF.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if((int) (event.getRawX() - initialTouchX) < 10 &&
                                (int) (event.getRawY() - initialTouchY) < 10){
                            Log.i(TAG, "click!");
                            Toast.makeText(FloatingService.this, "click!", Toast.LENGTH_SHORT).show();

                            //add selection view
                            mWindowManager.addView(selectionView, selectionParams);

                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                        paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(v, paramsF);
                        break;
                }
                return false;
            }
        });

        selectionView.setSelectionListener(new SelectionListener() {
            @Override
            public void onSelectionStop(float startX, float startY, float endX, float endY) {

                //save coordnts
                Things.start = new Point((int) startX, (int) startY);
                Things.end = new Point((int) endX, (int) endY);

                //delete selection view
                mWindowManager.removeView(selectionView);

                //taking and cropping screenshot
                Intent i = new Intent(getApplicationContext(), ScreenCaptureService.class);
                startService(i);
            }
        });

        mWindowManager.addView(image, paramsF);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
