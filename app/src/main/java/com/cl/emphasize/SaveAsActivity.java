package com.cl.emphasize;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveAsActivity extends AppCompatActivity {
    protected String fileContents;
    protected String fileName;
    protected Button saveAsOkButton;
    protected Button saveAsCancelButton;
    protected EditText saveAsFileName;
    protected FileOutputStream myOutputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_as);

        fileContents = getIntent().getExtras().getString("fileContents");
        fileName = getIntent().getExtras().getString("fileName");

        saveAsOkButton = (Button)findViewById(R.id.saveAsOkButton);
        saveAsCancelButton = (Button)findViewById(R.id.saveAsCancelButton);
        saveAsFileName = (EditText)findViewById(R.id.saveAsFileName);
        saveAsFileName.setText(fileName);

        saveAsOkButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                final String userInputFileName = saveAsFileName.getText().toString();
                if(fileNameAlreadyExists(userInputFileName)){
                    //file name is already taken. Ask to overwrite
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:{ //exit
                                    File oldFile = new File(getFilesDir(), userInputFileName);
                                    oldFile.delete();
                                    File newFile = new File(getFilesDir(), userInputFileName);
                                    try{
                                        myOutputStream = new FileOutputStream(newFile, false);
                                        myOutputStream.write(fileContents.getBytes());
                                        myOutputStream.close();
                                    } catch(FileNotFoundException e){
                                        Log.d("SAVEAS", "FileNotFoundException");
                                    } catch(IOException e) {
                                        Log.d("SAVEAS", "IOException");
                                    }

                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("isNewFile", false);
                                    returnIntent.putExtra("fileName", userInputFileName);
                                    setResult(Activity.RESULT_OK, returnIntent);
                                    finish();
                                    break;
                                }
                                case DialogInterface.BUTTON_NEGATIVE:{ //cancel
                                    Intent returnIntent = new Intent();
                                    setResult(Activity.RESULT_CANCELED, returnIntent);
                                    finish();
                                    break;
                                }
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(SaveAsActivity.this);
                    builder.setMessage("A file named \"" + userInputFileName +
                            "\" already exists.\nOverwrite it?")
                            .setPositiveButton("Overwrite", dialogClickListener)
                            .setNegativeButton("Cancel", dialogClickListener)
                            .show();
                } else{
                    //file name not taken. Now save it
                    File oldFile = new File(getFilesDir(), userInputFileName);
                    oldFile.delete();
                    File newFile = new File(getFilesDir(), userInputFileName);
                    try{
                        myOutputStream = new FileOutputStream(newFile, false);
                        myOutputStream.write(fileContents.getBytes());
                        myOutputStream.close();
                    } catch(FileNotFoundException e){
                        Log.d("SAVEAS", "FileNotFoundException");
                    } catch(IOException e) {
                        Log.d("SAVEAS", "IOException");
                    }

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("isNewFile", false);
                    returnIntent.putExtra("fileName", userInputFileName);
                    returnIntent.putExtra("changesSaved", true);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
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
        return (new File(getFilesDir(), userInputFileName)).exists();
    }

    @Override
    public void onBackPressed(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
}