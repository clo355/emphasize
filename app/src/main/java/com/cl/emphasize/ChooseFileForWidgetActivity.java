package com.cl.emphasize;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class ChooseFileForWidgetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_file_for_widget);

        ListView listView = (ListView) findViewById(R.id.listViewCFFW);
        TextView noFilesView = (TextView) findViewById(R.id.noFilesLabelCFFW);
        Button cancelButton = (Button)findViewById(R.id.cancelButtonCFFW);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        //Initially populate ListView
        ArrayList<String> myFileNameArray = new ArrayList<String>();
        File[] fileListOnCreate = getFilesDir().listFiles();
        for(File foundFile : fileListOnCreate){
            String foundFileName = foundFile.getName();
            myFileNameArray.add(foundFileName);
        }

        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                new ArrayList(myFileNameArray));
        //myFileNameArray will be deleted by clear(), if passed by reference! So, used new ArrayList().
        listView.setAdapter(listViewAdapter);

        if(fileListOnCreate.length == 0){
            noFilesView.setText("No files found");
        } else{
            noFilesView.setText("");
        }
    }
}
