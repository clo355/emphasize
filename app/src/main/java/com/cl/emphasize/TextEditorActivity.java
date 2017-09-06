package com.cl.emphasize;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TextEditorActivity extends AppCompatActivity {

    String fileName;
    boolean isNewFile;
    boolean continueEditing;
    boolean madeChanges;
    public static final int NEW_FILE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);

        TextView textPrint = (TextView)findViewById(R.id.textPrint);
        EditText textEditor = (EditText)findViewById(R.id.textEditor);

        fileName = getIntent().getExtras().getString("fileName");
        isNewFile = getIntent().getExtras().getBoolean("isNewFile");
        continueEditing = true;

        textPrint.append("fileName=" + fileName + "\n");
        textPrint.append("isNewFile=" + Boolean.toString(isNewFile));

        Button saveButton = (Button)findViewById(R.id.textEditorSaveButton);
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(isNewFile){
                    Intent saveAsIntent = new Intent(getApplicationContext(), SaveAsActivity.class);
                    saveAsIntent.putExtra("isNewFile", isNewFile);
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
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                isNewFile = returnedIntent.getExtras().getBoolean("isNewFile");
                //user saved file. isNewFile should be false
            }
            if(resultCode == Activity.RESULT_CANCELED){
                //user pressed cancel in SaveAsActivity
            }
        }
    }

    @Override
    public void onBackPressed(){
        //if file was changed, ask "Save changes? Yes No"
        finish();
    }
}
