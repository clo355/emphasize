package com.cl.emphasize;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;


public class FileContentsWidget extends AppWidgetProvider {

    public static String CHOOSE_FILE_ACTION = "ActionChooseFileForFileContentsWidget";
    protected static String receivedFileContents = "Select file";
    protected int receivedBlinkDelay = 300;

    static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId) {

        Intent intent = new Intent(context, ChooseFileForWidgetActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.file_contents_widget);
        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
        views.setTextViewText(R.id.appwidget_text, receivedFileContents);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        /*

        final int blinkDelay = receivedBlinkDelay;
        CharSequence fileContents = receivedFileContents;
        views.setTextViewText(R.id.appwidget_text, fileContents);

        //loop switch between 2 colors
        final Handler myHandler = new Handler();
        final Runnable runnable = new Runnable(){
            boolean lightOn = true;
            public void run(){
                if (lightOn){
                    lightOn = false;
                    views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                            Color.argb(150, 255, 248, 231)); //turn light off
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                    myHandler.postDelayed(this, blinkDelay);
                } else{
                    lightOn = true;
                    views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                            Color.argb(220, 255, 248, 231)); //turn light on
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                    myHandler.postDelayed(this, blinkDelay);
                }
            }
        };

        //start the blink loop
        myHandler.post(runnable);
        */
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getExtras() == null) {
            receivedFileContents = "No.....";
            Log.d("FILECONTENTSWIDGET", "No.....");
        } else{
            String action = intent.getAction();
            Bundle b = intent.getExtras();
            receivedFileContents = b.getString("fileContents");
            Log.d("FILECONTENTSWIDGET", "Good!!!");

            if(action != null && action.equals(CHOOSE_FILE_ACTION)){
                final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                ComponentName name = new ComponentName(context, FileContentsWidget.class);
                int[] appWidgetId = AppWidgetManager.getInstance(context).getAppWidgetIds(name);
                final int N = appWidgetId.length;
                if(N < 1){
                    return;
                } else{
                    int id = appWidgetId[N-1];
                    //updateWidget(context, appWidgetManager, id, title1);
                    updateAppWidget(context, mgr, id);
                }
            } else{
                super.onReceive(context, intent);
            }
        }

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
        //might need to do handler.removeCallbacksAndMessages(); to prevent memory leak
    }
}
