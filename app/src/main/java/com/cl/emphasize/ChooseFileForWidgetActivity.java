package com.cl.emphasize;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Chris Lo
 */

public class ChooseFileForWidgetActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "PreferenceFile";
    protected ArrayList<String> myFileNameArray;
    protected String fileContents = "";
    protected String fileName = "";
    protected boolean globalIsConfig; //differentiate between configuration and user pressing widget button
    protected int blinkDelay = 333;
    protected String backgroundColor = "overwritten";

    @TargetApi(26)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int globalTheme = settings.getInt("globalTheme", R.style.lightTheme);
        setTheme(globalTheme);

        setContentView(R.layout.activity_choose_file_for_widget);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ListView listView = (ListView) findViewById(R.id.listViewCFFW);
        TextView noFilesView = (TextView) findViewById(R.id.noFilesLabelCFFW);
        Button cancelButton = (Button)findViewById(R.id.cancelButtonCFFW);

        //Initially populate ListView
        myFileNameArray = new ArrayList<String>();
        File[] fileListOnCreate = getFilesDir().listFiles();
        for(File foundFile : fileListOnCreate){
            String foundFileName = foundFile.getName();
            myFileNameArray.add(foundFileName);
        }

        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                new ArrayList(myFileNameArray));
        //myFileNameArray will be deleted by clear(), if passed by reference! So used new ArrayList().
        listView.setAdapter(listViewAdapter);

        //"No files found" label
        if(fileListOnCreate.length == 0){
            noFilesView.setText("No notes found");
        } else{
            noFilesView.setText("");
        }

        //check if this is for widget configuration
        Intent maybeConfigIntent = getIntent();
        Bundle extras = maybeConfigIntent.getExtras();
        int configWidgetId = 0;
        if(extras != null){ //should always contain extras
            boolean isConfig = extras.getBoolean("isConfig", true);
            if(isConfig){
                Log.d("CFFWactivity", "isConfig was true");
                globalIsConfig = true;
                configWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);
                Log.d("CFFWactivity", "configWidgetId is " + configWidgetId);
                backgroundColor = "white";
                blinkDelay = 0;
            } else{
                Log.d("CFFWactivity", "isConfig was false");
                globalIsConfig = false;
                backgroundColor = getIntent().getExtras().getString("currentBackgroundColor");
                blinkDelay = getIntent().getExtras().getInt("currentBlinkDelay");
            }
        } else{
            //extras shouldn't be null
            Log.d("CFFWactivity", "Extras were null");
        }
        final int returnConfigId = configWidgetId; //not used if it's not config

        //Press listView object, show options, send contents to widget
        final Intent intent = new Intent(getApplicationContext(), BlinkWidget.class);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
                final TextView speedLabelDisplay = new TextView(ChooseFileForWidgetActivity.this);
                speedLabelDisplay.setGravity(Gravity.CENTER);
                speedLabelDisplay.setText("\n\nBlink Speed");
                speedLabelDisplay.setTypeface(Typeface.DEFAULT_BOLD);

                final TextView speedDisplay = new TextView(ChooseFileForWidgetActivity.this);
                speedDisplay.setGravity(Gravity.CENTER);

                final TextView emptySpace1 = new TextView(ChooseFileForWidgetActivity.this);
                emptySpace1.setGravity(Gravity.CENTER);
                emptySpace1.setText("");

                SeekBar delaySeekBar = new SeekBar(ChooseFileForWidgetActivity.this);
                delaySeekBar.setMax(100);
                //Start off as:
                if(blinkDelay == 0){
                    speedDisplay.setText("No Blink");
                    delaySeekBar.setProgress(0);
                } else if(blinkDelay <= 166){
                    speedDisplay.setText("Faster");
                    delaySeekBar.setProgress(100);
                } else if(blinkDelay <= 332) {
                    speedDisplay.setText("Fast");
                    delaySeekBar.setProgress(80);
                } else if(blinkDelay <= 498){
                    speedDisplay.setText("Normal");
                    delaySeekBar.setProgress(60);
                } else if(blinkDelay <= 664){
                    speedDisplay.setText("Slow");
                    delaySeekBar.setProgress(40);
                } else if(blinkDelay <= 830){
                    speedDisplay.setText("Slower");
                    delaySeekBar.setProgress(20);
                } else{
                    speedDisplay.setText("No Blink");
                    delaySeekBar.setProgress(0);
                }

                final TextView backgroundColorLabelDisplay = new TextView(ChooseFileForWidgetActivity.this);
                backgroundColorLabelDisplay.setGravity(Gravity.CENTER);
                backgroundColorLabelDisplay.setText("\n\nBackground Color");
                backgroundColorLabelDisplay.setTypeface(Typeface.DEFAULT_BOLD);

                final TextView backgroundColorDisplay = new TextView(ChooseFileForWidgetActivity.this);
                backgroundColorDisplay.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams bgParams = new LinearLayout.LayoutParams( //width and height
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                bgParams.gravity = Gravity.CENTER;
                backgroundColorDisplay.setLayoutParams(bgParams);
                backgroundColorDisplay.setText("      ");

                final TextView emptySpace2 = new TextView(ChooseFileForWidgetActivity.this);
                emptySpace2.setGravity(Gravity.CENTER);
                emptySpace2.setText("");

                SeekBar backgroundColorSeekBar = new SeekBar(ChooseFileForWidgetActivity.this);
                backgroundColorSeekBar.setMax(7);
                //start off as:
                switch(backgroundColor){
                    case "red":{
                        backgroundColorSeekBar.setProgress(0);
                        backgroundColorDisplay.setBackgroundColor(Color.rgb(255, 158, 158));
                        break;
                    }
                    case "orange":{
                        backgroundColorSeekBar.setProgress(1);
                        backgroundColorDisplay.setBackgroundColor(Color.rgb(225, 231, 175));
                        break;
                    }
                    case "yellow":{
                        backgroundColorSeekBar.setProgress(2);
                        backgroundColorDisplay.setBackgroundColor(Color.rgb(255, 253, 193));
                        break;
                    }
                    case "green":{
                        backgroundColorSeekBar.setProgress(3);
                        backgroundColorDisplay.setBackgroundColor(Color.rgb(197, 255, 195));
                        break;
                    }
                    case "blue":{
                        backgroundColorSeekBar.setProgress(4);
                        backgroundColorDisplay.setBackgroundColor(Color.rgb(163, 220, 255));
                        break;
                    }
                    case "purple":{
                        backgroundColorSeekBar.setProgress(5);
                        backgroundColorDisplay.setBackgroundColor(Color.rgb(232, 167, 255));
                        break;
                    }
                    case "gray":{
                        backgroundColorSeekBar.setProgress(6);
                        backgroundColorDisplay.setBackgroundColor(Color.rgb(200, 200, 200));
                        break;
                    }
                    case "white":{
                        backgroundColorSeekBar.setProgress(7);
                        backgroundColorDisplay.setBackgroundColor(Color.rgb(238, 238, 238));
                        break;
                    }
                }

                //Blink speed seekbar, int 0 to 100
                delaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int speed, boolean fromUser) {
                        if(speed > 80){
                            blinkDelay = 166;
                            speedDisplay.setText("Faster");
                        } else if(speed > 60) {
                            blinkDelay = 332;
                            speedDisplay.setText("Fast");
                        } else if(speed > 40){
                            blinkDelay = 498;
                            speedDisplay.setText("Normal");
                        } else if(speed > 20){
                            blinkDelay = 664;
                            speedDisplay.setText("Slow");
                        } else if(speed > 1){
                            blinkDelay = 830;
                            speedDisplay.setText("Slower");
                        } else{
                            blinkDelay = 0;
                            speedDisplay.setText("No Blink");
                        }
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar){
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar){
                    }
                });

                //Background color seekbar
                backgroundColorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int color, boolean fromUser) {
                        switch(color){
                            case 0:{
                                backgroundColor = "red";
                                backgroundColorDisplay.setBackgroundColor(Color.rgb(255, 158, 158));
                                break;
                            }
                            case 1:{
                                backgroundColor = "orange";
                                backgroundColorDisplay.setBackgroundColor(Color.rgb(255, 231, 175));
                                break;
                            }
                            case 2:{
                                backgroundColor = "yellow";
                                backgroundColorDisplay.setBackgroundColor(Color.rgb(255, 253, 193));
                                break;
                            }
                            case 3:{
                                backgroundColor = "green";
                                backgroundColorDisplay.setBackgroundColor(Color.rgb(197, 255, 195));
                                break;
                            }
                            case 4:{
                                backgroundColor = "blue";
                                backgroundColorDisplay.setBackgroundColor(Color.rgb(163, 220, 255));
                                break;
                            }
                            case 5:{
                                backgroundColor = "purple";
                                backgroundColorDisplay.setBackgroundColor(Color.rgb(232, 167, 255));
                                break;
                            }
                            case 6:{
                                backgroundColor = "gray";
                                backgroundColorDisplay.setBackgroundColor(Color.rgb(200, 200, 200));
                                break;
                            }
                            case 7:{
                                backgroundColor = "white";
                                backgroundColorDisplay.setBackgroundColor(Color.rgb(238, 238, 238));
                                break;
                            }
                        }
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar){
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar){
                    }
                });

                LinearLayout widgetSettingsLayout = new LinearLayout(ChooseFileForWidgetActivity.this);
                widgetSettingsLayout.setOrientation(LinearLayout.VERTICAL);
                widgetSettingsLayout.addView(speedLabelDisplay);
                widgetSettingsLayout.addView(speedDisplay);
                widgetSettingsLayout.addView(emptySpace1);
                widgetSettingsLayout.addView(delaySeekBar);
                widgetSettingsLayout.addView(backgroundColorLabelDisplay);
                widgetSettingsLayout.addView(backgroundColorDisplay);
                widgetSettingsLayout.addView(emptySpace2);
                widgetSettingsLayout.addView(backgroundColorSeekBar);

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        if(which == DialogInterface.BUTTON_POSITIVE){
                            //Pressed OK in widget settings dialog
                            dialog.dismiss();
                            File fileClicked = new File(getFilesDir(), myFileNameArray.get(position));
                            fileContents = getEmphasizeFileContents(fileClicked);
                            fileName = fileClicked.getName();
                            if(globalIsConfig){ //is widget configuration
                                //store everything in SharedPreferences. onReceive() will
                                //get them only after config, then update to start runnable
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("configFileName", fileName);
                                editor.putString("configFileContents", fileContents);
                                editor.putInt("configBlinkDelay", blinkDelay);
                                editor.putString("configBackgroundColor", backgroundColor);
                                editor.putInt("configWidgetId", returnConfigId);
                                editor.commit();

                                Intent configIntent = new Intent(getBaseContext(), ChooseFileForWidgetActivity.class);
                                configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, returnConfigId);
                                setResult(RESULT_OK, configIntent);
                                Log.d("CFFWactivity", "CFFWactivity config RESULT_OK, doing finish()");
                                finish();
                            } else{ //CFFWactivity was started by widget corner button
                                intent.setAction(BlinkWidget.CHOOSE_FILE_ACTION);
                                intent.putExtra("fileName", fileName);
                                intent.putExtra("fileContents", fileContents);
                                intent.putExtra("blinkDelay", blinkDelay);
                                intent.putExtra("backgroundColor", backgroundColor);
                                //Send widget ID back so onReceive() sees which widget to update
                                intent.putExtra("widgetId", getIntent().getExtras().getInt("widgetId"));
                                sendBroadcast(intent); //broadcasted to widget's onReceive()
                                finish();
                            }
                        } else{ //BUTTON_NEGATIVE
                            dialog.dismiss();
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChooseFileForWidgetActivity.this);
                builder.setTitle("Widget Settings")
                        .setView(widgetSettingsLayout)
                        .setCancelable(true)
                        .setPositiveButton("OK", dialogClickListener)
                        .setNegativeButton("CANCEL", dialogClickListener)
                        .show();
            }
        });


        //Press cancel
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private String getEmphasizeFileContents(File whichFile){
        String fileContents = "";
        try{
            BufferedReader fileReader = new BufferedReader(new FileReader(whichFile));
            String line;
            while((line = fileReader.readLine()) != null){
                fileContents = fileContents + line + "\n";
            }
            //remove last newline if file isn't empty string
            if(!fileContents.equals("")){
                fileContents = fileContents.substring(0, fileContents.length() - 1);
            }
        } catch(IOException e){
            Log.d("CFFWactivity", "IOException");
        }
        return fileContents;
    }

    @Override
    public void onBackPressed(){ //Same as cancel
        finish();
    }
}
