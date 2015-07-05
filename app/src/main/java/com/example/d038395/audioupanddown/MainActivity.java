package com.example.d038395.audioupanddown;

import android.content.Context;
import android.media.MediaPlayer;
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
import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    private  ListView listView;
    private  File filepath;
    private class myAudioFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String filename) {
            String[] _ext ={".mp3",".wav",".3gp",".m4a"};
            int len=filename.length();
            if(len<=4)
                return false;
            String ext=filename.substring(len-4,len).toLowerCase();
            for(String str:_ext){
                if(ext.equals(str))
                    return true;
            }
            return false;
        }
    }
    private final myAudioFilenameFilter myfilter =new myAudioFilenameFilter();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String filePathString= Environment.getExternalStorageDirectory().getPath()+
                File.separator+getString(R.string.app_name);
        filepath= new File(filePathString);
        if (!filepath.isDirectory())
            filepath.mkdir();
        listView = (ListView) findViewById(R.id.list_item);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String audioSelect = filepath.getPath()
                        + File.separator + parent.getItemAtPosition(position);
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(audioSelect);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                        }
                    });
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    Toast.makeText(view.getContext(),
                            "can't play the media", Toast.LENGTH_LONG).show();
                }

            }
        });
        String[] fileListStr=filepath.list(myfilter);
        ArrayAdapter arrayAdapter=new ArrayAdapter<>(this,R.layout.audio_list,fileListStr);
        listView.setAdapter(arrayAdapter);

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
        switch (id){
            case R.id.action_settings:
                return true;
            case R.id.action_refresh:
                String[] fileListStr=filepath.list(myfilter);
                ArrayAdapter arrayAdapter=new ArrayAdapter<>(this,R.layout.audio_list,fileListStr);
                listView.setAdapter(arrayAdapter);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }


}
