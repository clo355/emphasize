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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 * @author Chris Lo
 */

public class SaveAsActivity extends AppCompatActivity {
    protected String fileContents;
    protected String fileName;
    protected boolean isNewFile;
    protected boolean isRenamedNewFile = false;
    protected Button saveAsOkButton;
    protected Button saveAsCancelButton;
    protected EditText saveAsFileName;
    protected FileOutputStream myOutputStream;
    public static String EDIT_FILE_FROM_OUTSIDE_ACTION = "ActionEditFileFromOutside";
    public static final String PREFS_NAME = "PreferenceFile";
    public static final String widgetDataFileName = "widget_data.dat";
    public static final String notesDirectory = "notes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int globalTheme = settings.getInt("globalTheme", R.style.lightTheme);
        setTheme(globalTheme);

        setContentView(R.layout.activity_save_as);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        fileContents = getIntent().getExtras().getString("fileContents");
        fileName = getIntent().getExtras().getString("fileName");
        isNewFile = getIntent().getExtras().getBoolean("isNewFile");
        final boolean exitAfterSaveAs = getIntent().getExtras().getBoolean("exitAfterSaveAs", false);

        saveAsOkButton = (Button)findViewById(R.id.saveAsOkButton);
        saveAsCancelButton = (Button)findViewById(R.id.saveAsCancelButton);
        saveAsFileName = (EditText)findViewById(R.id.saveAsFileName);
        saveAsFileName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        //Existing file "New Note", keep hint. New "New Note", search for available "New Note #"
        String freeFileName = "";
        if(isNewFile && !fileName.equals("New Note")){
            isRenamedNewFile = true;
            saveAsFileName.setHint(fileName);
        } else if(isNewFile && fileName.equals("New Note")){
            String newFilePrefix = "New Note";
            if(fileNameAlreadyExists(newFilePrefix)){ //"New Note" already taken
                int newFileSuffix = 1;
                while(fileNameAlreadyExists(newFilePrefix + " " + newFileSuffix)){
                    newFileSuffix++;
                }
                freeFileName = newFilePrefix + " " + newFileSuffix;
                saveAsFileName.setHint(freeFileName);
            } else{ //"Note" not taken
                freeFileName = "New Note";
                saveAsFileName.setHint(freeFileName);
            }
        } else{
            saveAsFileName.setHint(fileName);
        }

