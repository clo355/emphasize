package com.cl.emphasize;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * @author Chris Lo
 */

public class NormalWidget extends AppWidgetProvider {

    public static String CHOOSE_FILE_ACTION = "ActionChooseFileForNormalWidget";
    protected static String receivedFileContents = "overwritten";
    protected static String receivedBackgroundColor = "overwritten";
    //protected static String receivedTextColor;
    //protected static int receivedTextSize;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        //Clicked widget, bring up CFFWactivity
        Intent intent = new Intent(context, ChooseFileForWidgetActivity.class);
        intent.putExtra("widgetId", appWidgetId);
        intent.putExtra("widgetType", "normal");
        //backgroundColor may not be correctly sent after first time
        intent.putExtra("currentBackgroundColor", receivedBackgroundColor);
        //PendingIntent param appWidgetId to let CFFWactivity know it's a unique intent
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.normal_widget);
        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
        views.setTextViewText(R.id.appwidget_text, receivedFileContents);
        switch(receivedBackgroundColor){
            case "red":{
                views.setInt(R.id.RelativeLayoutNormal, "setBackgroundColor",
                        Color.argb(255, 255, 37, 37));
                break;
            }
            case "orange":{
                views.setInt(R.id.RelativeLayoutNormal, "setBackgroundColor",
                        Color.argb(255, 225, 179, 37));
                break;
            }
            case "yellow":{
                views.setInt(R.id.RelativeLayoutNormal, "setBackgroundColor",
                        Color.argb(255, 234, 236, 14));
                break;
            }
            case "green":{
                views.setInt(R.id.RelativeLayoutNormal, "setBackgroundColor",
                        Color.argb(255, 111, 236, 14));
                break;
            }
            case "blue":{
                views.setInt(R.id.RelativeLayoutNormal, "setBackgroundColor",
                        Color.argb(255, 14, 197, 236));
                break;
            }
            case "purple":{
                views.setInt(R.id.RelativeLayoutNormal, "setBackgroundColor",
                        Color.argb(255, 189, 14, 236));
                break;
            }
            case "gray":{
                views.setInt(R.id.RelativeLayoutNormal, "setBackgroundColor",
                        Color.argb(255, 163, 163, 163));
                break;
            }
            case "white":{
                views.setInt(R.id.RelativeLayoutNormal, "setBackgroundColor",
                        Color.argb(255, 255, 255, 255));
                break;
            }
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        receivedFileContents = "Select note";
        if(intent.getExtras() == null){
            //pressed cancel on CFFWactivity
        } else{
            String action = intent.getAction();
            Bundle b = intent.getExtras();
            if(action != null && action.equals(CHOOSE_FILE_ACTION)){
                receivedFileContents = b.getString("fileContents");
                receivedBackgroundColor = b.getString("backgroundColor");
                final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                ComponentName name = new ComponentName(context, NormalWidget.class);
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

