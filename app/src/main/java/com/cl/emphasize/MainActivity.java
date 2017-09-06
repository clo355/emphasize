package com.cl.emphasize;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/*
    This app allows you to emphasize your note widgets by making them glow and jiggle.
    You can also save your notes as actual text files.
    The editor's save feature is modeled after Microsoft Word.
 */

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    //Test output
    TextView textPrint = null;

    @TargetApi(21)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textPrint = (TextView) findViewById(R.id.textPrint);
        Button newTextFile = (Button)findViewById(R.id.mainNewTextFileButton);

        //App permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        //ListView objects
        ArrayList<Integer> myInts = new ArrayList<Integer>();
        for (int i = 0; i <= 19; i++) {
            myInts.add(i);
        }

        ListView listView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<Integer> listViewAdapter = new ArrayAdapter<Integer>(
                this,
                android.R.layout.simple_list_item_1,
                myInts);
        listView.setAdapter(listViewAdapter);

        newTextFile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent newFileIntent = new Intent(getApplicationContext(), TextEditorActivity.class);
                String fileName = "New File";
                boolean isNewFile = true;
                newFileIntent.putExtra("fileName", fileName);
                newFileIntent.putExtra("isNewFile", isNewFile);
                startActivity(newFileIntent);
            }
        });

        /*
        for(Integer i : myInts) {
            textPrint.append(i.toString());
        }
        */
    }

    //App permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch(requestCode){
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:{
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        textPrint.append("***WRITE_EXTERNAL_STORAGE granted\n");
                } else {
                    // permission denied. display ok box warning "This app won't be able
                    // to save text files unless the permission is granted." loop ask again
                    textPrint.append("***WRITE_EXTERNAL_STORAGE denied\n");
                }
            }
            default: {
                return;
            }
        }
    }
}