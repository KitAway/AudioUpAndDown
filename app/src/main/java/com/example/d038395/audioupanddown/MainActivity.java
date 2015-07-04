package com.example.d038395.audioupanddown;

import android.content.Context;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String filePathString= Environment.getExternalStorageDirectory().getPath()+
                File.separator+getString(R.string.app_name);
        final File filepath= new File(filePathString);
        if (!filepath.isDirectory())
            filepath.mkdir();
        String[] filelistStr=filepath.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                String mp3=".mp3";
                String wav=".wav";
                String _3gp=".3gp";
                int len=filename.length();
                if(len<=4)
                    return false;
                String ext=filename.substring(len-4,len).toLowerCase();
                return (ext.equals(mp3) || ext.equals(wav) || ext.equals(_3gp));
            }
        });
        ArrayAdapter arrayAdapter=new ArrayAdapter<>(this,R.layout.audio_list,filelistStr);;
        ListView listView = (ListView) findViewById(R.id.list_item);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(view.getContext(),
                        (String)parent.getItemAtPosition(position),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
