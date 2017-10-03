package com.cl.emphasize;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    String fileName;
    String originalFileContents;
    boolean isNewFile;
    public static final int NEW_FILE_REQUEST_CODE = 1;
    public static final String PREFS_NAME = "PreferenceFile";

    TextView fileNameDisplay;
    EditText textEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int globalTheme = settings.getInt("globalTheme", R.style.lightTheme);
        setTheme(globalTheme);

        setContentView(R.layout.activity_text_editor);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        fileNameDisplay = (TextView)findViewById(R.id.fileNameDisplay);
        textEditor = (EditText)findViewById(R.id.textEditor);

        fileName = getIntent().getExtras().getString("fileName");
        isNewFile = getIntent().getExtras().getBoolean("isNewFile");
        originalFileContents = getIntent().getExtras().getString("fileContents");
        textEditor.setText(originalFileContents);

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
                    saveAsIntent.putExtra("isNewFile", isNewFile);
                    saveAsIntent.putExtra("fileName", fileName);
                    saveAsIntent.putExtra("fileContents", textEditor.getText().toString());
                    startActivityForResult(saveAsIntent, NEW_FILE_REQUEST_CODE);
                    //on return to Main, calls the overridden onActivityResult()
                } else{
                    //overwrite file with given fileName
                    File oldFile = new File(getFilesDir(), fileName);
                    oldFile.delete();
                    File newFile = new File(getFilesDir(), fileName);
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
                }
            }
        });

        Button saveAsButton = (Button)findViewById(R.id.textEditorSaveAsButton);
        saveAsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent saveAsIntent = new Intent(getApplicationContext(), SaveAsActivity.class);
                //saveAsIntent.putExtra("isNewFile", isNewFile);
                saveAsIntent.putExtra("fileName", fileName);
                saveAsIntent.putExtra("fileContents", textEditor.getText().toString());
                startActivityForResult(saveAsIntent, NEW_FILE_REQUEST_CODE);
                //on return to Main, calls the overridden onActivityResult()
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
            //no changes. exit
            finish();
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
            builder.setMessage("You have unsaved changes.")
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
