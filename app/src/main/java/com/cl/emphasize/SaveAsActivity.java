package com.cl.emphasize;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
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

    boolean isNewFile;
    String fileContents;
    String fileName;
    Button saveAsOkButton;
    Button saveAsCancelButton;
    EditText saveAsFileName;
    File myFile;
    FileOutputStream myOutputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_as);

        isNewFile = getIntent().getExtras().getBoolean("isNewFile");
        fileContents = getIntent().getExtras().getString("fileContents");
        fileName = getIntent().getExtras().getString("fileName");

        saveAsOkButton = (Button)findViewById(R.id.saveAsOkButton);
        saveAsCancelButton = (Button)findViewById(R.id.saveAsCancelButton);
        saveAsFileName = (EditText)findViewById(R.id.saveAsFileName);
        saveAsFileName.setText(fileName);

        saveAsOkButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String userInputFileName = saveAsFileName.getText().toString();
                if(fileNameAlreadyExists(userInputFileName)){
                    //already exists. Overwrite? Yes no
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("isNewFile", false);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } else{
                    //file name not taken. Now save it
                    myFile = new File(getFilesDir(), userInputFileName);

                    try{
                        myOutputStream = new FileOutputStream(myFile);
                        myOutputStream.write(fileContents.getBytes());
                        myOutputStream.close();
                    } catch(FileNotFoundException e){
                        Log.d("SAVEAS", "FileNotFoundException");
                    } catch(IOException e) {
                        Log.d("SAVEAS", "IOException");
                    }

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("isNewFile", false);
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
}
