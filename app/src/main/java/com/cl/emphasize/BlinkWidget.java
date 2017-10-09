package com.cl.emphasize;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED;

/**
 * @author Chris Lo
 */

public class BlinkWidget extends AppWidgetProvider {

    //Widget is initially updated with my default values. Pressing widget edit button will open
    //ChooseFileForWidgetActivity, which broadcasts intent extras to BlinkWidget's onReceive().

    public static final String PREFS_NAME = "PreferenceFile";
    public static String CHOOSE_FILE_ACTION = "ActionChooseFileForBlinkWidget";
    public static String EDIT_FILE_ACTION = "ActionEditFileForBlinkWidget";
    //Following 3 are overwritten by defaults in onReceive()
    protected static String receivedFileName = "file";
    protected static String receivedFileContents = "Select note";
    protected static int receivedBlinkDelay = 0;
    protected static String receivedBackgroundColor = "white";
    static final Handler myHandler = new Handler(Looper.getMainLooper()); //for postDelayed()
    protected static HashMap<Integer, Boolean> widgetIdStopRunnable = new HashMap<Integer, Boolean>();
    protected static HashMap<Integer, Boolean> widgetIdIsRunning = new HashMap<Integer, Boolean>();
    protected static List<Integer> hasRunnableBeforeMe = new ArrayList<Integer>();
    protected static List<Integer> widgetIdWait = new ArrayList<Integer>();

