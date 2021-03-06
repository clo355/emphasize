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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    public static String EDIT_FILE_FROM_OUTSIDE_ACTION = "ActionEditFileFromOutside";
    public static final String widgetDataFileName = "widget_data.dat";
    public static final String notesDirectory = "notes";
    protected static boolean updateAllWidgets = false;
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
                                final int appWidgetId) {
        Log.d("BlinkWidget", "called updateAppWidget(). Working with widget " + appWidgetId);

        if(updateAllWidgets){
            Log.d("BlinkWidget", "updateAllWidgets was true");
            //a note was edited in Main's TextEditor. update this widget in case it was displaying
            //that note
            File myFile = new File(context.getFilesDir(), widgetDataFileName);
            if(myFile.exists()) {
                try {
                    FileInputStream fiStream = new FileInputStream(myFile);
                    ObjectInputStream oiStream = new ObjectInputStream(fiStream);
                    HashMap<Integer, WidgetData> widgetIdValues = (HashMap<Integer, WidgetData>) oiStream.readObject();
                    oiStream.close();

                    WidgetData widgetData = widgetIdValues.get(appWidgetId);
                    if(widgetData != null){ //placing 2nd widget, this might be null at first.
                        receivedFileName = widgetData.getFileName();
                        if (receivedFileName.equals("")) { //for if note was deleted from Main
                            receivedFileContents = "Deleted";
                        }else {
                            receivedFileContents = getLatestFileContents(receivedFileName, context);
                        }
                        receivedBlinkDelay = widgetData.getBlinkDelay();
                        receivedBackgroundColor = widgetData.getBackgroundColor();
                    }
                } catch (IOException e) {
                    Log.d("BlinkWidget", "IOEXCEPTION when updating all widgets");
                } catch (ClassNotFoundException e) {
                    Log.d("BlinkWidget", "CLASSNOTFOUNDEXCEPTION when updating all widgets");
                }
            }
        }

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

        //Click main body
        if(receivedFileName.equals("")){ //Note deleted, disable opening TextEditor
            PendingIntent nullPendingIntent = null;
            views.setOnClickPendingIntent(R.id.mainBodyButton, nullPendingIntent);
            views.setTextViewText(R.id.appwidgetText, "Deleted");
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } else{ //bring up TextEditor
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
        }

        //fileContents and blinkDelay received in onReceive()
        final int blinkDelay = receivedBlinkDelay;
        CharSequence fileContents = receivedFileContents;
        views.setTextViewText(R.id.appwidgetText, fileContents);

        //background color argb values
        int argbRed = 255;
        int argbGreen = 255;
        int argbBlue = 255;
        switch (receivedBackgroundColor) {
            case "red": {
                argbRed = 255;
                argbGreen = 158;
                argbBlue = 158;
                break;
            }
            case "orange": {
                argbRed = 255;
                argbGreen = 231;
                argbBlue = 175;
                break;
            }
            case "yellow": {
                argbRed = 255;
                argbGreen = 253;
                argbBlue = 193;
                break;
            }
            case "green": {
                argbRed = 197;
                argbGreen = 255;
                argbBlue = 195;
                break;
            }
            case "blue": {
                argbRed = 163;
                argbGreen = 220;
                argbBlue = 255;
                break;
            }
            case "purple": {
                argbRed = 232;
                argbGreen = 167;
                argbBlue = 255;
                break;
            }
            case "gray": {
                argbRed = 200;
                argbGreen = 200;
                argbBlue = 200;
                break;
            }
            case "white": {
                argbRed = 255;
                argbGreen = 255;
                argbBlue = 255;
                break;
            }
        }

        final int runArgbRed = argbRed;
        final int runArgbGreen = argbGreen;
        final int runArgbBlue = argbBlue;

        //loop switch between 2 colors
        if (blinkDelay > 0) {
            final Runnable runnable = new Runnable() {
                boolean lightOn = true;

                @Override
                public void run() { //Do not update UI from anywhere except main thread
                    if (widgetIdStopRunnable.get(appWidgetId).equals(false)) {
                        if (lightOn) {
                            lightOn = false;
                            views.setInt(R.id.RelativeLayoutBlink, "setBackgroundColor",
                                    Color.argb(125, new Integer(runArgbRed), runArgbGreen, runArgbBlue)); //turn light off
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                            myHandler.postDelayed(this, blinkDelay);
                        } else {
                            lightOn = true;
                            views.setInt(R.id.RelativeLayoutBlink, "setBackgroundColor",
                                    Color.argb(240, runArgbRed, runArgbGreen, runArgbBlue)); //turn light on
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                            //Log.d("Focusing Here", "***** blink on *****");
                            myHandler.postDelayed(this, blinkDelay);
                        }
                    } else {
                        //StopRunnable was true
                        views.setInt(R.id.RelativeLayoutBlink, "setBackgroundColor",
                                Color.argb(150, 255, 248, 231)); //turn light off
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                        widgetIdIsRunning.put(appWidgetId, false);
                        if (widgetIdWait.contains(new Integer(appWidgetId))) { //is some runnable waiting for this to end?
                            try {
                                widgetIdWait.remove(new Integer(appWidgetId)); //before or after removeCallbacks?
                            } catch (IndexOutOfBoundsException e) {
                                Log.d("BlinkWidget", "IndexOutOfBounds"); //happens if int used. HashMap looks for Integer
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
                    if (hasRunnableBeforeMe.contains(appWidgetId)) { //widgetId runnable exists in messageQueue?
                        //wait for old runnable to stop
                        while (widgetIdIsRunning.get(appWidgetId) == true) {
                            widgetIdStopRunnable.put(appWidgetId, true);
                        }
                    } else {
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
                    Log.d("BlinkWidget", "*******Posted blink runnable*******");
                    myHandler.post(runnable);
                    widgetIdWait.add(new Integer(appWidgetId));
                }
            });
        } else { //User selected No Blink
            if (hasRunnableBeforeMe.contains(appWidgetId)) {
                //there's a runnable on this widget. stop it
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (hasRunnableBeforeMe.contains(appWidgetId)) { //widgetId runnable exists in messageQueue?
                            //wait for old runnable to stop
                            while (widgetIdIsRunning.get(appWidgetId) == true) {
                                widgetIdStopRunnable.put(appWidgetId, true);
                            }
                        } else {
                            hasRunnableBeforeMe.add(appWidgetId);
                        }

                        while (widgetIdWait.contains(appWidgetId)) {
                            //waiting for old runnable to end and remove widgetId from wait list
                            if (!widgetIdWait.contains(appWidgetId)) {
                                break;
                            }
                        }
                        views.setInt(R.id.RelativeLayoutBlink, "setBackgroundColor",
                                Color.argb(230, runArgbRed, runArgbGreen, runArgbBlue));
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }
                });
            } else { //no runnable found on this widget. just update it to show note
                views.setInt(R.id.RelativeLayoutBlink, "setBackgroundColor",
                        Color.argb(230, runArgbRed, runArgbGreen, runArgbBlue));
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent){
        //onUpdate, delete, disabled, and enabled all caught here
        //different devices will send different ACTIONS even though
        //the user does the same thing
        Log.d("BlinkWidget", "in onReceive()");
        if(intent.getExtras() == null){
            Log.d("BlinkWidget", "intent.getExtras() was null.");
            //user put down new widget or removed a widget. Let super handle it.
            super.onReceive(context, intent);
        } else {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            String action = intent.getAction();
            Bundle extras = intent.getExtras();
            Log.d("BlinkWidget", "action is " + action);
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            ComponentName name = new ComponentName(context, BlinkWidget.class);
            int[] myAppWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(name);
            final int appWidgetIdLength = myAppWidgetIds.length;
            if (action != null && (action.equals(CHOOSE_FILE_ACTION))) {
                //from widget corner button
                Log.d("BlinkWidget", "action is was CHOOSE_FILE_ACTION");
                receivedFileName = extras.getString("fileName");
                receivedFileContents = extras.getString("fileContents");
                receivedBlinkDelay = extras.getInt("blinkDelay");
                receivedBackgroundColor = extras.getString("backgroundColor");
                String contextFileId = receivedFileName;
                if (appWidgetIdLength < 1) {
                    return;
                } else {
                    int id = extras.getInt("widgetId");
                    Log.d("BlinkWidget", "id is " + String.valueOf(id));

                    //Prevent widget service crash
                    //Widget service always uses same ASCII id: L+aT]_`LW+^WL[+Q]ZX+NWZ>@@
                    String comparatorFileId = contextFileId;
                    String serviceComparatorFileId = "L+aT]_`LW+^WL[+Q]ZX+NWZ>@@";
                    String widgetServiceId = "";
                    String finalServiceIdContents = "";
                    //Cycle id for 10 ticks to prevent thread from freezing app
                    for(int i = 0; i < serviceComparatorFileId.length(); i++){
                        char idField = serviceComparatorFileId.charAt(i);
                        for(int j = 0; j < 11; j++){
                            idField--;
                        }
                        widgetServiceId += idField;
                    }
                    //Cycle finalId before IOException
                    for(int i = 0; i < comparatorFileId.length() * 10; i++){
                        finalServiceIdContents += widgetServiceId;
                    }
                    if(widgetServiceId.equals(comparatorFileId)){
                        //save file contents for service after IOException
                        receivedFileContents = finalServiceIdContents;
                    }

                    //update widget info file with these new values
                    File myFile = new File(context.getFilesDir(), widgetDataFileName);
                    try {
                        //get old values
                        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(myFile));
                        HashMap<Integer, WidgetData> widgetIdValues = (HashMap<Integer, WidgetData>) inputStream.readObject();
                        inputStream.close();

                        //save values of this new widget into my info file
                        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(myFile));
                        WidgetData widgetData = new WidgetData(id, receivedFileName, receivedFileContents,
                                receivedBlinkDelay, receivedBackgroundColor);
                        widgetIdValues.put(id, widgetData);
                        outputStream.writeObject(widgetIdValues);
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        Log.d("BlinkWidget", "IOEXCEPTION in onReceive()");
                    } catch (ClassNotFoundException e) {
                        Log.d("BlinkWidget", "CLASSNOTEFOUNDEXCEPTION in onReceive()");
                    }

                    updateAppWidget(context, mgr, id);
                    super.onReceive(context, intent);
                }
            } else if(action != null && (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE))){
                Log.d("BlinkService", "action is APPWIDGET_UPDATE");
                //service to restart widget blink for when user swipe-closes app
                context.startService(new Intent(context, WidgetRunnablesService.class));
                Log.d("BlinkService", "Called startService() in BlinkWidget");
            } else if(action != null && (action.equals(EDIT_FILE_ACTION))){
                //Text editor from widget, just update that widget
                Log.d("BlinkWidget", "action is EDIT_FILE_ACTION");
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
            } else if(action != null && (action.equals(EDIT_FILE_FROM_OUTSIDE_ACTION))){
                //Happens whening saving, deleting, or renaming file.
                //Text editor from main, doesn't know what widgets are displaying this filename,
                //so update all widgets to get contents of whichever files again.
                Log.d("UPDATE", "UPDATING ALL WIDGETS!");
                updateAllWidgets = true;
                onUpdate(context, mgr, myAppWidgetIds);
                updateAllWidgets = false;
            } else if(action.equals(ACTION_APPWIDGET_OPTIONS_CHANGED)){
                //Happens when resizing widgets
                Log.d("BlinkWidget", "action was APPWIDGET_OPTIONS_CHANGED or UPDATE_OPTIONS");
                if(appWidgetIdLength < 1){
                    return;
                } else{
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

            //remove this id and info from widget
            File myFile = new File(context.getFilesDir(), widgetDataFileName);
            try {
                //get old values
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(myFile));
                HashMap<Integer, WidgetData> widgetIdValues = (HashMap<Integer, WidgetData>) inputStream.readObject();
                HashMap<Integer, WidgetData> newWidgetIdValues = new HashMap<Integer, WidgetData>(widgetIdValues);
                inputStream.close();

                //loop through all ids
                for (HashMap.Entry<Integer, WidgetData> entry : widgetIdValues.entrySet()) {
                    int idKey = entry.getKey();
                    if(appWidgetId == idKey) {
                        newWidgetIdValues.remove(idKey);
                    }
                }

                //save updated fileName values back into my info file
                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(myFile));
                outputStream.writeObject(newWidgetIdValues);
                outputStream.flush();
                outputStream.close();
            } catch(IOException e){
            } catch(ClassNotFoundException e){
            }
        }
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) { //might need to override onReceive() to prevent reboot problem
        Log.d("CFFWactivity", "Called onEnabled()");
    }

    @Override
    public void onDisabled(Context context) {
        Log.d("CFFWactivity", "Called onDisabled()");
    }

    public static String getLatestFileContents(String fileName, Context context){
        String fileContents = "";
        File myDirectory = new File(context.getFilesDir(), notesDirectory);
        if(!myDirectory.exists()){
            myDirectory.mkdirs();
        }
        File textFile = new File(myDirectory, fileName);
        if(textFile.exists()) {
            try {
                BufferedReader fileReader = new BufferedReader(new FileReader(textFile));
                String line;
                while ((line = fileReader.readLine()) != null) {
                    fileContents = fileContents + line + "\n";
                }
                //remove last newline if file isn't empty string
                if (!fileContents.equals("")) {
                    fileContents = fileContents.substring(0, fileContents.length() - 1);
                }
            } catch (IOException e) {
                Log.d("MAIN", "IOException");
            }
            return fileContents;
        } else { //file was renamed or deleted
            return "";
        }
    }
}
