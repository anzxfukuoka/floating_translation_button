package jp.anzx.wap_translatr_client;

import android.content.Intent;
import android.graphics.Point;

public class Things {

    /*
    * класс с глобальными переменными
    * (криво, но пусть будет)
    */

    // нужно для MediaProjection
    public static Integer resultCode;
    public static Intent data;

    // координаты выделения
    public static Point start;
    public static Point end;

    //...
    public static int statusBarHeight;
    public static int navigationBarHeight;

}
