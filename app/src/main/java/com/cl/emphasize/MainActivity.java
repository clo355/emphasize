package com.cl.emphasize;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**********************************************************************************
 *   Emphasize
 *
 *   This app allows you post blinking notes to your home screen.
 *
 *   CL
 **********************************************************************************/

public class MainActivity extends AppCompatActivity {


    protected ListView listView;
    protected ArrayAdapter<String> listViewAdapter;
    protected ArrayList<String> myFileNameArray;
    protected TextView textPrint = null;
    public static final String PREFS_NAME = "PreferenceFile"; //for sort preference

    @TargetApi(21)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        textPrint = (TextView) findViewById(R.id.textPrint);
        Button mainSortButton = (Button)findViewById(R.id.mainSortButton);
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

        //Press sort
        mainSortButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Sort by:");
                CharSequence[] sortChoices = {"Alphanumeric 0-9, A-Z", "Alphanumeric Z-A, 9-0",
                        "Newest", "Oldest"};
                final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                int savedSort = settings.getInt("savedSort", 3);
                builder.setSingleChoiceItems(sortChoices, savedSort, new DialogInterface.OnClickListener(){
                    Handler myHandler = new Handler();
                    @Override
                    public void onClick(final DialogInterface dialog, int whichRadio){
                        switch(whichRadio){
                            case 0:{ //Alphanumeric 0-9, A-Z
                                myHandler.postDelayed(new Runnable() {
                                    public void run(){
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putInt("savedSort", 0);
                                        editor.commit();
                                        updateListView();
                                        dialog.cancel();
                                    }
                                }, 200);
                                break;
                            }
                            case 1:{ //Alphanumeric Z-A, 9-0
                                myHandler.postDelayed(new Runnable() {
                                    public void run(){
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putInt("savedSort", 1);
                                        editor.commit();
                                        updateListView();
                                        dialog.cancel();
                                    }
                                }, 200);
                                break;
                            }
                            case 2:{ //Newest
                                myHandler.postDelayed(new Runnable() {
                                    public void run(){
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putInt("savedSort", 2);
                                        editor.commit();
                                        updateListView();
                                        dialog.cancel();
                                    }
                                }, 200);
                                break;
                            }
                            case 3:{ //Oldest
                                myHandler.postDelayed(new Runnable() {
                                    public void run(){
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putInt("savedSort", 3);
                                        editor.commit();
                                        updateListView();
                                        dialog.cancel();
                                    }
                                }, 200);
                                break;
                            }
                        }
                    }
                });
                builder.create().show();
            }
        });

        //Press new
        mainNewTextFileButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent newFileIntent = new Intent(getApplicationContext(), TextEditorActivity.class);
                newFileIntent.putExtra("fileName", "New File");
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
                CharSequence options[] = new CharSequence[] {"Edit", "Rename", "Delete", "Properties"};
                final File longClickedFile = new File(getFilesDir(), myFileNameArray.get(position));
                final String longClickedFileName = longClickedFile.getName();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(longClickedFileName);
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
                            case 1: { //Rename
                                final EditText renameEditText = new EditText(MainActivity.this);
                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialog, int which){
                                        //Create an EditText for user input
                                        LinearLayout.LayoutParams renameLayout = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.MATCH_PARENT);
                                        renameEditText.setLayoutParams(renameLayout);

                                        switch (which){
                                            case DialogInterface.BUTTON_POSITIVE:{ //OK
                                                //also need to check if file exists or not

                                                //Get contents of old file
                                                String oldFileContents = "";
                                                try {
                                                    BufferedReader fileReader = new BufferedReader(new FileReader(longClickedFile));
                                                    String line;
                                                    while((line = fileReader.readLine()) != null) {
                                                        oldFileContents = oldFileContents + line + "\n";
                                                    }
                                                } catch(IOException e){
                                                    Log.d("MAIN", "IOException");
                                                }

                                                //fill new file with old file's contents
                                                FileOutputStream myOutputStream;
                                                File newFile = new File(getFilesDir(), renameEditText.getText().toString());
                                                try{
                                                    myOutputStream = new FileOutputStream(newFile, false);
                                                    myOutputStream.write(oldFileContents.getBytes());
                                                    myOutputStream.close();
                                                } catch(FileNotFoundException e){
                                                    Log.d("SAVEAS", "FileNotFoundException");
                                                } catch(IOException e) {
                                                    Log.d("SAVEAS", "IOException");
                                                }
                                                File oldFile = new File(getFilesDir(), longClickedFileName);
                                                oldFile.delete();
                                                updateListView();
                                                showAsShortToast("Renamed as " + newFile.getName());
                                                break;
                                            }
                                            case DialogInterface.BUTTON_NEGATIVE:{ //Cancel
                                                break;
                                            }
                                        }
                                    }
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("Rename file:")
                                        .setView(renameEditText)
                                        .setPositiveButton("OK", dialogClickListener)
                                        .setNegativeButton("Cancel", dialogClickListener)
                                        .show();
                                break;
                            }
                            case 2: { //Delete
                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){
                                            case DialogInterface.BUTTON_POSITIVE: {
                                                File fileToDelete = new File(getFilesDir(), longClickedFileName);
                                                fileToDelete.delete();
                                                updateListView();
                                                showAsShortToast(fileToDelete.getName() + " deleted");
                                                break;
                                            }
                                            case DialogInterface.BUTTON_NEGATIVE: {
                                                break;
                                            }
                                        }
                                    }
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("Delete " + longClickedFileName + "?")
                                        .setPositiveButton("Delete", dialogClickListener)
                                        .setNegativeButton("Cancel", dialogClickListener)
                                        .show();
                                break;
                            }
                            case 3: { //Properties: show create date, last edit date, file byte size
                                Date modDate = new Date(longClickedFile.lastModified());
                                String lastEditDate = modDate.toString();
                                long fileByteSize = longClickedFile.length();
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Properties");
                                builder.setMessage("\n• File name:\n     " + longClickedFileName + "\n\n" +
                                "• Last modification:\n     " + lastEditDate + "\n\n" +
                                "• File size:\n     " + fileByteSize + " bytes\n");
                                builder.setCancelable(true);
                                builder.setPositiveButton("Close", new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                                AlertDialog alert = builder.create();
                                builder.show();
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

        //Sort arraylist with user preference before putting into adapter
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int sortPreference = settings.getInt("savedSort", 2);
        /* 0 is Alphanumeric 0-9, A-Z
         * 1 is Alphanumeric Z-A, 9-0
         * 2 is Newest
         * 3 is Oldest
         */
        if((sortPreference == 0) || (sortPreference == 1)){
            //pass myFileNameArray by reference. Will be sorted by method
            sortByAlphabetical(myFileNameArray, sortPreference);
        } else{
            //create an arraylist of all the files
            ArrayList<File> myFileArray = new ArrayList<File>(Arrays.asList(fileListUpdateListView));
            //Sort myFileArray by mod date, then clear myFileNameArray.
            sortByRecent(myFileArray, sortPreference);
            myFileNameArray.clear();
            //Loop through myFileArray and add all the .getName()s to myFileNameArray.
            for(File sortedFile : myFileArray){
                myFileNameArray.add(sortedFile.getName());
            }
            //Complete. Now myFileNameArray is sorted by recent.
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

    public void sortByAlphabetical(ArrayList<String> whichArray, int whichSort){
        //whichArray should have been passed in by reference
        switch(whichSort){
            case 0:{ //Alphanumeric 0-9, A-Z
                Collections.sort(whichArray, new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        return s1.compareToIgnoreCase(s2);
                    }
                });
                break;
            }
            case 1:{ //Alphanumeric Z-A, 9-0
                Collections.sort(whichArray, new Comparator<String>(){
                    @Override
                    public int compare(String s1, String s2) {
                        return s1.compareToIgnoreCase(s2);
                    }
                });
                Collections.reverse(whichArray);
                break;
            }
        }
    }

    public void sortByRecent(ArrayList<File> whichArray, int whichSort){
        switch(whichSort){
            case 2:{
                Collections.sort(whichArray, new Comparator<File>(){
                    @Override
                    public int compare(File file1, File file2){
                        return Long.valueOf(file1.lastModified()).compareTo(file2.lastModified());
                    }
                });
                Collections.reverse(whichArray);
                break;
            }
            case 3:{
                Collections.sort(whichArray, new Comparator<File>(){
                    @Override
                    public int compare(File file1, File file2){
                        return Long.valueOf(file1.lastModified()).compareTo(file2.lastModified());
                    }
                });
                break;
            }
        }
    }

    public void showAsShortToast(String text){
        CharSequence toastText = text;
        int duration = Toast.LENGTH_SHORT; //2 seconds
        Toast toast = Toast.makeText(getApplicationContext(), toastText, duration);
        toast.show();
    }

    @Override
    public void onResume(){
        super.onResume();
        updateListView();
    }
}