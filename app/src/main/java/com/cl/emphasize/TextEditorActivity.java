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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 * @author Chris Lo
 */

public class TextEditorActivity extends AppCompatActivity {

    public static String EDIT_FILE_ACTION = "ActionEditFileForBlinkWidget";
    public static String EDIT_FILE_FROM_OUTSIDE_ACTION = "ActionEditFileFromOutside";
    public static final int NEW_FILE_REQUEST_CODE = 1;
    public static final String PREFS_NAME = "PreferenceFile";
    public static final String widgetDataFileName = "widget_data.dat";
    public static final String notesDirectory = "notes";
    protected FileOutputStream myOutputStream;

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

        if(fileName.length() <= 15) {
            fileNameDisplay.setText(fileName);
        } else{
            fileNameDisplay.setText(fileName.substring(0, 15) + "...");
        }

        fileNameDisplay.setOnClickListener(new View.OnClickListener(){
            //Pay attention to using originalFileContents or TextEditor contents.
            @Override
            public void onClick(View view) {
                final EditText renameEditText = new EditText(TextEditorActivity.this);
                renameEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                renameEditText.setHint(fileName);

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        //Create an EditText for user input
                        LinearLayout.LayoutParams renameLayout = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        renameEditText.setLayoutParams(renameLayout);

                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:{//OK
                                //if renaming to same name or blank, do nothing
                                final String renameToThis = renameEditText.getText().toString();
                                if (renameToThis.equals("") ||
                                        renameToThis.equals(fileName)) {
                                    break;
                                }

                                if(isNewFile){ //Default new file was renamed
                                    fileName = renameToThis;
                                    fileNameDisplay.setText(renameToThis);
                                } else{ //An existing file was renamed
                                    if(fileNameAlreadyExists(renameToThis)){
                                        //ask to overwrite. Yes = file replacement. No = cancel
                                        final File myDirectory = new File(getFilesDir(), notesDirectory);
                                        if(!myDirectory.exists()){
                                            myDirectory.mkdirs();
                                        }
                                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE: { //Overwrite
                                                        File oldFile = new File(myDirectory, fileName);
                                                        oldFile.delete();
                                                        File newFile = new File(myDirectory, renameToThis);
                                                        try {
                                                            myOutputStream = new FileOutputStream(newFile, false);
                                                            myOutputStream.write(originalFileContents.getBytes());
                                                            myOutputStream.close();
                                                        } catch (FileNotFoundException e) {
                                                            Log.d("SAVEAS", "FileNotFoundException");
                                                        } catch (IOException e) {
                                                            Log.d("SAVEAS", "IOException");
                                                        }

                                                        //update widget info file with the new name
                                                        File myFile = new File(getFilesDir(), widgetDataFileName);
                                                        try {
                                                            //get old values
                                                            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(myFile));
                                                            HashMap<Integer, WidgetData> widgetIdValues = (HashMap<Integer, WidgetData>) inputStream.readObject();
                                                            HashMap<Integer, WidgetData> newWidgetIdValues = new HashMap<Integer, WidgetData>(widgetIdValues);
                                                            inputStream.close();

                                                            //loop through all ids, change file name of any widgets displaying old file to new file.
                                                            for (HashMap.Entry<Integer, WidgetData> entry : widgetIdValues.entrySet()) {
                                                                int idKey = entry.getKey();
                                                                WidgetData oldWidgetData = entry.getValue();
                                                                if (oldWidgetData.getFileName().equals(fileName)) {
                                                                    String newFileName = renameToThis;
                                                                    String sameFileContents = originalFileContents;
                                                                    int sameBlinkDelay = oldWidgetData.getBlinkDelay();
                                                                    String sameBackgroundColor = oldWidgetData.getBackgroundColor();
                                                                    WidgetData newWidgetData = new WidgetData(idKey, newFileName, sameFileContents,
                                                                            sameBlinkDelay, sameBackgroundColor);
                                                                    newWidgetIdValues.put(idKey, newWidgetData);
                                                                }
                                                            }

                                                            //save updated fileName values back into my info file
                                                            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(myFile));
                                                            outputStream.writeObject(newWidgetIdValues);
                                                            outputStream.flush();
                                                            outputStream.close();

                                                            //tell widgets to update with new name
                                                            Intent returnIntent = new Intent(getApplicationContext(), BlinkWidget.class);
                                                            returnIntent.setAction(EDIT_FILE_FROM_OUTSIDE_ACTION);
                                                            //widget seems to only receive broadcast if there's extras in it
                                                            returnIntent.putExtra("fileName", newFile.getName());
                                                            sendBroadcast(returnIntent);
                                                            fileName = renameToThis;
                                                        } catch (IOException e) {
                                                            Log.d("BlinkWidget", "IOEXCEPTION in onReceive()");
                                                        } catch (ClassNotFoundException e) {
                                                            Log.d("BlinkWidget", "CLASSNOTEFOUNDEXCEPTION in onReceive()");
                                                        }

                                                        fileNameDisplay.setText(renameToThis);
                                                        showAsShortToast("Overwritten");
                                                        break;
                                                    }
                                                    case DialogInterface.BUTTON_NEGATIVE: { //cancel
                                                        break;
                                                    }
                                                }
                                            }
                                        };

                                        AlertDialog.Builder builder = new AlertDialog.Builder(TextEditorActivity.this);
                                        builder.setMessage("A note named \"" + renameToThis +
                                                "\" already exists.\nOverwrite it?")
                                                .setPositiveButton("Overwrite", dialogClickListener)
                                                .setNegativeButton("Cancel", dialogClickListener)
                                                .show();
                                    } else{ //new name not taken do file replacement.
                                        final File myDirectory = new File(getFilesDir(), notesDirectory);
                                        FileOutputStream myOutputStream;
                                        File newFile = new File(myDirectory, renameEditText.getText().toString());
                                        try {
                                            myOutputStream = new FileOutputStream(newFile, false);
                                            myOutputStream.write(originalFileContents.getBytes());
                                            myOutputStream.close();
                                        } catch (FileNotFoundException e) {
                                            Log.d("SAVEAS", "FileNotFoundException");
                                        } catch (IOException e) {
                                            Log.d("SAVEAS", "IOException");
                                        }
                                        File oldFile = new File(myDirectory, fileName);
                                        oldFile.delete();

                                        //update widget info file with these new values
                                        File myFile = new File(getFilesDir(), widgetDataFileName);
                                        try {
                                            //get old values
                                            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(myFile));
                                            HashMap<Integer, WidgetData> widgetIdValues = (HashMap<Integer, WidgetData>) inputStream.readObject();
                                            HashMap<Integer, WidgetData> newWidgetIdValues = new HashMap<Integer, WidgetData>(widgetIdValues);
                                            inputStream.close();

                                            //loop through all ids, change file name of any widgets displaying old file to new file.
                                            for (HashMap.Entry<Integer, WidgetData> entry : widgetIdValues.entrySet()) {
                                                int idKey = entry.getKey();
                                                WidgetData oldWidgetData = entry.getValue();
                                                if (oldWidgetData.getFileName().equals(fileName)) {
                                                    String newFileName = newFile.getName();
                                                    String sameFileContents = originalFileContents;
                                                    int sameBlinkDelay = oldWidgetData.getBlinkDelay();
                                                    String sameBackgroundColor = oldWidgetData.getBackgroundColor();
                                                    WidgetData newWidgetData = new WidgetData(idKey, newFileName, sameFileContents,
                                                            sameBlinkDelay, sameBackgroundColor);
                                                    newWidgetIdValues.put(idKey, newWidgetData);
                                                }
                                            }

                                            //save updated fileName values back into my info file
                                            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(myFile));
                                            outputStream.writeObject(newWidgetIdValues);
                                            outputStream.flush();
                                            outputStream.close();

