package com.cl.emphasize;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/*
    This app allows you to post your notes to your home screen, and emphasize them
    by making them glow and jiggle. You can also save your notes as actual text files.
    The editor's save features are modeled after Microsoft Word's Save and Save As.
 */

public class MainActivity extends AppCompatActivity {

    protected ListView listView;
    protected ArrayAdapter<File> listViewAdapter;
    protected ArrayList<File> myFileArray;
    protected File workingDir;
    protected TextView textPrint = null;
    //private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    @TargetApi(21)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textPrint = (TextView) findViewById(R.id.textPrint);
        Button mainNewTextFileButton = (Button)findViewById(R.id.mainNewTextFileButton);
        Button mainSettingsButton = (Button)findViewById(R.id.mainSettingsButton);

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

        if(fileListOnCreate.length == 0){
            textPrint.setText("No files found");
        } else{
            textPrint.setText("");
        }

        //Press new
        mainNewTextFileButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent newFileIntent = new Intent(getApplicationContext(), TextEditorActivity.class);
                newFileIntent.putExtra("fileName", "NewFile.txt");
                newFileIntent.putExtra("fileContents", "");
                newFileIntent.putExtra("isNewFile", true);
                startActivity(newFileIntent);
            }
        });

        //Press settings
        mainSettingsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent newFileIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(newFileIntent);
            }
        });

        //Press or hold ListView object
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
                File fileClicked = myFileArray.get(position);
                String fileContents = "";
                Intent newFileIntent = new Intent(getApplicationContext(), TextEditorActivity.class);
                try {
                    BufferedReader fileReader = new BufferedReader(new FileReader(fileClicked));
                    String line;
                    while((line = fileReader.readLine()) != null) {
                        fileContents += line;
                    }
                } catch(IOException e){
                    Log.d("MAIN", "IOException");
                }

                newFileIntent.putExtra("fileName", fileClicked.getName());
                newFileIntent.putExtra("fileContents", fileContents);
                newFileIntent.putExtra("isNewFile", false);
                startActivity(newFileIntent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l){
                CharSequence options[] = new CharSequence[] {"Edit", "Delete", "Post on home screen"};
                final File longClickedFile = myFileArray.get(position);
                final String optionsFileName = longClickedFile.getName();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(optionsFileName);
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int clickedOption) {
                        switch(clickedOption){
                            case 0: { //Edit: open in TextEditorActivity
                                String fileContents = "";
                                Intent newFileIntent = new Intent(getApplicationContext(), TextEditorActivity.class);
                                try {
                                    BufferedReader fileReader = new BufferedReader(new FileReader(longClickedFile));
                                    String line;
                                    while((line = fileReader.readLine()) != null) {
                                        fileContents += line;
                                    }
                                } catch(IOException e){
                                    Log.d("MAIN", "IOException");
                                }

                                newFileIntent.putExtra("fileName", longClickedFile.getName());
                                newFileIntent.putExtra("fileContents", fileContents);
                                newFileIntent.putExtra("isNewFile", false);
                                startActivity(newFileIntent);
                                break;
                            }
                            case 1: { //Delete
                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){
                                            case DialogInterface.BUTTON_POSITIVE: {
                                                File oldFile = new File(getFilesDir(), optionsFileName);
                                                oldFile.delete();
                                                updateListView();
                                                break;
                                            }
                                            case DialogInterface.BUTTON_NEGATIVE: {
                                                break;
                                            }
                                        }
                                    }
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("Delete " + optionsFileName + "?")
                                        .setPositiveButton("Delete", dialogClickListener)
                                        .setNegativeButton("Cancel", dialogClickListener)
                                        .show();
                                break;
                            }
                            case 2: { //Post on home screen
                            }
                        }
                    }
                });
                builder.show();
                return true; //prevents onItemClick() from also firing
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

        if(fileListUpdateListView.length == 0){
            textPrint.setText("No files found");
        } else{
            textPrint.setText("");
        }
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