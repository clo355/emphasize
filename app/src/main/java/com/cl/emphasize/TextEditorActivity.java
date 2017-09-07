package com.cl.emphasize;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TextEditorActivity extends AppCompatActivity {

    String fileName;
    boolean isNewFile;
    boolean continueEditing = true;
    boolean madeChanges = false;
    public static final int NEW_FILE_REQUEST_CODE = 1;

    TextView textPrint;
    EditText textEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);

        textPrint = (TextView)findViewById(R.id.textPrint);
        textEditor = (EditText)findViewById(R.id.textEditor);

        fileName = getIntent().getExtras().getString("fileName");
        isNewFile = getIntent().getExtras().getBoolean("isNewFile");
        textEditor.setText(getIntent().getExtras().getString("fileContents"));
        continueEditing = true;

        textPrint.append("fileName=" + fileName + "\n");
        textPrint.append("isNewFile=" + Boolean.toString(isNewFile) + "\n");
        textPrint.append("path=" + getFilesDir().toString());

        Button saveButton = (Button)findViewById(R.id.textEditorSaveButton);
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(isNewFile){
                    Intent saveAsIntent = new Intent(getApplicationContext(), SaveAsActivity.class);
                    saveAsIntent.putExtra("isNewFile", isNewFile);
                    saveAsIntent.putExtra("fileName", fileName);
                    saveAsIntent.putExtra("fileContents", textEditor.getText().toString());
                    startActivityForResult(saveAsIntent, NEW_FILE_REQUEST_CODE);
                    //on return, calls the overrided onActivityResult()
                } else{
                    /*overwrite file with given fileName*/
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent){
        if(requestCode == NEW_FILE_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                //user pressed ok and saved file. incoming isNewFile should be false
                //also update file name
                isNewFile = returnedIntent.getExtras().getBoolean("isNewFile");
            }
            if(resultCode == Activity.RESULT_CANCELED){
                //user pressed cancel in SaveAsActivity
            }
        }
    }

    @Override
    public void onBackPressed(){
        //if file was changed, ask "Save changes? Yes No". If yes, go to SaveAsActivity
        finish();
    }
}
