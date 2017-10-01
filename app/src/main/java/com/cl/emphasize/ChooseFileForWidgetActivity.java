package com.cl.emphasize;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
    protected String backgroundColor = "white";
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
            noFilesView.setText("No files found");
        } else{
            noFilesView.setText("");
        }

        //Press listView object, send contents to widget
        //Widget type: "blink", "jiggle", or "normal"
        widgetType = getIntent().getExtras().getString("widgetType");
        if(widgetType.equals("blink")){
            intent = new Intent(getApplicationContext(), BlinkWidget.class);
        } else if(widgetType.equals("jiggle")){
            intent = new Intent(getApplicationContext(), JiggleWidget.class);
        } else{
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
                final TextView sampleDisplay = new TextView(ChooseFileForWidgetActivity.this);
                final TextView delayDisplay = new TextView(ChooseFileForWidgetActivity.this);

                sampleDisplay.setGravity(Gravity.CENTER);
                sampleDisplay.setText("Display");

                delayDisplay.setGravity(Gravity.CENTER);
                delayDisplay.setText("\n\nNormal");

                SeekBar delaySeekBar = new SeekBar(ChooseFileForWidgetActivity.this);
                delaySeekBar.setMax(100);
                delaySeekBar.setProgress(50);

                //SeekBar textColorSeekBar = new SeekBar(ChooseFileForWidgetActivity.this);
                //SeekBar textSizeSeekBar = new SeekBar(ChooseFileForWidgetActivity.this);
                //SeekBar backgroundColorSeekBar = new SeekBar(ChooseFileForWidgetActivity.this);

                delaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int speed, boolean fromUser) {
                        if(speed > 80){
                            blinkDelay = 50;
                            delayDisplay.setText("\n\nFaster");
                        } else if(speed > 60) {
                            blinkDelay = 120;
                            delayDisplay.setText("\n\nFast");
                        } else if(speed > 40){
                            blinkDelay = 333;
                            delayDisplay.setText("\n\nNormal");
                        } else if(speed > 20){
                            blinkDelay = 666;
                            delayDisplay.setText("\n\nSlow");
                        } else{
                            blinkDelay = 999;
                            delayDisplay.setText("\n\nSlower");
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
                widgetSettingsLayout.addView(delayDisplay);
                widgetSettingsLayout.addView(delaySeekBar);
                //widgetSettingsLayout.addView(textColorSeekBar);
                //widgetSettingsLayout.addView(textSizeSeekBar);
                //widgetSettingsLayout.addView(backgroundColorSeekBar);

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
