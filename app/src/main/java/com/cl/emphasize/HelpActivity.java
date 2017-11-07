package com.cl.emphasize;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

public class HelpActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "PreferenceFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int globalTheme = settings.getInt("globalTheme", R.style.lightTheme);
        setTheme(globalTheme);

        setContentView(R.layout.activity_help);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //help topic list
        ArrayList<String> helpQueryArray = new ArrayList<String>();
        helpQueryArray.add("❔    Save As"); //0
        helpQueryArray.add("❔    Renaming and deleting notes"); //1
        helpQueryArray.add("❔    Pinning notes on your home screen"); //2
        helpQueryArray.add("❔    Adjusting size of pinned notes"); //3

        ListView helpList = (ListView)findViewById(R.id.helpList);
        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                new ArrayList(helpQueryArray));
        helpList.setAdapter(listViewAdapter);
        helpList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int helpPosition, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HelpActivity.this);
                LayoutInflater inflater = LayoutInflater.from(HelpActivity.this);
                View inflatedView = inflater.inflate(R.layout.help_dialog_image_layout, null);
                ImageView helpImage = inflatedView.findViewById(R.id.helpDialogImageView);
                switch(helpPosition){
                    case 0:{
                        builder.setTitle(R.string.help0_title);
                        builder.setMessage(R.string.help0_text);
                        helpImage.setImageResource(R.mipmap.help0);
                        break;
                    }
                    case 1:{
                        builder.setTitle(R.string.help1_title);
                        builder.setMessage(R.string.help1_text);
                        helpImage.setImageResource(R.mipmap.help0);
                        break;
                    }
                    case 2:{
                        builder.setTitle(R.string.help2_title);
                        builder.setMessage(R.string.help2_text);
                        helpImage.setImageResource(R.mipmap.help0);
                        break;
                    }
                    case 3:{
                        builder.setTitle(R.string.help3_title);
                        builder.setMessage(R.string.help3_text);
                        helpImage.setImageResource(R.mipmap.help0);
                        break;
                    }
                }
                builder.setView(inflatedView);
                builder.setCancelable(true);
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        Button helpBackButton = (Button)findViewById(R.id.helpBackButton);
        helpBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}
