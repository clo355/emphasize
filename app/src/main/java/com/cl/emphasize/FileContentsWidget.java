package com.cl.emphasize;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;

public class FileContentsWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId) {

        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.file_contents_widget);
        CharSequence widgetText = context.getString(R.string.appwidget_text);

        //loop switch between 2 colors
        final Handler myHandler = new Handler();
        final Runnable runnable = new Runnable() {
            int count = 0;
            public void run() {
                if ((count++ % 2) == 0){
                    views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                            Color.argb(220, 255, 248, 231)); //light "on"
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                    myHandler.postDelayed(this, 333);
                } else{
                    views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                            Color.argb(150, 255, 248, 231)); //light "off"
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                    myHandler.postDelayed(this, 333);
                }
            }
        };

        //start the loop
        myHandler.post(runnable);



        /*
        final Handler handler = new Handler();
        new Runnable() {
            @Override
            public void run() {
                views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                        Color.argb(220, 255, 248, 231)); //beige "on"
                appWidgetManager.updateAppWidget(appWidgetId, views);
                handler.postDelayed(this, 300);
            }
        }.run();
        new Runnable() {
            @Override
            public void run() {
                views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                        Color.argb(150, 255, 248, 231)); //beige "off"
                appWidgetManager.updateAppWidget(appWidgetId, views);
                handler.postDelayed(this, 300);
            }
        }.run();
        */



        /*
        handler.postDelayed(new Runnable() {
            @Override
            public void run(){
                views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                        Color.argb(255, 0, 255, 0)); //green
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }, 1000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run(){
                views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                        Color.argb(255, 0, 0, 255)); //blue
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }, 1000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run(){
                views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                        Color.argb(255, 255, 0, 0)); //red
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }, 1000);
        */

        /*
        boolean lightOn = true;

        for(int i = 0; i <= 20; i++){
            if (lightOn) {
                views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                        Color.argb(150, 255, 248, 231)); //beige "off"
                lightOn = false;
                appWidgetManager.updateAppWidget(appWidgetId, views);
            } else {
                views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                        Color.argb(220, 255, 248, 231)); //beige "on"
                lightOn = true;
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
        */
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) { //might need to override onReceive() to prevent reboot problem
        // Enter relevant functionality for when the first widget is created (includes device reboot)
        //Display ListView of files, same as

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

class WaitThread extends Thread{

}
