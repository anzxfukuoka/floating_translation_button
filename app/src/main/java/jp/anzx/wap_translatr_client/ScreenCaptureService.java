package jp.anzx.wap_translatr_client;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

public class ScreenCaptureService extends Service {

    public static final String TAG = "WAPT_ScreenCaptureService";

    private int resultCode;
    private Intent data;

    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;

    int mWidth;
    int mHeight;
    int mDensity;

    private ImageReader imageReader;
    private DisplayMetrics metrics;
    private Handler handler;

    public ScreenCaptureService() {
    }

    @Override
    public int onStartCommand(Intent intent, int intentFlags, int startId) {

        Log.i(TAG, "ScreenCaptureService started, id: " + startId);

        //resultCode = intent.getIntExtra(EXTRA_M_RESULT_CODE, 4200);
        //data = intent.getParcelableExtra(EXTRA_M_DATA);

        resultCode = Things.resultCode;
        data = Things.data;

        //screen
        metrics = getApplicationContext().getResources().getDisplayMetrics();

        //WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        //window.getDefaultDisplay().getRealMetrics(metrics);

        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;
        mDensity = metrics.densityDpi;

        //Log.i(TAG, mWidth + " " + mHeight);


        /*Thread thread = new Thread(){
            public void run(){
                while (true){
                    takeScreenshot();
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();
        */

        takeScreenshot();

        return super.onStartCommand(intent, intentFlags, startId);
    }

    void takeScreenshot(){

        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);

        //screenshot
        imageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        //handler = new Handler();
        handler = new Handler(Looper.getMainLooper());

        mediaProjection.createVirtualDisplay("screen-mirror",
                mWidth,
                mHeight,
                mDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                        | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
                        | DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                //DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(),
                null,
                handler);

        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                reader.setOnImageAvailableListener(null, handler);

                Image image = reader.acquireLatestImage();

                final Image.Plane[] planes = image.getPlanes();
                final ByteBuffer buffer = planes[0].getBuffer();

                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * metrics.widthPixels;
                // create bitmap
                Bitmap bmp = Bitmap.createBitmap(metrics.widthPixels + (int) ((float) rowPadding / (float) pixelStride), metrics.heightPixels, Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(buffer);

                image.close();
                reader.close();

                Bitmap realSizeBitmap = Bitmap.createBitmap(bmp, 0, 0, metrics.widthPixels, metrics.heightPixels/*bmp.getHeight()*/);
                bmp.recycle();

                /* do something with [realSizeBitmap] */
                //saveBitmap(cropBitmap(realSizeBitmap));
                //saveBitmap(realSizeBitmap);


                Bitmap result = cropBitmap(realSizeBitmap);

                //Translator.init() -> MainAct//
                String recognised_text = Translator.extractText(result);

                //Toast.makeText(getApplicationContext(), recognised_text, Toast.LENGTH_LONG)
                //        .show();

                Translator.translate(recognised_text, getApplication());

                mediaProjection.stop();
            }
        }, handler);


    }

    Bitmap cropBitmap(Bitmap bitmap){

        //Log.e(TAG, Things.resultCode + "");
        //Log.e(TAG, Things.data + "");
        //Log.e(TAG, Things.start + "");
        //Log.e(TAG, Things.end + "");

        //Log.e(TAG, Math.abs(Things.end.x - Things.start.x) + "");
        //Log.e(TAG, Math.abs(Things.end.y - Things.start.y) + "");

        int width = 0, height = 0;
        int x = 0, y = 0;

        if(Things.start.x > Things.end.x){
            width = Things.start.x - Things.end.x;
            x = Things.end.x;
        }

        if(Things.start.y > Things.end.y){
            height = Things.start.y - Things.end.y;
            y = Things.end.y;
        }

        if(Things.start.x < Things.end.x){
            width = Things.end.x - Things.start.x;
            x = Things.start.x;
        }

        if(Things.start.y < Things.end.y){
            height = Things.end.y - Things.start.y;
            y = Things.start.y;
        }

        //cut statusbar
        y += Things.statusBarHeight;

        return Bitmap.createBitmap(bitmap, x, y, width, height);
    }

    void saveBitmap(Bitmap bitmap){
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        //save path
        String path = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

        try {

            File imageFile = new File(path);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("я вас не звал");
    }
}