                                            //tell widgets to update with new name
                                            Intent returnIntent = new Intent(getApplicationContext(), BlinkWidget.class);
                                            returnIntent.setAction(EDIT_FILE_FROM_OUTSIDE_ACTION);
                                            //widget seems to only receive broadcast if there's extras in it
                                            returnIntent.putExtra("fileName", newFile.getName());
                                            sendBroadcast(returnIntent);
                                            fileName = newFile.getName();
                                            fileNameDisplay.setText(renameToThis);
                                            showAsShortToast("Renamed");
                                        } catch (IOException e) {
                                            Log.d("BlinkWidget", "IOEXCEPTION in onReceive()");
                                        } catch (ClassNotFoundException e) {
                                            Log.d("BlinkWidget", "CLASSNOTEFOUNDEXCEPTION in onReceive()");
                                        }
                                    }
                                }
                                break;
                            }
                            case DialogInterface.BUTTON_NEGATIVE:{ //Cancel
                                break;
                            }
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(TextEditorActivity.this);
                builder.setMessage("Rename file:")
                        .setView(renameEditText)
                        .setPositiveButton("OK", dialogClickListener)
                        .setNegativeButton("Cancel", dialogClickListener);
                final AlertDialog renameDialog = builder.create();
                renameDialog.show();

                //Bring up keyboard for dialog's EditText
                try {
                    renameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View view, boolean hasFocus) {
                            if (hasFocus) {
                                renameDialog.getWindow()
                                        .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                            }
                        }
                    });
                } catch(NullPointerException e){
                    //Keyboard won't automatically show
                }
            }
        });

        Button saveButton = (Button)findViewById(R.id.textEditorSaveButton);
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(isNewFile && fileName.equals("New Note")){
                    //new file with default name, go to save as
                    Intent saveAsIntent = new Intent(getApplicationContext(), SaveAsActivity.class);
                    saveAsIntent.putExtra("fileName", fileName);
                    saveAsIntent.putExtra("fileContents", textEditor.getText().toString());
                    saveAsIntent.putExtra("isNewFile", isNewFile);
                    startActivityForResult(saveAsIntent, NEW_FILE_REQUEST_CODE);
                    //on return to Main, goes to the overridden onActivityResult()
                } else if(isNewFile && !fileName.equals("New Note")) {
                    final File myDirectory = new File(getFilesDir(), notesDirectory);
                    if(!myDirectory.exists()){
                        myDirectory.mkdirs();
                    }
                    if(fileNameAlreadyExists(fileName)){
                        //ask to overwrite. Yes, file rename/replacement. No, cancel
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE: { //Overwrite
                                        File oldFile = new File(myDirectory, fileName);
                                        oldFile.delete();
                                        File newFile = new File(myDirectory, fileName);
                                        try {
                                            myOutputStream = new FileOutputStream(newFile, false);
                                            myOutputStream.write(textEditor.getText().toString().getBytes());
                                            myOutputStream.close();
                                        } catch (FileNotFoundException e) {
                                            Log.d("SAVEAS", "FileNotFoundException");
                                        } catch (IOException e) {
                                            Log.d("SAVEAS", "IOException");
                                        }

                                        //update widget info file with the new name
                                        File myFile = new File(getFilesDir(), widgetDataFileName);
                                        try {
                                            //get old values
                                            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(myFile));
                                            HashMap<Integer, WidgetData> widgetIdValues = (HashMap<Integer, WidgetData>) inputStream.readObject();
                                            HashMap<Integer, WidgetData> newWidgetIdValues = new HashMap<Integer, WidgetData>(widgetIdValues);
                                            inputStream.close();

                                            //loop through all ids, change file name of any widgets displaying old file to new file.
                                            for (HashMap.Entry<Integer, WidgetData> entry : widgetIdValues.entrySet()) {
                                                int idKey = entry.getKey();
                                                WidgetData oldWidgetData = entry.getValue();
                                                if (oldWidgetData.getFileName().equals(fileName)) {
                                                    String newFileName = newFile.getName();
                                                    String sameFileContents = textEditor.getText().toString();
                                                    int sameBlinkDelay = oldWidgetData.getBlinkDelay();
                                                    String sameBackgroundColor = oldWidgetData.getBackgroundColor();
                                                    WidgetData newWidgetData = new WidgetData(idKey, newFileName, sameFileContents,
                                                            sameBlinkDelay, sameBackgroundColor);
                                                    newWidgetIdValues.put(idKey, newWidgetData);
                                                }
                                            }

                                            //save updated fileName values back into my info file
                                            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(myFile));
                                            outputStream.writeObject(newWidgetIdValues);
                                            outputStream.flush();
                                            outputStream.close();

                                            //tell widgets to update with new name
                                            Intent returnIntent = new Intent(getApplicationContext(), BlinkWidget.class);
                                            returnIntent.setAction(EDIT_FILE_FROM_OUTSIDE_ACTION);
                                            //widget seems to only receive broadcast if there's extras in it
                                            returnIntent.putExtra("fileName", newFile.getName());
                                            sendBroadcast(returnIntent);
                                            fileName = newFile.getName();
                                            originalFileContents = textEditor.getText().toString();
                                            isNewFile = false;
                                            showAsShortToast("Overwritten");
                                        } catch (IOException e) {
                                            Log.d("BlinkWidget", "IOEXCEPTION in onReceive()");
                                        } catch (ClassNotFoundException e) {
                                            Log.d("BlinkWidget", "CLASSNOTEFOUNDEXCEPTION in onReceive()");
                                        }

                                        break;
                                    }
                                    case DialogInterface.BUTTON_NEGATIVE: { //cancel
                                        break;
                                    }
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(TextEditorActivity.this);
                        builder.setMessage("A note named \"" + fileName +
                                "\" already exists.\nOverwrite it?")
                                .setPositiveButton("Overwrite", dialogClickListener)
                                .setNegativeButton("Cancel", dialogClickListener)
                                .show();
                    } else{ //new name not taken. Just save it
                        File oldFile = new File(myDirectory, fileName);
                        oldFile.delete();
                        File newFile = new File(myDirectory, fileName);
                        try {
                            myOutputStream = new FileOutputStream(newFile, false);
                            myOutputStream.write(textEditor.getText().toString().getBytes());
                            myOutputStream.close();
                        } catch (FileNotFoundException e) {
                            Log.d("SAVEAS", "FileNotFoundException");
                        } catch (IOException e) {
                            Log.d("SAVEAS", "IOException");
                        }

                        //update widget info file with the new name
                        File myFile = new File(getFilesDir(), widgetDataFileName);
                        try {
                            //get old values
                            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(myFile));
                            HashMap<Integer, WidgetData> widgetIdValues = (HashMap<Integer, WidgetData>) inputStream.readObject();
                            HashMap<Integer, WidgetData> newWidgetIdValues = new HashMap<Integer, WidgetData>(widgetIdValues);
                            inputStream.close();

                            //loop through all ids, change file name of any widgets displaying old file to new file.
                            for (HashMap.Entry<Integer, WidgetData> entry : widgetIdValues.entrySet()) {
                                int idKey = entry.getKey();
                                WidgetData oldWidgetData = entry.getValue();
                                if (oldWidgetData.getFileName().equals(fileName)) {
                                    String newFileName = newFile.getName();
                                    String sameFileContents = textEditor.getText().toString();
                                    int sameBlinkDelay = oldWidgetData.getBlinkDelay();
                                    String sameBackgroundColor = oldWidgetData.getBackgroundColor();
                                    WidgetData newWidgetData = new WidgetData(idKey, newFileName, sameFileContents,
                                            sameBlinkDelay, sameBackgroundColor);
                                    newWidgetIdValues.put(idKey, newWidgetData);
                                }
                            }

                            //save updated fileName values back into my info file
                            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(myFile));
                            outputStream.writeObject(newWidgetIdValues);
                            outputStream.flush();
                            outputStream.close();

                            //tell widgets to update with new name
                            Intent returnIntent = new Intent(getApplicationContext(), BlinkWidget.class);
                            returnIntent.setAction(EDIT_FILE_FROM_OUTSIDE_ACTION);
                            //widget seems to only receive broadcast if there's extras in it
                            returnIntent.putExtra("fileName", newFile.getName());
                            sendBroadcast(returnIntent);
                            fileName = newFile.getName();
                            originalFileContents = textEditor.getText().toString();
                            isNewFile = false;
                            showAsShortToast("Saved");
                        } catch (IOException e) {
                            Log.d("BlinkWidget", "IOEXCEPTION in onReceive()");
                        } catch (ClassNotFoundException e) {
                            Log.d("BlinkWidget", "CLASSNOTEFOUNDEXCEPTION in onReceive()");
                        }
                    }
                } else{ //else it's an existing file. Just save it
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
            public void onClick(View v){
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
                if(fileName.length() <= 15) {
                    fileNameDisplay.setText(fileName);
                } else{
                    fileNameDisplay.setText(fileName.substring(0, 15) + "...");
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
            //changes were made. ask "Save changes before exiting?" Save Cancel Exit
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
                        case DialogInterface.BUTTON_NEUTRAL: { //Save
                            //fileName should be up-to-date when fileNameDisplay is changed
                            if(isNewFile && fileName.equals("New Note")){
                                //new file with default name, go to save as
                                Intent saveAsIntent = new Intent(getApplicationContext(), SaveAsActivity.class);
                                saveAsIntent.putExtra("fileName", fileName);
                                saveAsIntent.putExtra("fileContents", textEditor.getText().toString());
                                saveAsIntent.putExtra("isNewFile", isNewFile);
                                startActivityForResult(saveAsIntent, NEW_FILE_REQUEST_CODE);
                                break;
                                //on return to Main, goes to the overridden onActivityResult()
                            } else if(isNewFile && !fileName.equals("New Note")) {
                                final File myDirectory = new File(getFilesDir(), notesDirectory);
                                if(!myDirectory.exists()){
                                    myDirectory.mkdirs();
                                }
                                if(fileNameAlreadyExists(fileName)){
                                    //ask to overwrite. Yes, file rename/replacement. No, cancel
                                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE: { //Overwrite
                                                    File oldFile = new File(myDirectory, fileName);
                                                    oldFile.delete();
                                                    File newFile = new File(myDirectory, fileName);
                                                    try {
                                                        myOutputStream = new FileOutputStream(newFile, false);
                                                        myOutputStream.write(textEditor.getText().toString().getBytes());
                                                        myOutputStream.close();
                                                    } catch (FileNotFoundException e) {
                                                        Log.d("SAVEAS", "FileNotFoundException");
                                                    } catch (IOException e) {
                                                        Log.d("SAVEAS", "IOException");
                                                    }

                                                    //update widget info file with the new name
                                                    File myFile = new File(getFilesDir(), widgetDataFileName);
                                                    try {
                                                        //get old values
                                                        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(myFile));
                                                        HashMap<Integer, WidgetData> widgetIdValues = (HashMap<Integer, WidgetData>) inputStream.readObject();
                                                        HashMap<Integer, WidgetData> newWidgetIdValues = new HashMap<Integer, WidgetData>(widgetIdValues);
                                                        inputStream.close();

                                                        //loop through all ids, change file name of any widgets displaying old file to new file.
                                                        for (HashMap.Entry<Integer, WidgetData> entry : widgetIdValues.entrySet()) {
                                                            int idKey = entry.getKey();
                                                            WidgetData oldWidgetData = entry.getValue();
                                                            if (oldWidgetData.getFileName().equals(fileName)) {
                                                                String newFileName = newFile.getName();
                                                                String sameFileContents = textEditor.getText().toString();
                                                                int sameBlinkDelay = oldWidgetData.getBlinkDelay();
                                                                String sameBackgroundColor = oldWidgetData.getBackgroundColor();
                                                                WidgetData newWidgetData = new WidgetData(idKey, newFileName, sameFileContents,
                                                                        sameBlinkDelay, sameBackgroundColor);
                                                                newWidgetIdValues.put(idKey, newWidgetData);
                                                            }
                                                        }

                                                        //save updated fileName values back into my info file
                                                        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(myFile));
                                                        outputStream.writeObject(newWidgetIdValues);
                                                        outputStream.flush();
                                                        outputStream.close();

                                                        //tell widgets to update with new name
                                                        Intent returnIntent = new Intent(getApplicationContext(), BlinkWidget.class);
                                                        returnIntent.setAction(EDIT_FILE_FROM_OUTSIDE_ACTION);
                                                        //widget seems to only receive broadcast if there's extras in it
                                                        returnIntent.putExtra("fileName", newFile.getName());
                                                        sendBroadcast(returnIntent);
                                                        fileName = newFile.getName();
                                                        originalFileContents = textEditor.getText().toString();
                                                        isNewFile = false;
                                                        showAsShortToast("Saved");
                                                        finish();
                                                        //break;
                                                    } catch (IOException e) {
                                                        Log.d("BlinkWidget", "IOEXCEPTION in onReceive()");
                                                    } catch (ClassNotFoundException e) {
                                                        Log.d("BlinkWidget", "CLASSNOTEFOUNDEXCEPTION in onReceive()");
                                                    }
                                                    break;
                                                }
                                                case DialogInterface.BUTTON_NEGATIVE: { //cancel
                                                    break;
                                                }
                                            }
                                        }
                                    };

                                    AlertDialog.Builder builder = new AlertDialog.Builder(TextEditorActivity.this);
                                    builder.setMessage("A note named \"" + fileName +
                                            "\" already exists.\nOverwrite it?")
                                            .setPositiveButton("Overwrite", dialogClickListener)
                                            .setNegativeButton("Cancel", dialogClickListener)
                                            .show();
                                } else{ //new name not taken. Just save it
                                    File oldFile = new File(myDirectory, fileName);
                                    oldFile.delete();
                                    File newFile = new File(myDirectory, fileName);
                                    try {
                                        myOutputStream = new FileOutputStream(newFile, false);
                                        myOutputStream.write(textEditor.getText().toString().getBytes());
                                        myOutputStream.close();
                                    } catch (FileNotFoundException e) {
                                        Log.d("SAVEAS", "FileNotFoundException");
                                    } catch (IOException e) {
                                        Log.d("SAVEAS", "IOException");
                                    }

                                    //update widget info file with the new name
                                    File myFile = new File(getFilesDir(), widgetDataFileName);
                                    try {
                                        //get old values
                                        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(myFile));
                                        HashMap<Integer, WidgetData> widgetIdValues = (HashMap<Integer, WidgetData>) inputStream.readObject();
                                        HashMap<Integer, WidgetData> newWidgetIdValues = new HashMap<Integer, WidgetData>(widgetIdValues);
                                        inputStream.close();

                                        //loop through all ids, change file name of any widgets displaying old file to new file.
                                        for (HashMap.Entry<Integer, WidgetData> entry : widgetIdValues.entrySet()) {
                                            int idKey = entry.getKey();
                                            WidgetData oldWidgetData = entry.getValue();
                                            if (oldWidgetData.getFileName().equals(fileName)) {
                                                String newFileName = newFile.getName();
                                                String sameFileContents = textEditor.getText().toString();
                                                int sameBlinkDelay = oldWidgetData.getBlinkDelay();
                                                String sameBackgroundColor = oldWidgetData.getBackgroundColor();
                                                WidgetData newWidgetData = new WidgetData(idKey, newFileName, sameFileContents,
                                                        sameBlinkDelay, sameBackgroundColor);
                                                newWidgetIdValues.put(idKey, newWidgetData);
                                            }
                                        }

                                        //save updated fileName values back into my info file
                                        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(myFile));
                                        outputStream.writeObject(newWidgetIdValues);
                                        outputStream.flush();
                                        outputStream.close();

                                        //tell widgets to update with new name
                                        Intent returnIntent = new Intent(getApplicationContext(), BlinkWidget.class);
                                        returnIntent.setAction(EDIT_FILE_FROM_OUTSIDE_ACTION);
                                        //widget seems to only receive broadcast if there's extras in it
                                        returnIntent.putExtra("fileName", newFile.getName());
                                        sendBroadcast(returnIntent);
                                        fileName = newFile.getName();
                                        originalFileContents = textEditor.getText().toString();
                                        isNewFile = false;
                                        showAsShortToast("Saved");
                                        finish();
                                    } catch (IOException e) {
                                        Log.d("BlinkWidget", "IOEXCEPTION in onReceive()");
                                    } catch (ClassNotFoundException e) {
                                        Log.d("BlinkWidget", "CLASSNOTEFOUNDEXCEPTION in onReceive()");
                                    }
                                }
                            } else{ //else it's an existing file. Just save it
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

                                //trigger widget updates
                                Intent returnIntent = new Intent(getApplicationContext(), BlinkWidget.class);
                                returnIntent.setAction(EDIT_FILE_FROM_OUTSIDE_ACTION);
                                //widget seems to only receive broadcast if there's extras in it
                                returnIntent.putExtra("fileName", fileName);
                                returnIntent.putExtra("fileContents", originalFileContents);
                                sendBroadcast(returnIntent);

                                showAsShortToast("Saved");
                                finish();
                            }
                        }
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(TextEditorActivity.this);
            builder.setMessage("Save changes before exiting?")
                    .setPositiveButton("Exit", dialogClickListener)
                    .setNegativeButton("Cancel", dialogClickListener)
                    .setNeutralButton("Save", dialogClickListener)
                    .show();
        }
    }

    public boolean fileNameAlreadyExists(String userInputFileName){
        File myDirectory = new File(getFilesDir(), notesDirectory);
        if(!myDirectory.exists()){
            myDirectory.mkdirs();
        }
        return (new File(myDirectory, userInputFileName)).exists();
    }

    public void showAsShortToast(String text){
        CharSequence toastText = text;
        int duration = Toast.LENGTH_SHORT; //2 seconds
        Toast toast = Toast.makeText(getApplicationContext(), toastText, duration);
        toast.show();
    }
}
