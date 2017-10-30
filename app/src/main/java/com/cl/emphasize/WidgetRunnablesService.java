package com.cl.emphasize;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

public class WidgetRunnablesService extends Service{

    public static String EDIT_FILE_FROM_OUTSIDE_ACTION = "ActionEditFileFromOutside";
    public static final String widgetDataFileName = "widget_data.dat";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("WidgetService", "Calling onStartCommand()");

        //may need this because some devices destroy service and runnable after user uses some other
        // app. OnTaskRemoved() not called.
        //But it starts up again from START_STICKY. This should post runnable again.

        //first check if widgetfile is empty. If empty, don't broadcast because of exception when
        //calling getFileName() for non-existent file in BlinkWidget. Not empty, then broadcast.
        File myFile = new File(getFilesDir(), widgetDataFileName);
        if(myFile.exists()){
            try {
                Log.d("WidgetService", "File exists");
                //get old values
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(myFile));
                HashMap<Integer, WidgetData> widgetIdValues = (HashMap<Integer, WidgetData>) inputStream.readObject();
                inputStream.close();

                if (!widgetIdValues.isEmpty()) {
                    Log.d("WidgetService", "Widgetfile hashmap not empty. Broadcast update All");
                    //broadcast update all widgets
                    Intent returnIntent = new Intent(getApplicationContext(), BlinkWidget.class);
                    returnIntent.setAction(EDIT_FILE_FROM_OUTSIDE_ACTION);
                    returnIntent.putExtra("x", "x");
                    sendBroadcast(returnIntent);
                }
            } catch (IOException e) {
                Log.d("WidgetService", "IOEXCEPTION @@@@@@@@@@@@@@@@@@@");
            } catch (ClassNotFoundException e) {
                Log.d("WidgetService", "CLASSNOTFOUND @@@@@@@@@@@@@@@@@@@");
            }
        }

        Log.d("WidgetService", "Bottom");
        return START_STICKY; //service restarts when ended
    }

    @Override
    public void onTaskRemoved(Intent intent){
        //When user closes app in android Recent-Apps list, all widgets' blink runnables are
        //destroyed. This service triggers all widget updates to post runnable once more.
        //Widget runnables not destroyed if Blink Note is not in Recent Apps (expected).
        Log.d("WidgetService", "Calling onTaskRemoved()");
        Intent returnIntent = new Intent(getApplicationContext(), BlinkWidget.class);
        returnIntent.setAction(EDIT_FILE_FROM_OUTSIDE_ACTION);
        returnIntent.putExtra("x","x");
        sendBroadcast(returnIntent);
    }

    @Override
    public IBinder onBind(Intent intent){
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
