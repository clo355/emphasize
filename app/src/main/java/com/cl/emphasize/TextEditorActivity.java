package com.cl.emphasize;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Chris Lo
 */

public class TextEditorActivity extends AppCompatActivity {

    public static String EDIT_FILE_ACTION = "ActionEditFileForBlinkWidget";
    public static String EDIT_FILE_FROM_OUTSIDE_ACTION = "ActionEditFileFromOutside";
    public static final int NEW_FILE_REQUEST_CODE = 1;
    public static final String PREFS_NAME = "PreferenceFile";
    public static final String notesDirectory = "notes";

    String fileName;
    String originalFileContents;
    boolean isNewFile;
    boolean fromWidget;

    TextView fileNameDisplay;
    EditText textEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TextEditor", "called onCreate()");
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int globalTheme = settings.getInt("globalTheme", R.style.lightTheme);
        setTheme(globalTheme);

        setContentView(R.layout.activity_text_editor);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        fileNameDisplay = (TextView)findViewById(R.id.fileNameDisplay);
        textEditor = (EditText)findViewById(R.id.textEditor);
        textEditor.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE|
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        fromWidget = getIntent().getExtras().getBoolean("fromWidget", false);
        fileName = getIntent().getExtras().getString("fileName");
        isNewFile = getIntent().getExtras().getBoolean("isNewFile");
        originalFileContents = getIntent().getExtras().getString("fileContents");
        textEditor.setText(originalFileContents);
        textEditor.setSelection(textEditor.getText().length());

        if(fileName.length() <= 13) {
            fileNameDisplay.setText(fileName);
        } else{
            fileNameDisplay.setText(fileName.substring(0, 13) + "...");
        }

        Button saveButton = (Button)findViewById(R.id.textEditorSaveButton);
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(isNewFile){
                    //new file, go to save as
                    Intent saveAsIntent = new Intent(getApplicationContext(), SaveAsActivity.class);
                    saveAsIntent.putExtra("fileName", fileName);
                    saveAsIntent.putExtra("fileContents", textEditor.getText().toString());
                    saveAsIntent.putExtra("isNewFile", isNewFile);
                    startActivityForResult(saveAsIntent, NEW_FILE_REQUEST_CODE);
                    //on return to Main, goes to the overridden onActivityResult()
                } else{
                    //overwrite file with given fileName
                    File myDirectory = new File(getFilesDir(), notesDirectory);
                    if(!myDirectory.exists()){
                        myDirectory.mkdirs();
                    }
                    File oldFile = new File(myDirectory, fileName);
                    oldFile.delete();
                    File newFile = new File(myDirectory, fileName);
                    try{
                        FileOutputStream myOutputStream = new FileOutputStream(newFile, false);
                        myOutputStream.write(textEditor.getText().toString().getBytes());
                        myOutputStream.close();
                    } catch(FileNotFoundException e){
                        Log.d("SAVEAS", "FileNotFoundException");
                    } catch(IOException e) {
                        Log.d("SAVEAS", "IOException");
                    }
                    originalFileContents = textEditor.getText().toString();
                    showAsShortToast("Saved");

                    //trigger widget updates
                    Intent returnIntent = new Intent(getApplicationContext(), BlinkWidget.class);
                    returnIntent.setAction(EDIT_FILE_FROM_OUTSIDE_ACTION);
                    //widget seems to only receive broadcast if there's extras in it
                    returnIntent.putExtra("fileName", fileName);
                    returnIntent.putExtra("fileContents", originalFileContents);
                    sendBroadcast(returnIntent);
                }
            }
        });

        Button saveAsButton = (Button)findViewById(R.id.textEditorSaveAsButton);
        saveAsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent saveAsIntent = new Intent(getApplicationContext(), SaveAsActivity.class);
                saveAsIntent.putExtra("fileName", fileName);
                saveAsIntent.putExtra("fileContents", textEditor.getText().toString());
                saveAsIntent.putExtra("isNewFile", isNewFile);
                startActivityForResult(saveAsIntent, NEW_FILE_REQUEST_CODE);
                //on return to Main, goes to the overridden onActivityResult()
            }
        });

        Button backButton = (Button)findViewById(R.id.textEditorBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent){
        if(requestCode == NEW_FILE_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                //user pressed ok and saved file
                isNewFile = returnedIntent.getExtras().getBoolean("isNewFile");
                fileName = returnedIntent.getExtras().getString("fileName");
                originalFileContents = textEditor.getText().toString();
                if(fileName.length() <= 13) {
                    fileNameDisplay.setText(fileName);
                } else{
                    fileNameDisplay.setText(fileName.substring(0, 13) + "...");
                }
                showAsShortToast("Saved as " + fileName);
            }
            if(resultCode == Activity.RESULT_CANCELED){
                //user pressed cancel or system back in SaveAsActivity
            }
        }
    }

    @Override
    public void onBackPressed(){
        if(textEditor.getText().toString().equals(originalFileContents)){
            //saved, or no changes. exit
            if(fromWidget){ //TextEditor was opened by widget
                Log.d("TextEditor", "fileName is " + fileName);
                Log.d("TextEditor", "fileContents is " + originalFileContents);
                Log.d("TextEditor", "widgetId is " + getIntent().getExtras().getInt("widgetId"));
                Log.d("TextEditor", "blinkDelay is " + getIntent().getExtras().getInt("currentBlinkDelay"));
                Log.d("TextEditor", "backgroundColor is " + getIntent().getExtras().getString("currentBackgroundColor"));
                Intent returnIntent = new Intent(getApplicationContext(), BlinkWidget.class);
                returnIntent.setAction(EDIT_FILE_ACTION);
                returnIntent.putExtra("fileName", fileName);
                returnIntent.putExtra("fileContents", originalFileContents);
                returnIntent.putExtra("blinkDelay", getIntent().getExtras().getInt("currentBlinkDelay"));
                returnIntent.putExtra("backgroundColor", getIntent().getExtras().getString("currentBackgroundColor"));
                returnIntent.putExtra("widgetId", getIntent().getExtras().getInt("widgetId"));
                sendBroadcast(returnIntent);
                finish();
            } else{ //TextEditor opened from Main
                finish();
            }
        } else{
            //changes were made. ask "You have unsaved changes. Exit without saving?" Cancel Exit
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE: { //exit
                            finish();
                            break;
                        }
                        case DialogInterface.BUTTON_NEGATIVE: { //cancel
                            break;
                        }
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(TextEditorActivity.this);
            builder.setMessage("Changes unsaved. Exit anyway?")
                    .setPositiveButton("Exit", dialogClickListener)
                    .setNegativeButton("Cancel", dialogClickListener)
                    .show();
        }
    }

    public void showAsShortToast(String text){
        CharSequence toastText = text;
        int duration = Toast.LENGTH_SHORT; //2 seconds
        Toast toast = Toast.makeText(getApplicationContext(), toastText, duration);
        toast.show();
    }
}
