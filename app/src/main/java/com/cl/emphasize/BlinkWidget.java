package com.cl.emphasize;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class BlinkWidget extends AppWidgetProvider {

    //Widget is initially updated with my default values. Pressing button will open
    //ChooseFileForWidgetActivity, which broadcasts intent extras to BlinkWidget's onReceive().

    public static String CHOOSE_FILE_ACTION = "ActionChooseFileForBlinkWidget";
    protected static String receivedFileContents = "Select file";
    protected static int receivedBlinkDelay = 0;
    //protected static String receivedBackgroundColor;
    //protected static String receivedTextColor;
    //protected static int receivedTextSize;
    static final Handler myHandler = new Handler(Looper.getMainLooper()); //for postDelayed()
    protected static HashMap<Integer, Boolean> widgetIdStopRunnable = new HashMap<Integer, Boolean>();
    protected static HashMap<Integer, Boolean> widgetIdIsRunning = new HashMap<Integer, Boolean>();
    protected static List<Integer> hasRunnableBeforeMe = new ArrayList<Integer>();
    protected static List<Integer> widgetIdWait = new ArrayList<Integer>();

    static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId){
        //Clicked widget, bring up CFFWactivity
        Intent intent = new Intent(context, ChooseFileForWidgetActivity.class);
        intent.putExtra("widgetId", appWidgetId);
        intent.putExtra("widgetType", "blink");
        //PendingIntent param appWidgetId to let CFFWactivity know it's a unique intent
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.blink_widget);
        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
        views.setTextViewText(R.id.appwidget_text, receivedFileContents);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        //fileContents and blinkDelay received in onReceive()
        final int blinkDelay = receivedBlinkDelay;
        CharSequence fileContents = receivedFileContents;
        views.setTextViewText(R.id.appwidget_text, fileContents);

        //loop switch between 2 colors
        if(blinkDelay > 0){
            final Runnable runnable = new Runnable(){
                boolean lightOn = true;
                @Override
                public void run() { //Do not update UI from anywhere except main thread
                    if(widgetIdStopRunnable.get(appWidgetId).equals(false)){
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
                    } else{
                        //StopRunnable was true
                        views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                                Color.argb(150, 255, 248, 231)); //turn light off
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                        widgetIdIsRunning.put(appWidgetId, false);
                        Log.d("updateAppWidget", "called removeCallbacksAndMessages(this)");
                        Log.d("updateAppWidget", "Dealing with " + appWidgetId);
                        Log.d("updateAppWidget", "widgetIdWait.contains(appWidgetId) is " + widgetIdWait.contains(new Integer(appWidgetId)));
                        if(widgetIdWait.contains(new Integer(appWidgetId))) { //is some runnable waiting for this to end?
                            try {
                                widgetIdWait.remove(new Integer(appWidgetId)); //before or after removeCallbacks?
                            } catch(IndexOutOfBoundsException e){
                                Log.d("EXCEPTION", "Why would this happen!!!");
                            }
                        }

                        myHandler.removeCallbacksAndMessages(this);
                    }
                }
            };

            //This checks widgetIdIsRunning in the background so it doesn't block old runnable's check loop
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    if(hasRunnableBeforeMe.contains(appWidgetId)){ //widgetId runnable exists in messageQueue?
                        //wait for old runnable to stop
                        while(widgetIdIsRunning.get(appWidgetId) == true){
                            widgetIdStopRunnable.put(appWidgetId, true);
                        }
                    } else{
                        hasRunnableBeforeMe.add(appWidgetId);
                    }

                    Log.d("AsyncTask", "Starting widgetIdWait loop");
                    while (widgetIdWait.contains(appWidgetId)) {
                        //waiting for old runnable to end and remove widgetId from wait list
                        if (!widgetIdWait.contains(appWidgetId)) {
                            break;
                        }
                    }

                    Log.d("AsyncTask", "widgetIdWait loop ended");
                    //old runnable stopped. start new runnable
                    widgetIdStopRunnable.put(appWidgetId, false);
                    widgetIdIsRunning.put(appWidgetId, true);
                    myHandler.post(runnable);
                    widgetIdWait.add(new Integer(appWidgetId));
                }
            });
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
            //probably pressed cancel
            Log.d("onReceive", "getExtras() was null");
        } else{
            Log.d("onReceive", "getExtras() was not null");
            String action = intent.getAction();
            Bundle b = intent.getExtras();
            if(action != null && action.equals(CHOOSE_FILE_ACTION)){
                receivedFileContents = b.getString("fileContents");
                receivedBlinkDelay = b.getInt("blinkDelay");
                final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                ComponentName name = new ComponentName(context, BlinkWidget.class);
                int[] appWidgetId = AppWidgetManager.getInstance(context).getAppWidgetIds(name);
                final int appWidgetIdLength = appWidgetId.length;
                if(appWidgetIdLength < 1){
                    Log.d("onReceive", "got here 5");
                    return;
                } else{
                    int id = b.getInt("widgetId");
                    updateAppWidget(context, mgr, id);
                    Log.d("onReceive", "got here 4");
                    super.onReceive(context, intent);
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
