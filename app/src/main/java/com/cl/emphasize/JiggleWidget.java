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

public class JiggleWidget extends AppWidgetProvider {

    public static String CHOOSE_FILE_ACTION = "ActionChooseFileForJiggleWidget";
    protected static String receivedFileContents = "Select file";
    protected static int receivedJiggleDelay = 0;
    //protected static String receivedBackgroundColor;
    //protected static String receivedTextColor;
    //protected static int receivedTextSize;
    static final Handler myHandler = new Handler(Looper.getMainLooper());
    protected static HashMap<Integer, Boolean> widgetIdStopRunnable = new HashMap<Integer, Boolean>();
    protected static HashMap<Integer, Boolean> widgetIdIsRunning = new HashMap<Integer, Boolean>();
    protected static List<Integer> hasRunnableBeforeMe = new ArrayList<Integer>();
    protected static List<Integer> widgetIdWait = new ArrayList<Integer>();

    static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId) {
        //Clicked widget, bring up CFFWactivity
        Intent intent = new Intent(context, ChooseFileForWidgetActivity.class);
        intent.putExtra("widgetId", appWidgetId);
        intent.putExtra("widgetType", "jiggle");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.jiggle_widget);
        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
        views.setTextViewText(R.id.appwidget_text, receivedFileContents);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        final int jiggleDelay = receivedJiggleDelay;
        CharSequence fileContents = receivedFileContents;
        views.setTextViewText(R.id.appwidget_text, fileContents);

        if(jiggleDelay > 0){
            final Runnable runnable = new Runnable(){
                int jigglePosition = 1; //center1, right2, center3, left4, repeat.
                @Override
                public void run() { //Do not update UI from anywhere except main thread
                    if(widgetIdStopRunnable.get(appWidgetId).equals(false)){
                        if (jigglePosition == 1) {
                            jigglePosition = 2;
                            //Change padding on API 16+: id, left, top, right, bottom
                            //Only this first setViewPadding works. The others reset padding to 0
                            views.setViewPadding(R.id.RelativeLayoutJiggle, 0, 0, 150, 0);
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                            Log.d("JiggleWidget", "right2");
                            myHandler.postDelayed(this, jiggleDelay);
                        }else if(jigglePosition == 2){
                            jigglePosition = 3;
                            views.setViewPadding(R.id.RelativeLayoutJiggle, 100, 0, 100, 0);
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                            Log.d("JiggleWidget", "center3");
                            myHandler.postDelayed(this, jiggleDelay);
                        } else if(jigglePosition == 3){
                            jigglePosition = 4;
                            views.setViewPadding(R.layout.jiggle_widget, 150, 0, 0, 0);
                            //views.setInt(R.layout.jiggle_widget, "setPadding", 30);
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                            Log.d("JiggleWidget", "left4");
                            myHandler.postDelayed(this, jiggleDelay);
                        } else{ //jigglePosition is "left4"
                            jigglePosition = 1;
                            views.setViewPadding(R.layout.jiggle_widget, 100, 0, 100, 0);
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                            Log.d("JiggleWidget", "center1");
                            myHandler.postDelayed(this, jiggleDelay);
                        }
                    } else{
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                        widgetIdIsRunning.put(appWidgetId, false);
                        Log.d("updateAppWidget", "called removeCallbacksAndMessages(this)");
                        Log.d("updateAppWidget", "Dealing with " + appWidgetId);
                        Log.d("updateAppWidget", "widgetIdWait.contains(appWidgetId) is " + widgetIdWait.contains(new Integer(appWidgetId)));
                        if(widgetIdWait.contains(new Integer(appWidgetId))) {
                            try {
                                widgetIdWait.remove(new Integer(appWidgetId));
                            } catch(IndexOutOfBoundsException e){
                                Log.d("JiggleWidget", "IndexOutOfBoundsException");
                            }
                        }
                        myHandler.removeCallbacksAndMessages(this);
                    }
                }
            };

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    if(hasRunnableBeforeMe.contains(appWidgetId)){
                        while(widgetIdIsRunning.get(appWidgetId) == true){
                            widgetIdStopRunnable.put(appWidgetId, true);
                        }
                    } else{
                        hasRunnableBeforeMe.add(appWidgetId);
                    }

                    Log.d("AsyncTask", "Starting widgetIdWait loop");
                    while (widgetIdWait.contains(appWidgetId)) {
                        if (!widgetIdWait.contains(appWidgetId)) {
                            break;
                        }
                    }

                    Log.d("AsyncTask", "widgetIdWait loop ended");
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
        receivedJiggleDelay = 0;
        if(intent.getExtras() == null){
            //probably pressed cancel
        } else{
            String action = intent.getAction();
            Bundle b = intent.getExtras();
            if(action != null && action.equals(CHOOSE_FILE_ACTION)){
                receivedFileContents = b.getString("fileContents");
                receivedJiggleDelay = b.getInt("jiggleDelay");
                final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                ComponentName name = new ComponentName(context, JiggleWidget.class);
                int[] appWidgetId = AppWidgetManager.getInstance(context).getAppWidgetIds(name);
                final int appWidgetIdLength = appWidgetId.length;
                if(appWidgetIdLength < 1){
                    return;
                } else{
                    int id = b.getInt("widgetId");
                    updateAppWidget(context, mgr, id);
                    super.onReceive(context, intent);
                }
            }
            super.onReceive(context, intent);
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
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

