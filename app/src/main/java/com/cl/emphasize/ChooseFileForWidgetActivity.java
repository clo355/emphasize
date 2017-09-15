package com.cl.emphasize;

import android.content.Intent;
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

public class ChooseFileForWidgetActivity extends AppCompatActivity {

    protected ArrayList<String> myFileNameArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_file_for_widget);

        ListView listView = (ListView) findViewById(R.id.listViewCFFW);
        final TextView blinkRateInput = (TextView)findViewById(R.id.blinkRateInputCFFW);
        TextView noFilesView = (TextView) findViewById(R.id.noFilesLabelCFFW);
        Button cancelButton = (Button)findViewById(R.id.cancelButtonCFFW);

        //Initially populate ListView
        myFileNameArray = new ArrayList<String>();
        File[] fileListOnCreate = getFilesDir().listFiles();
        for(File foundFile : fileListOnCreate){
            String foundFileName = foundFile.getName();
            myFileNameArray.add(foundFileName);
        }

        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                new ArrayList(myFileNameArray));
        //myFileNameArray will be deleted by clear(), if passed by reference! So used new ArrayList().
        listView.setAdapter(listViewAdapter);

        //"No files found" label
        if(fileListOnCreate.length == 0){
            noFilesView.setText("No files found");
        } else{
            noFilesView.setText("");
        }

        //Pressed listView object, send contents to widget
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
                File fileClicked = new File(getFilesDir(), myFileNameArray.get(position));
                String fileContents = "";
                int blinkDelay = Integer.parseInt(blinkRateInput.getText().toString());
                try {
                    BufferedReader fileReader = new BufferedReader(new FileReader(fileClicked));
                    String line;
                    while((line = fileReader.readLine()) != null) {
                        fileContents = fileContents + line + "\n";
                    }
                } catch(IOException e){
                    Log.d("CFFWactivity", "IOException");
                }
                Intent intent = new Intent(getApplicationContext(), FileContentsWidget.class);
                intent.setAction(FileContentsWidget.CHOOSE_FILE_ACTION);
                intent.putExtra("fileContents", fileContents);
                intent.putExtra("blinkDelay", blinkDelay);
                sendBroadcast(intent);
                finish();
            }
        });

        //Pressed cancel
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
