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

import java.io.File;
import java.util.ArrayList;

/*
    This app allows you to post your notes to your home screen, and emphasize them
    by making them glow and jiggle. You can also save your notes as actual text files.
    The editor's save features are modeled after Microsoft Word's Save and Save As.
 */

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<File> listViewAdapter;
    ArrayList<File> myFileArray;
    File workingDir;
    //private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    //Test output
    TextView textPrint = null;

    @TargetApi(21)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textPrint = (TextView) findViewById(R.id.textPrint);
        Button mainNewTextFileButton = (Button)findViewById(R.id.mainNewTextFileButton);

        //App permissions
        /*
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        */

        //Initially populate ListView
        myFileArray = new ArrayList<File>();
        workingDir = new File(getFilesDir().toString());
        File[] fileListOnCreate = workingDir.listFiles();
        for(File foundFile : fileListOnCreate){
            myFileArray.add(foundFile);
        }
        listView = (ListView) findViewById(R.id.listView);
        listViewAdapter = new ArrayAdapter<File>(
                this,
                android.R.layout.simple_list_item_1,
                myFileArray);
        listView.setAdapter(listViewAdapter);

        //Buttons
        mainNewTextFileButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent newFileIntent = new Intent(getApplicationContext(), TextEditorActivity.class);
                String fileName = "NewFile.txt";
                boolean isNewFile = true;
                newFileIntent.putExtra("fileName", fileName);
                newFileIntent.putExtra("isNewFile", isNewFile);
                startActivity(newFileIntent);
            }
        });
    }

    public void updateListView(){
        myFileArray.clear();
        File[] fileListUpdateListView = workingDir.listFiles();
        for(File foundFile : fileListUpdateListView){
            myFileArray.add(foundFile);
        }
        listView.setAdapter(null);
        listViewAdapter.clear();
        listViewAdapter.addAll(fileListUpdateListView);
        listViewAdapter.notifyDataSetChanged(); //updates the view
        listView.setAdapter(listViewAdapter);
    }

    @Override
    public void onResume(){
        super.onResume();
        updateListView();
    }

    //App permissions
    /*
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
    */
}