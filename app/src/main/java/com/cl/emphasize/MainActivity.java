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

/**********************************************************************************
 *   Emphasize
 *
 *   This app allows you post notes onto your home screen, and emphasize them
 *   by having them flash or jiggle. Your notes are saved as actual
 *   text files.
 *
 *   Inspired by Colornote!
 *   CL
 **********************************************************************************/

public class MainActivity extends AppCompatActivity {

    protected ListView listView;
    protected ArrayAdapter<String> listViewAdapter;
    protected ArrayList<String> myFileNameArray;
    protected TextView textPrint = null;

    @TargetApi(21)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textPrint = (TextView) findViewById(R.id.textPrint);
        Button mainNewTextFileButton = (Button)findViewById(R.id.mainNewTextFileButton);
        Button mainSettingsButton = (Button)findViewById(R.id.mainSettingsButton);

        //Initially populate ListView
        myFileNameArray = new ArrayList<String>();
        File[] fileListOnCreate = getFilesDir().listFiles();
        for(File foundFile : fileListOnCreate){
            String foundFileName = foundFile.getName();
            myFileNameArray.add(foundFileName);
        }

        listView = (ListView) findViewById(R.id.listView);
        listViewAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                new ArrayList(myFileNameArray));
        //myFileNameArray will be deleted by clear(), if passed by reference! So, used new ArrayList().
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

        //Press ListView object
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
                File fileClicked = new File(getFilesDir(), myFileNameArray.get(position));
                String fileContents = "";
                Intent newFileIntent = new Intent(getApplicationContext(), TextEditorActivity.class);
                try {
                    BufferedReader fileReader = new BufferedReader(new FileReader(fileClicked));
                    String line;
                    while((line = fileReader.readLine()) != null) {
                        fileContents = fileContents + line + "\n";
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

        //Hold ListView object
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l){
                CharSequence options[] = new CharSequence[] {"Edit", "Delete", "Properties"};
                final File longClickedFile = new File(getFilesDir(), myFileNameArray.get(position));
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
                                        fileContents = fileContents + line + "\n";
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
                            case 2: { //Properties: show create date, last edit date, file size
                            }
                        }
                    }
                });
                builder.show();
                return true; //prevents onItemClick() from also firing after doing onItemLongClick()
            }
        });
    }

    public void updateListView(){
        myFileNameArray.clear();
        File[] fileListUpdateListView = getFilesDir().listFiles();
        for(File foundFile : fileListUpdateListView){
            String foundFileName = foundFile.getName();
            myFileNameArray.add(foundFileName);
        }
        listView.setAdapter(null);
        listViewAdapter.clear();
        listViewAdapter.addAll(new ArrayList(myFileNameArray));
        //myFileNameArray will be deleted by clear(), if passed by reference! So, use new ArrayList()
        listViewAdapter.notifyDataSetChanged(); //update ListView to show changes
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
}