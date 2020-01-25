package jp.anzx.wap_translatr_client;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Translator {

    public static final String TAG = "WAPT_Translator";

    private static String DATA_PATH;

    private static ProgressBar progressBar;

    private static FirebaseTranslator firebaseTranslator;


    public static void setProgressBar(ProgressBar progress) {
        progressBar = progress;
    }


    public static void init(Context context){

        // Tesseract
        extractTessDataFiles(context);

        // FirebaseTranslator
        downloadLang();

    }

    static void downloadLang(){

        int src = -1, dist = -1;

        switch (Things.srcLang){
            case Things.JP:
                src = FirebaseTranslateLanguage.JA;
                break;
            case Things.EN:
                src = FirebaseTranslateLanguage.EN;
                break;
            case Things.RU:
                src = FirebaseTranslateLanguage.RU;
                break;
        }

        switch (Things.distLang){
            case Things.JP:
                dist = FirebaseTranslateLanguage.JA;
                break;
            case Things.EN:
                dist = FirebaseTranslateLanguage.EN;
                break;
            case Things.RU:
                dist = FirebaseTranslateLanguage.RU;
                break;
        }

        //show progressbar
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);

        // Create an translator:
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(src)
                        .setTargetLanguage(dist)
                        .build();

        firebaseTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        //download translation model on device
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        firebaseTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {

                                Log.i(TAG, "Model downloaded successfully. Okay to start translating.");

                                //hide progressbar
                                if (progressBar != null)
                                    progressBar.setVisibility(View.GONE);

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {

                                Log.e(TAG, "Model couldn’t be downloaded or other internal error.");
                            }
                        });


    }


    public static void translate(String text, Context con){
        //translate source text to english

        final Context context = con;

        if (Things.srcLang.equals(Things.JP)){
            text = text.replaceAll(" ", "");
            text = text.replaceAll("\n", "");
        }

        firebaseTranslator.translate(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                Log.i(TAG, translatedText);
                                Toast.makeText(context , translatedText, Toast.LENGTH_LONG).show();

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG ,"Problem in translating the text entered");
                            }
                        });
    }

    public static String extractText(Bitmap bitmap)
    {
        TessBaseAPI tessBaseApi = new TessBaseAPI();
        tessBaseApi.init(DATA_PATH, Things.srcLang);
        tessBaseApi.setImage(bitmap);
        String extractedText = tessBaseApi.getUTF8Text();
        Log.i(TAG, extractedText);
        tessBaseApi.end();
        return extractedText;
    }


    private static void extractTessDataFiles(Context context){
        try{

            DATA_PATH = context.getFilesDir().getPath();
            Log.i(TAG, DATA_PATH);

            //создаем папку
            File data_dir = new File(DATA_PATH + "/tessdata");
            if(!data_dir.exists()){
                data_dir.mkdir();
            }

            //перемещаем файлы из assets в телефон
            String[] files = context.getAssets().list("tessdata");

            for(String filename : files){
                //считываем из assets
                byte[] buffer = null;
                InputStream is;

                is = context.getAssets().open("tessdata/" + filename);
                int size = is.available();
                buffer = new byte[size];
                is.read(buffer);
                is.close();

                //впихиваем в телефон
                FileOutputStream fos = new FileOutputStream(new File(DATA_PATH + "/tessdata", filename));
                fos.write(buffer);
                fos.close();

                Log.e(TAG, new File(DATA_PATH, filename).getPath());
            }

        }catch (IOException e){
            e.printStackTrace();
            Log.e(TAG, e.getMessage());

        }

    }




}