        //Automatically show keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        final String defaultFileName = freeFileName;
        saveAsOkButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                final String userInputFileName = saveAsFileName.getText().toString();
                if(userInputFileName.equals("") && !isRenamedNewFile){ //user left file name empty, name as "New Note #"
                    final File myDirectory = new File(getFilesDir(), notesDirectory);
                    if (!myDirectory.exists()) {
                        myDirectory.mkdirs();
                    }
                    if(isNewFile){ //Use free default name
                        File newDefaultFile = new File(myDirectory, defaultFileName);
                        try {
                            myOutputStream = new FileOutputStream(newDefaultFile, false);
                            myOutputStream.write(fileContents.getBytes());
                            myOutputStream.close();
                        } catch (FileNotFoundException e) {
                            Log.d("SAVEAS", "FileNotFoundException");
                        } catch (IOException e) {
                            Log.d("SAVEAS", "IOException");
                        }
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("isNewFile", false);
                        returnIntent.putExtra("fileName", defaultFileName);
                        returnIntent.putExtra("changesSaved", true);
                        returnIntent.putExtra("exitAfterSaveAs", exitAfterSaveAs);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    } else { //save as same given/same fileName
                        File newDefaultFile = new File(myDirectory, fileName);
                        try {
                            myOutputStream = new FileOutputStream(newDefaultFile, false);
                            myOutputStream.write(fileContents.getBytes());
                            myOutputStream.close();
                        } catch (FileNotFoundException e) {
                            Log.d("SAVEAS", "FileNotFoundException");
                        } catch (IOException e) {
                            Log.d("SAVEAS", "IOException");
                        }

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("isNewFile", false);
                        returnIntent.putExtra("fileName", fileName);
                        returnIntent.putExtra("changesSaved", true);
                        returnIntent.putExtra("exitAfterSaveAs", exitAfterSaveAs);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                } else{ //user wrote a file name, or it's a renamed new file from TextEditor

                    final File myDirectory = new File(getFilesDir(), notesDirectory);
                    if(!myDirectory.exists()){
                        myDirectory.mkdirs();
                    }

                    final String wroteOrRenamedName;
                    if(isRenamedNewFile){
                        wroteOrRenamedName = fileName;
                    } else{
                        wroteOrRenamedName = userInputFileName;
                    }
                    if(saveAsFileName.getText().toString().equals(".") ||
                            saveAsFileName.getText().toString().equals("..")){
                        showAsShortToast("Notes can't be named . or ..");
                        saveAsFileName.setText("");
                    } else if(containsSlashes(saveAsFileName.getText().toString())){
                        showAsShortToast("Name can't contain slashes");
                        saveAsFileName.setText("");
                    } else if(fileNameAlreadyExists(wroteOrRenamedName)){ //isRenamedFile, name already taken?
                        if (wroteOrRenamedName.equals(fileName)) { //user trying to save as same name
                            File oldFile = new File(myDirectory, fileName); //just overwrite/create it.
                            oldFile.delete();
                            File newFile = new File(myDirectory, fileName);
                            try {
                                myOutputStream = new FileOutputStream(newFile, false);
                                myOutputStream.write(fileContents.getBytes());
                                myOutputStream.close();
                            } catch (FileNotFoundException e) {
                                Log.d("SAVEAS", "FileNotFoundException");
                            } catch (IOException e) {
                                Log.d("SAVEAS", "IOException");
                            }

                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("isNewFile", false);
                            returnIntent.putExtra("fileName", fileName);
                            returnIntent.putExtra("changesSaved", true);
                            returnIntent.putExtra("exitAfterSaveAs", exitAfterSaveAs);
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        } else { //user trying to save file as another name that already exists
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE: { //Overwrite
                                            File oldFile = new File(myDirectory, wroteOrRenamedName);
                                            oldFile.delete();
                                            File newFile = new File(myDirectory, wroteOrRenamedName);
                                            try {
                                                myOutputStream = new FileOutputStream(newFile, false);
                                                myOutputStream.write(fileContents.getBytes());
                                                myOutputStream.close();
                                            } catch (FileNotFoundException e) {
                                                Log.d("SAVEAS", "FileNotFoundException");
                                            } catch (IOException e) {
                                                Log.d("SAVEAS", "IOException");
                                            }

                                            Intent returnIntent = new Intent();
                                            returnIntent.putExtra("isNewFile", false);
                                            returnIntent.putExtra("fileName", wroteOrRenamedName);
                                            returnIntent.putExtra("changesSaved", true);
                                            returnIntent.putExtra("exitAfterSaveAs", exitAfterSaveAs);
                                            setResult(Activity.RESULT_OK, returnIntent);
                                            finish();
                                            break;
                                        }
                                        case DialogInterface.BUTTON_NEGATIVE: { //cancel
                                            Intent returnIntent = new Intent();
                                            setResult(Activity.RESULT_CANCELED, returnIntent);
                                            finish();
                                            break;
                                        }
                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(SaveAsActivity.this);
                            builder.setMessage("A note named \"" + wroteOrRenamedName +
                                    "\" already exists.\nOverwrite it?")
                                    .setPositiveButton("Overwrite", dialogClickListener)
                                    .setNegativeButton("Cancel", dialogClickListener)
                                    .show();
                        }
                    } else{ //input file name not taken.
                        //Or isRenamedFile, and not taken. Now save it.
                        File oldFile = new File(myDirectory, wroteOrRenamedName);
                        oldFile.delete();
                        File newFile = new File(myDirectory, wroteOrRenamedName);
                        try {
                            myOutputStream = new FileOutputStream(newFile, false);
                            myOutputStream.write(fileContents.getBytes());
                            myOutputStream.close();
                        } catch (FileNotFoundException e) {
                            Log.d("SAVEAS", "FileNotFoundException");
                        } catch (IOException e) {
                            Log.d("SAVEAS", "IOException");
                        }

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("isNewFile", false);
                        returnIntent.putExtra("fileName", wroteOrRenamedName);
                        returnIntent.putExtra("changesSaved", true);
                        returnIntent.putExtra("exitAfterSaveAs", exitAfterSaveAs);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                }
            }
        });

        saveAsCancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });
    }

    public boolean fileNameAlreadyExists(String userInputFileName){
        File myDirectory = new File(getFilesDir(), notesDirectory);
        if(!myDirectory.exists()){
            myDirectory.mkdirs();
        }
        return (new File(myDirectory, userInputFileName)).exists();
    }

    public boolean containsSlashes(String myString){
        for(int i = 0; i < myString.length(); i++){
            if((myString.charAt(i) == '/') ||
                    (myString.charAt(i) == '\\')){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    public void showAsShortToast(String text){
        CharSequence toastText = text;
        int duration = Toast.LENGTH_SHORT; //2 seconds
        Toast toast = Toast.makeText(getApplicationContext(), toastText, duration);
        toast.show();
    }

    public void updateWidgetFileFromAtoB(String fileA, String fileB, String newContents){
        //In the widget file, for every id displaying fileA, change it to display fileB
        //with newContents. Blink rate and BgColor are kept the same.
        //This method is only needed for existing files that get saveAs'd,
        //as only existing files can be displayed in a widget.
        File myFile = new File(getFilesDir(), widgetDataFileName);
        try {
            //get old values
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(myFile));
            HashMap<Integer, WidgetData> widgetIdValues = (HashMap<Integer, WidgetData>) inputStream.readObject();
            HashMap<Integer, WidgetData> newWidgetIdValues = new HashMap<Integer, WidgetData>(widgetIdValues);
            inputStream.close();

            //loop through all ids, change file name of any widgets displaying A to B.
            for (HashMap.Entry<Integer, WidgetData> entry : widgetIdValues.entrySet()) {
                int idKey = entry.getKey();
                WidgetData oldWidgetData = entry.getValue();
                if (oldWidgetData.getFileName().equals(fileA)) {
                    String newFileName = fileB;
                    String updatedFileContents = newContents;
                    int sameBlinkDelay = oldWidgetData.getBlinkDelay();
                    String sameBackgroundColor = oldWidgetData.getBackgroundColor();
                    WidgetData newWidgetData = new WidgetData(idKey, newFileName, updatedFileContents,
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
            returnIntent.putExtra("fileName", fileB);
            sendBroadcast(returnIntent);
        } catch (IOException e) {
            Log.d("BlinkWidget", "IOEXCEPTION in onReceive()");
        } catch (ClassNotFoundException e) {
            Log.d("BlinkWidget", "CLASSNOTEFOUNDEXCEPTION in onReceive()");
        }
    }
}
