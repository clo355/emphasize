package com.cl.emphasize;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ChooseFileForWidgetActivity extends AppCompatActivity {

    protected ArrayList<String> myFileNameArray;
    protected String widgetType;
    protected String fileContents = "";
    protected int blinkDelay = 333;
    protected int jiggleDelay = 200;
    protected String backgroundColor = "overwritten";
    protected int backgroundTransparency;
    protected String textColor = "black";
    protected String textSize = "medium"; //Large, medium, small

    @TargetApi(26)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_file_for_widget);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ListView listView = (ListView) findViewById(R.id.listViewCFFW);
        TextView noFilesView = (TextView) findViewById(R.id.noFilesLabelCFFW);
        final Button widgetSettingsButton = (Button)findViewById(R.id.widgetSettingsButtonCFFW);
        Button cancelButton = (Button)findViewById(R.id.cancelButtonCFFW);
        final Intent intent;

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

        //Press listView object, send contents to widget
        //Widget type: "blink", "jiggle", or "normal"
        backgroundColor = getIntent().getExtras().getString("currentBackgroundColor");
        //bgColor always white. widget not sending correct color.
        widgetType = getIntent().getExtras().getString("widgetType");
        if(widgetType.equals("blink")){
            blinkDelay = getIntent().getExtras().getInt("currentBlinkDelay");
            if(blinkDelay <= 0){ //catch 0 from BlinkWidget default
                blinkDelay = 333;
            }
            intent = new Intent(getApplicationContext(), BlinkWidget.class);
        } else if(widgetType.equals("jiggle")){
            intent = new Intent(getApplicationContext(), JiggleWidget.class);
        } else{ //"normal"
            intent = new Intent(getApplicationContext(), NormalWidget.class);
        }

        switch(widgetType) {
            case "blink":{
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        File fileClicked = new File(getFilesDir(), myFileNameArray.get(position));
                        fileContents = getEmphasizeFileContents(fileClicked);
                        intent.setAction(BlinkWidget.CHOOSE_FILE_ACTION);
                        intent.putExtra("fileContents", fileContents);
                        intent.putExtra("blinkDelay", blinkDelay);
                        intent.putExtra("backgroundColor", backgroundColor);
                        intent.putExtra("textColor", textColor);
                        intent.putExtra("textSize", textSize);
                        //Send widget ID back so onReceive() sees which widget to update
                        intent.putExtra("widgetId", getIntent().getExtras().getInt("widgetId"));
                        Log.d("CFFWactivity", "put extras, sent intent back to BlinkWidget");
                        sendBroadcast(intent); //broadcasted to widget's onReceive()
                        finish();
                    }
                });
                break;
            }
            case "jiggle":{
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        File fileClicked = new File(getFilesDir(), myFileNameArray.get(position));
                        fileContents = getEmphasizeFileContents(fileClicked);
                        intent.setAction(JiggleWidget.CHOOSE_FILE_ACTION);
                        intent.putExtra("fileContents", fileContents);
                        intent.putExtra("jiggleDelay", jiggleDelay);
                        intent.putExtra("backgroundColor", backgroundColor);
                        intent.putExtra("textColor", textColor);
                        intent.putExtra("textSize", textSize);
                        intent.putExtra("widgetId", getIntent().getExtras().getInt("widgetId"));
                        sendBroadcast(intent);
                        finish();
                    }
                });
                break;
            }
            case "normal":{
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        File fileClicked = new File(getFilesDir(), myFileNameArray.get(position));
                        fileContents = getEmphasizeFileContents(fileClicked);
                        intent.setAction(NormalWidget.CHOOSE_FILE_ACTION);
                        intent.putExtra("fileContents", fileContents);
                        intent.putExtra("backgroundColor", backgroundColor);
                        intent.putExtra("textColor", textColor);
                        intent.putExtra("textSize", textSize);
                        intent.putExtra("widgetId", getIntent().getExtras().getInt("widgetId"));
                        sendBroadcast(intent);
                        finish();
                    }
                });
                break;
            }
        }

        //delay: slider for ms delay
        //text: size slider, color slider
        //background: color slider
        widgetSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                final TextView speedLabelDisplay = new TextView(ChooseFileForWidgetActivity.this);
                speedLabelDisplay.setGravity(Gravity.CENTER);
                speedLabelDisplay.setText("\n\nSpeed");
                speedLabelDisplay.setTypeface(Typeface.DEFAULT_BOLD);

                final TextView speedDisplay = new TextView(ChooseFileForWidgetActivity.this);
                speedDisplay.setGravity(Gravity.CENTER);

                final TextView emptySpace1 = new TextView(ChooseFileForWidgetActivity.this);
                emptySpace1.setGravity(Gravity.CENTER);
                emptySpace1.setText("");

                SeekBar delaySeekBar = new SeekBar(ChooseFileForWidgetActivity.this);
                delaySeekBar.setMax(100);
                //Start off as:
                if(blinkDelay <= 50){
                    speedDisplay.setText("Faster");
                    delaySeekBar.setProgress(100);
                } else if(blinkDelay <= 120) {
                    speedDisplay.setText("Fast");
                    delaySeekBar.setProgress(75);
                } else if(blinkDelay <= 333){
                    speedDisplay.setText("Normal");
                    delaySeekBar.setProgress(50);
                } else if(blinkDelay <= 666){
                    speedDisplay.setText("Slow");
                    delaySeekBar.setProgress(25);
                } else{
                    speedDisplay.setText("Slower");
                    delaySeekBar.setProgress(0);
                }

                //SeekBar textColorSeekBar = new SeekBar(ChooseFileForWidgetActivity.this);
                //SeekBar textSizeSeekBar = new SeekBar(ChooseFileForWidgetActivity.this);
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
                        backgroundColorDisplay.setBackgroundColor(Color.rgb(255, 37, 37));
                        break;
                    }
                    case "orange":{
                        backgroundColorSeekBar.setProgress(1);
                        backgroundColorDisplay.setBackgroundColor(Color.rgb(225, 179, 37));
                        break;
                    }
                    case "yellow":{
                        backgroundColorSeekBar.setProgress(2);
                        backgroundColorDisplay.setBackgroundColor(Color.rgb(234, 236, 14));
                        break;
                    }
                    case "green":{
                        backgroundColorSeekBar.setProgress(3);
                        backgroundColorDisplay.setBackgroundColor(Color.rgb(111, 236, 14));
                        break;
                    }
                    case "blue":{
                        backgroundColorSeekBar.setProgress(4);
                        backgroundColorDisplay.setBackgroundColor(Color.rgb(14, 197, 236));
                        break;
                    }
                    case "purple":{
                        backgroundColorSeekBar.setProgress(5);
                        backgroundColorDisplay.setBackgroundColor(Color.rgb(189, 14, 236));
                        break;
                    }
                    case "gray":{
                        backgroundColorSeekBar.setProgress(6);
                        backgroundColorDisplay.setBackgroundColor(Color.rgb(163, 163, 163));
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
                            blinkDelay = 50;
                            speedDisplay.setText("Faster");
                        } else if(speed > 60) {
                            blinkDelay = 120;
                            speedDisplay.setText("Fast");
                        } else if(speed > 40){
                            blinkDelay = 333;
                            speedDisplay.setText("Normal");
                        } else if(speed > 20){
                            blinkDelay = 666;
                            speedDisplay.setText("Slow");
                        } else{
                            blinkDelay = 999;
                            speedDisplay.setText("Slower");
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
                                backgroundColorDisplay.setBackgroundColor(Color.rgb(255, 37, 37));
                                break;
                            }
                            case 1:{
                                backgroundColor = "orange";
                                backgroundColorDisplay.setBackgroundColor(Color.rgb(225, 179, 37));
                                break;
                            }
                            case 2:{
                                backgroundColor = "yellow";
                                backgroundColorDisplay.setBackgroundColor(Color.rgb(234, 236, 14));
                                break;
                            }
                            case 3:{
                                backgroundColor = "green";
                                backgroundColorDisplay.setBackgroundColor(Color.rgb(111, 236, 14));
                                break;
                            }
                            case 4:{
                                backgroundColor = "blue";
                                backgroundColorDisplay.setBackgroundColor(Color.rgb(14, 197, 236));
                                break;
                            }
                            case 5:{
                                backgroundColor = "purple";
                                backgroundColorDisplay.setBackgroundColor(Color.rgb(189, 14, 236));
                                break;
                            }
                            case 6:{
                                backgroundColor = "gray";
                                backgroundColorDisplay.setBackgroundColor(Color.rgb(163, 163, 163));
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
                if(widgetType.equals("blink")) {
                    widgetSettingsLayout.addView(speedLabelDisplay);
                    widgetSettingsLayout.addView(speedDisplay);
                    widgetSettingsLayout.addView(emptySpace1);
                    widgetSettingsLayout.addView(delaySeekBar);
                }
                //widgetSettingsLayout.addView(textColorSeekBar);
                //widgetSettingsLayout.addView(textSizeSeekBar);
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
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChooseFileForWidgetActivity.this);
                builder.setTitle("Widget Settings")
                        .setView(widgetSettingsLayout)
                        .setCancelable(true)
                        .setPositiveButton("OK", dialogClickListener)
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
        String emphasizeFileContents = "";
        try{
            BufferedReader fileReader = new BufferedReader(new FileReader(whichFile));
            String line;
            while((line = fileReader.readLine()) != null){
                emphasizeFileContents = emphasizeFileContents + line + "\n";
            }
        } catch(IOException e){
            Log.d("CFFWactivity", "IOException");
        }
        return emphasizeFileContents;
    }

    @Override
    public void onBackPressed(){ //Same as cancel
        finish();
    }
}
