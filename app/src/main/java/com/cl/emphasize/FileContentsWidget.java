package com.cl.emphasize;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Timer;
import java.util.TimerTask;

public class FileContentsWidget extends AppWidgetProvider {

    private String fileContents;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Intent intent = new Intent(context, FileContentsWidget.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.file_contents_widget);

        CharSequence widgetText = context.getString(R.string.appwidget_text);

        boolean lightOn = true;
        /*
        if(lightOn){
            views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                    Color.rgb(178, 158, 108)); //dark beige
            lightOn = false;
        } else{
            views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                    Color.rgb(255, 248, 231)); //light beige
            lightOn = true;
        }
        */
        try { //"wait x seconds"
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                Color.rgb(249, 124, 243));

        appWidgetManager.updateAppWidget(appWidgetId, views); //tell widget update changes

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

