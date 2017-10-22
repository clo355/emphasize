package com.cl.emphasize;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class WidgetRunnablesService extends Service{

    public static String EDIT_FILE_FROM_OUTSIDE_ACTION = "ActionEditFileFromOutside";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("WidgetService", "Calling onStartCommand()");

        Intent returnIntent = new Intent(getApplicationContext(), BlinkWidget.class);
        returnIntent.setAction(EDIT_FILE_FROM_OUTSIDE_ACTION);
        returnIntent.putExtra("x","x");
        sendBroadcast(returnIntent);

        return START_STICKY; //service restarts when ended
    }

    @Override
    public void onTaskRemoved(Intent intent){
        Log.d("WidgetService", "Calling onTaskRemoved()");
        //manifest service tag stopWithTask=false

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