    static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId){
        Log.d("BlinkWidget", "called updateAppWidget(). Working with widget " + appWidgetId);

        //Click corner button, bring up CFFWactivity
        Intent intent = new Intent(context, ChooseFileForWidgetActivity.class);
        intent.putExtra("widgetId", appWidgetId);
        intent.putExtra("currentBlinkDelay", receivedBlinkDelay);
        intent.putExtra("currentBackgroundColor", receivedBackgroundColor);
        intent.putExtra("isConfig", false);
        //PendingIntent param 2, is appWidgetId to let CFFWactivity differentiate between intent
        //PendingIntent param 4, reuses old appWidgetId intent but updates extras
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.blink_widget);
        views.setOnClickPendingIntent(R.id.selectFileButton, pendingIntent);
        views.setTextViewText(R.id.appwidgetText, receivedFileContents);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        Log.d("BlinkWidget", "got here 1");
        //Click main body, bring up TextEditor
        Intent textEditorIntent = new Intent(context, TextEditorActivity.class);
        textEditorIntent.putExtra("fromWidget", true);
        textEditorIntent.putExtra("widgetId", appWidgetId);
        textEditorIntent.putExtra("isNewFile", false);
        textEditorIntent.putExtra("fileName", receivedFileName);
        textEditorIntent.putExtra("fileContents", receivedFileContents);
        textEditorIntent.putExtra("currentBlinkDelay", receivedBlinkDelay);
        textEditorIntent.putExtra("currentBackgroundColor", receivedBackgroundColor);
        PendingIntent textEditorPendingIntent = PendingIntent.getActivity(context, appWidgetId,
                textEditorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.mainBodyButton, textEditorPendingIntent);
        views.setTextViewText(R.id.appwidgetText, receivedFileContents);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        Log.d("BlinkWidget", "got here 2");
        //fileContents and blinkDelay received in onReceive()
        final int blinkDelay = receivedBlinkDelay;
        CharSequence fileContents = receivedFileContents;
        views.setTextViewText(R.id.appwidgetText, fileContents);

        Log.d("BlinkWidget", "got here 3");
        //background color argb values
        int argbAlpha;
        int argbRed = 255;
        int argbGreen = 255;
        int argbBlue = 255;
        Log.d("BlinkWidget", "receivedBackgroundColor is " + receivedBackgroundColor);
        Log.d("BlinkWidget", "receivedBlinkDelay is " + receivedBlinkDelay);
        Log.d("BlinkWidget", "receivedFileContents is " + receivedFileContents);
        Log.d("BlinkWidget", "receivedFileName is " + receivedFileName);
        switch(receivedBackgroundColor){
            case "red":{
                argbRed = 255;
                argbGreen = 158;
                argbBlue = 158;
                break;
            }
            case "orange":{
                argbRed = 255;
                argbGreen = 231;
                argbBlue = 175;
                break;
            }
            case "yellow":{
                argbRed = 255;
                argbGreen = 253;
                argbBlue = 193;
                break;
            }
            case "green":{
                argbRed = 197;
                argbGreen = 255;
                argbBlue = 195;
                break;
            }
            case "blue":{
                argbRed = 163;
                argbGreen = 220;
                argbBlue = 255;
                break;
            }
            case "purple":{
                argbRed = 232;
                argbGreen = 167;
                argbBlue = 255;
                break;
            }
            case "gray":{
                argbRed = 200;
                argbGreen = 200;
                argbBlue = 200;
                break;
            }
            case "white":{
                argbRed = 255;
                argbGreen = 255;
                argbBlue = 255;
                break;
            }
        }

        final int runArgbRed = argbRed;
        final int runArgbGreen = argbGreen;
        final int runArgbBlue = argbBlue;

        Log.d("BlinkWidget", "got here 4");
        //loop switch between 2 colors
        if(blinkDelay > 0){
            final Runnable runnable = new Runnable(){
                boolean lightOn = true;
                @Override
                public void run() { //Do not update UI from anywhere except main thread
                    if(widgetIdStopRunnable.get(appWidgetId).equals(false)){
                        if (lightOn) {
                            lightOn = false;
                            views.setInt(R.id.RelativeLayoutBlink, "setBackgroundColor",
                                    Color.argb(160, new Integer(runArgbRed), runArgbGreen, runArgbBlue)); //turn light off
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                            myHandler.postDelayed(this, blinkDelay);
                        } else {
                            lightOn = true;
                            views.setInt(R.id.RelativeLayoutBlink, "setBackgroundColor",
                                    Color.argb(230, runArgbRed, runArgbGreen, runArgbBlue)); //turn light on
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                            myHandler.postDelayed(this, blinkDelay);
                        }
                    } else{
                        //StopRunnable was true
                        views.setInt(R.id.RelativeLayoutBlink, "setBackgroundColor",
                                Color.argb(150, 255, 248, 231)); //turn light off
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                        widgetIdIsRunning.put(appWidgetId, false);
                        if(widgetIdWait.contains(new Integer(appWidgetId))) { //is some runnable waiting for this to end?
                            try {
                                widgetIdWait.remove(new Integer(appWidgetId)); //before or after removeCallbacks?
                            } catch(IndexOutOfBoundsException e){
                                Log.d("BlinkWidget", "IndexOutOfBounds"); //happens if int used. HashMap looks for Integer
                            }
                        }
                        myHandler.removeCallbacksAndMessages(this);
                    }
                }
            };

            Log.d("BlinkWidget", "got here 5");
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

                    while (widgetIdWait.contains(appWidgetId)) {
                        //waiting for old runnable to end and remove widgetId from wait list
                        if (!widgetIdWait.contains(appWidgetId)) {
                            break;
                        }
                    }

                    //old runnable stopped. start new runnable
                    widgetIdStopRunnable.put(appWidgetId, false);
                    widgetIdIsRunning.put(appWidgetId, true);
                    myHandler.post(runnable);
                    widgetIdWait.add(new Integer(appWidgetId));
                }
            });
        } else{ //User selected No Blink
            if(hasRunnableBeforeMe.contains(appWidgetId)){
                //there's a runnable on this widget. stop it
                AsyncTask.execute(new Runnable(){
                    @Override
                    public void run(){
                        if(hasRunnableBeforeMe.contains(appWidgetId)){ //widgetId runnable exists in messageQueue?
                            //wait for old runnable to stop
                            while(widgetIdIsRunning.get(appWidgetId) == true){
                                widgetIdStopRunnable.put(appWidgetId, true);
                            }
                        } else{
                            hasRunnableBeforeMe.add(appWidgetId);
                        }

                        while (widgetIdWait.contains(appWidgetId)){
                            //waiting for old runnable to end and remove widgetId from wait list
                            if (!widgetIdWait.contains(appWidgetId)){
                                break;
                            }
                        }
                        views.setInt(R.id.RelativeLayoutBlink, "setBackgroundColor",
                                Color.argb(230, runArgbRed, runArgbGreen, runArgbBlue));
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }
                });
            } else{ //no runnable found on this widget. just update it to show note
                views.setInt(R.id.RelativeLayoutBlink, "setBackgroundColor",
                        Color.argb(230, runArgbRed, runArgbGreen, runArgbBlue));
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent){
        //Update, delete, disabled, and enabled all caught here
        //receivedFileContents = "Select note";
        //receivedBlinkDelay = 0;
        //receivedBackgroundColor = "white";
        if(intent.getExtras() == null){
            //user put down new widget or removed a widget. Let super handle it.
            super.onReceive(context, intent);
        } else{
            String action = intent.getAction();
            Bundle extras = intent.getExtras();
            Log.d("BlinkWidget", "in onReceive()");
            Log.d("BlinkWidget", "action is " + action);
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            ComponentName name = new ComponentName(context, BlinkWidget.class);
            int[] appWidgetId = AppWidgetManager.getInstance(context).getAppWidgetIds(name);
            final int appWidgetIdLength = appWidgetId.length;
            if(action != null && (action.equals(CHOOSE_FILE_ACTION))){
                Log.d("BlinkWidget", "action is was CHOOSE_FILE_ACTION");
                receivedFileName = extras.getString("fileName");
                receivedFileContents = extras.getString("fileContents");
                receivedBlinkDelay = extras.getInt("blinkDelay");
                receivedBackgroundColor = extras.getString("backgroundColor");
                if(appWidgetIdLength < 1){
                    return;
                } else{
                    int id = extras.getInt("widgetId");
                    Log.d("BlinkWidget", "id is " + String.valueOf(id));
                    updateAppWidget(context, mgr, id);
                    super.onReceive(context, intent);
                }
            } else if(action != null && (action.equals(EDIT_FILE_ACTION))){
                Log.d("BlinkWidget", "action is was EDIT_FILE_ACTION");
                receivedFileName = extras.getString("fileName");
                receivedFileContents = extras.getString("fileContents");
                receivedBlinkDelay = extras.getInt("blinkDelay");
                receivedBackgroundColor = extras.getString("backgroundColor");
                if (appWidgetIdLength < 1) {
                    return;
                } else {
                    int id = extras.getInt("widgetId");
                    Log.d("BlinkWidget", "id is " + String.valueOf(id));
                    updateAppWidget(context, mgr, id);
                    super.onReceive(context, intent);
                }
            } else if(action.equals(ACTION_APPWIDGET_OPTIONS_CHANGED)){
                Log.d("BlinkWidget", "action was APPWIDGET_OPTIONS_CHANGED");
                //came here from config. Get the SharedPreference values and start the runnable
                if(appWidgetIdLength < 1){
                    return;
                } else {
                    SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
                    //2nd params are defaults for new widget instances
                    receivedFileName = settings.getString("configFileName", "file");
                    receivedFileContents = settings.getString("configFileContents", "Select note");
                    receivedBlinkDelay = settings.getInt("configBlinkDelay", 0);
                    receivedBackgroundColor = settings.getString("configBackgroundColor", "white");
                    int id = settings.getInt("configWidgetId", 0);
                    Log.d("BlinkWidget", "id is " + String.valueOf(id));
                    updateAppWidget(context, mgr, id);
                    super.onReceive(context, intent);
                }
            }
            Log.d("BlinkWidget", "action was NULL or some other action");
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
    public void onDeleted(Context context, int[] appWidgetIds){
        Log.d("CFFWactivity", "Called onDeleted()");
        //stop these widgets' runnables
        for(int appWidgetId : appWidgetIds){
            widgetIdStopRunnable.put(appWidgetId, true);
        }
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) { //might need to override onReceive() to prevent reboot problem
        Log.d("CFFWactivity", "Called onEnabled()");
    }

    @Override
    public void onDisabled(Context context) {
        //might need to do handler.removeCallbacksAndMessages(null); to stop all runnables
        Log.d("CFFWactivity", "Called onDisabled()");
    }
}
