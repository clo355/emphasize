package com.cl.emphasize;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.widget.Button;
import android.widget.RemoteViews;

public class FileContentsWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId) {

        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.file_contents_widget);

        //open activity to choose file
        Intent chooseIntent = new Intent(context, ChooseFileForWidgetActivity.class);
        chooseIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent choosePendingIntent = PendingIntent.getActivity(context, 0, chooseIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_button, choosePendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);


        final int blinkDelay = 333;
        CharSequence fileContents = "Select file"; //intent get extra?
        views.setTextViewText(R.id.appwidget_text, fileContents);

        //loop switch between 2 colors
        final Handler myHandler = new Handler();
        final Runnable runnable = new Runnable() {
            boolean status = true;
            public void run() {
                if (status){
                    status = false;
                    views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                            Color.argb(220, 255, 248, 231)); //light "on"
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                    myHandler.postDelayed(this, blinkDelay);
                } else{
                    status = true;
                    views.setInt(R.id.RelativeLayout1, "setBackgroundColor",
                            Color.argb(150, 255, 248, 231)); //light "off"
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                    myHandler.postDelayed(this, blinkDelay);
                }
            }
        };


        //start the blink loop
        myHandler.post(runnable);

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
