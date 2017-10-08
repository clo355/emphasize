package com.cl.emphasize;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
 *   This app allows you to post blinking note widgets to your home screen.
 *
 *   @author Chris Lo
 **********************************************************************************/

public class MainActivity extends AppCompatActivity {

    public static final int ACCESSED_SETTINGS_REQUEST_CODE = 1;
    public static final int ACCESSED_FILE_REQUEST_CODE = 2;
    protected ListView listView;
    protected ArrayAdapter<String> listViewAdapter;
    protected ArrayList<String> myFileNameArray;
    protected TextView textPrint = null;
    public static final String PREFS_NAME = "PreferenceFile";

    @TargetApi(21)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //manually setting theme like this may give black background on certain devices
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int globalTheme = settings.getInt("globalTheme", R.style.lightTheme);
        setTheme(globalTheme);

        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        textPrint = (TextView) findViewById(R.id.textPrint);
        final Button mainSortButton = (Button)findViewById(R.id.mainSortButton);
        Button mainNewTextFileButton = (Button)findViewById(R.id.mainNewTextFileButton);
        mainNewTextFileButton.setBackgroundResource(R.mipmap.new_file_icon_normal);
        Button mainSettingsButton = (Button)findViewById(R.id.mainSettingsButton);
        mainSettingsButton.setBackgroundResource(R.mipmap.settings_icon_normal);

        //Load theme
        ConstraintLayout mainLayout = (ConstraintLayout)findViewById(R.id.activity_main);
        if(globalTheme == R.style.darkTheme){
            mainLayout.setBackgroundResource(R.mipmap.background_dark);
        }

        loadSortIcon(mainSortButton);

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
            textPrint.setText("No notes found");
        } else{
            textPrint.setText("");
        }

        //Press sort
        mainSortButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Sort:");
                CharSequence[] sortChoices = {"Alphanumeric 0-9, A-Z", "Alphanumeric Z-A, 9-0",
                        "Newest - Oldest", "Oldest - Newest"};
                final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                int savedSort = settings.getInt("savedSort", 2);
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
                                        loadSortIcon(mainSortButton);
                                        dialog.cancel();
                                    }
                                }, 200); //delay to let user see radio changed
                                break;
                            }
                            case 1:{ //Alphanumeric Z-A, 9-0
                                myHandler.postDelayed(new Runnable() {
                                    public void run(){
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putInt("savedSort", 1);
                                        editor.commit();
                                        updateListView();
                                        loadSortIcon(mainSortButton);
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
                                        loadSortIcon(mainSortButton);
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
                                        loadSortIcon(mainSortButton);
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
                newFileIntent.putExtra("fileName", "New Note");
                newFileIntent.putExtra("fileContents", "");
                newFileIntent.putExtra("isNewFile", true);
                startActivityForResult(newFileIntent, ACCESSED_FILE_REQUEST_CODE);
            }
        });

        //Press settings
        mainSettingsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent accessSettingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(accessSettingsIntent, ACCESSED_SETTINGS_REQUEST_CODE);
            }
        });

        //Press ListView object
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
                File fileClicked = new File(getFilesDir(), myFileNameArray.get(position));
                String fileContents = "";
                Intent fileIntent = new Intent(getApplicationContext(), TextEditorActivity.class);
                try {
                    BufferedReader fileReader = new BufferedReader(new FileReader(fileClicked));
                    String line;
                    while((line = fileReader.readLine()) != null) {
                        fileContents = fileContents + line + "\n";
                    }
                    //remove last newline if file isn't empty string
                    if(!fileContents.equals("")){
                        fileContents = fileContents.substring(0, fileContents.length() - 1);
                    }
                } catch(IOException e){
                    Log.d("MAIN", "IOException");
                }

                fileIntent.putExtra("fileName", fileClicked.getName());
                fileIntent.putExtra("fileContents", fileContents);
                fileIntent.putExtra("isNewFile", false);
                startActivityForResult(fileIntent, ACCESSED_FILE_REQUEST_CODE);
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
                                Intent fileIntent = new Intent(getApplicationContext(), TextEditorActivity.class);
                                try {
                                    BufferedReader fileReader = new BufferedReader(new FileReader(longClickedFile));
                                    String line;
                                    while((line = fileReader.readLine()) != null) {
                                        fileContents = fileContents + line + "\n";
                                    }
                                } catch(IOException e){
                                    Log.d("MAIN", "IOException");
                                }

                                fileIntent.putExtra("fileName", longClickedFile.getName());
                                fileIntent.putExtra("fileContents", fileContents);
                                fileIntent.putExtra("isNewFile", false);
                                startActivity(fileIntent);
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
                                builder.show();
                            }
                        }
                    }
                });
                builder.show();
                return true; //prevents onItemClick() from also firing after onItemLongClick()
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
         * 2 is Newest - Oldest
         * 3 is Oldest - Newest
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
            textPrint.setText("No notes found");
        } else{
            textPrint.setText("");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent){
        if(requestCode == ACCESSED_SETTINGS_REQUEST_CODE){
            //user might have changed theme
            recreate();
        } else if(requestCode == ACCESSED_FILE_REQUEST_CODE){
            updateListView();
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

    public void loadSortIcon(Button sortButton){
        //Set's the sort icon accordingly
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int whichSortIcon = settings.getInt("savedSort", 2);
        if(whichSortIcon == 0) {
            sortButton.setBackgroundResource(R.mipmap.sort_abc_down_icon_normal);
        } else if(whichSortIcon == 1){
            sortButton.setBackgroundResource(R.mipmap.sort_abc_up_icon_normal);
        } else if(whichSortIcon == 2){
            sortButton.setBackgroundResource(R.mipmap.sort_recent_down_icon_normal);
        } else{ //3
            sortButton.setBackgroundResource(R.mipmap.sort_recent_up_icon_normal);
        }
    }

    public void showAsShortToast(String text){
        CharSequence toastText = text;
        int duration = Toast.LENGTH_SHORT; //2 seconds
        Toast toast = Toast.makeText(getApplicationContext(), toastText, duration);
        toast.show();
    }
}