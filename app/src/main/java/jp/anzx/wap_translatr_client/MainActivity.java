package jp.anzx.wap_translatr_client;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "WAPT_MainActivity";

    //request codes
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 666;
    private static final int MEDIA_PROJECTION_PERMISSION_REQUEST_CODE = 1337;

    private MediaProjectionManager mProjectionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setContentView(new SelectionView(this));

        getScreenSizes();

        //translator
        ProgressBar progressBar = findViewById(R.id.loadingpanel);


        Translator.setProgressBar(progressBar);
        Translator.init(this);

        //toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //round btn
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "(- -)", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                start();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void start(){
        //if overlay and screencapture permissions
        if (Settings.canDrawOverlays(this) &&
                Things.resultCode != null && Things.data != null ) {

            Intent i = new Intent(this, FloatingService.class);
            startService(i);

        }
        else{
            gainPermissions();
        }
    }

    private void gainPermissions(){
        //Overlay
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        }else{
            Log.i(TAG, "already has overlay permission");
        }

        //ScreenCapture
        if (Things.resultCode == null || Things.data == null ){

            mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), MEDIA_PROJECTION_PERMISSION_REQUEST_CODE);
        }else{
            Log.i(TAG, "already has screencapture permission");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "overlay permission gained");
                }
                else{
                    Log.d(TAG, "overlay permission denied");
                }
            }
        }

        if (requestCode == MEDIA_PROJECTION_PERMISSION_REQUEST_CODE){
            if (resultCode == RESULT_OK) {

                Things.resultCode = resultCode;
                Things.data = data;

                Log.i(TAG, "screencapture permission gained");
            }
            else{
                Log.d(TAG, "screencapture permission denied");
            }
        }
    }

    void getScreenSizes(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        int height = size.y;

        Rect rectgle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);

        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            Things.navigationBarHeight = resources.getDimensionPixelSize(resourceId);
        }

        int StatusBarHeight = height - rectgle.bottom - Things.navigationBarHeight;
        Things.statusBarHeight = StatusBarHeight;

    }
}
