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

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        //Clicked widget, bring up CFFWactivity
        Intent intent = new Intent(context, ChooseFileForWidgetActivity.class);
        intent.putExtra("widgetId", appWidgetId);
        intent.putExtra("widgetType", "jiggle");
        //PendingIntent param appWidgetId to let CFFWactivity know it's a unique intent
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.jiggle_widget);
        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
        views.setTextViewText(R.id.appwidget_text, receivedFileContents);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        //fileContents and jiggleDelay received in onReceive(), for use in 3 positions loop below
        final int jiggleDelay = receivedJiggleDelay;
        CharSequence fileContents = receivedFileContents;
        views.setTextViewText(R.id.appwidget_text, fileContents);

        //loop switch between 3 positions
        appWidgetManager.updateAppWidget(appWidgetId, views);

        //Change padding on API 16+ for RemoteView:
        //remoteView.setViewPadding(R.id.widget_item, 30, 0, 0, 0);
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

