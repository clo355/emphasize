package com.cl.emphasize;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;


public class FileContentsWidget extends AppWidgetProvider {

    public static String CHOOSE_FILE_ACTION = "ActionChooseFileForFileContentsWidget";
    protected static String receivedFileContents = "Select file";
    protected static int receivedBlinkDelay = 0;

    static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId){
        Log.d("updateAppWidget called", "appWidgetId is " + Integer.toString(appWidgetId));
        //Clicked widget, bring up CFFWactivity
        Intent intent = new Intent(context, ChooseFileForWidgetActivity.class);
        intent.putExtra("widgetId", appWidgetId);
        //PendingIntent is appWidgetId to let CFFWactivity know it's a unique intent
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.file_contents_widget);
        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
        views.setTextViewText(R.id.appwidget_text, receivedFileContents);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        //fileContents and blinkDelay received in onReceive()
        final int blinkDelay = receivedBlinkDelay;
        CharSequence fileContents = receivedFileContents;
        views.setTextViewText(R.id.appwidget_text, fileContents);

        //loop switch between 2 colors, only if blinkDelay valid
        final Handler myHandler = new Handler(); //for postDelayed()
        if(blinkDelay > 0){
            final Runnable runnable = new Runnable(){
                boolean lightOn = true;

                public void run() { //Do not update UI from anywhere except main thread
                    if (lightOn) {
                        lightOn = false;
                        views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                                Color.argb(150, 255, 248, 231)); //turn light off
                        appWidgetManager.updateAppWidget(appWidgetId, views);

                        myHandler.postDelayed(this, blinkDelay);
                    } else {
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
        } else{
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("onReceive called", "once");
        receivedFileContents = "Select file";
        receivedBlinkDelay = 0;
        if(intent.getExtras() == null){
            Log.d("onReceive", "getExtras() was null");
        } else{
            Log.d("onReceive", "getExtras() was not null");
            String action = intent.getAction();
            Bundle b = intent.getExtras();
            if(action != null && action.equals(CHOOSE_FILE_ACTION)){
                receivedFileContents = b.getString("fileContents");
                receivedBlinkDelay = b.getInt("blinkDelay");
                final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                ComponentName name = new ComponentName(context, FileContentsWidget.class);
                int[] appWidgetId = AppWidgetManager.getInstance(context).getAppWidgetIds(name);
                final int appWidgetIdLength = appWidgetId.length;
                if(appWidgetIdLength < 1){
                    Log.d("onReceive", "got here 5");
                    return;
                } else{
                    int id = b.getInt("widgetId");
                    Log.d("onReceive", "widgetId is " + Integer.toString(id));
                    updateAppWidget(context, mgr, id);
                    Log.d("onReceive", "got here 4");
                    super.onReceive(context, intent); //added
                }
                Log.d("onReceive", "got here 3");
            }
            Log.d("onReceive", "got here 2 (expected)");
            super.onReceive(context, intent);
        }
        Log.d("onReceive", "got here 1 (expected)");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("onUpdate called", "once");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) { //might need to override onReceive() to prevent reboot problem
        Log.d("onEnabled called", "once");
        // Enter relevant functionality for when the first widget is created (includes device reboot)
        //Display ListView of files, same as

    }

    @Override
    public void onDisabled(Context context) {
        Log.d("onDisabled called", "once");
        // Enter relevant functionality for when the last widget is disabled
        //might need to do handler.removeCallbacksAndMessages(null); to stop runnables

    }
}
