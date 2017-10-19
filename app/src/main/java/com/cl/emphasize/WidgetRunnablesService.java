package com.cl.emphasize;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class WidgetRunnablesService extends Service{

    public static String EDIT_FILE_FROM_OUTSIDE_ACTION = "ActionEditFileFromOutside";

    public WidgetRunnablesService(){
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        //trigger widget updates
        Intent returnIntent = new Intent(getApplicationContext(), BlinkWidget.class);
        returnIntent.setAction(EDIT_FILE_FROM_OUTSIDE_ACTION);
        returnIntent.putExtra("x", "x");
        sendBroadcast(returnIntent);

        return START_STICKY; //service restarts when ended
    }

    @Override
    public void onTaskRemoved(Intent i){
        //manifest service tag stopWithTask=false
    }

    @Override
    public IBinder onBind(Intent intent){
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
