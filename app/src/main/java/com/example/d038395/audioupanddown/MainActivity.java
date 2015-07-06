package com.example.d038395.audioupanddown;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.zip.Inflater;

public class MainActivity extends ActionBarActivity {

    private  ListView listView;
    private  File filepath;
    private String[] fileListStr;
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
                playAudio(audioSelect);
            }
        });
        refreshFileList();
        registerForContextMenu(listView);

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
                refreshFileList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if(v.getId()==listView.getId()){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(fileListStr[info.position]);
            String [] menuItem = getResources().getStringArray(R.array.contextMenuItem);
            for (int i=0 ;i < menuItem.length;i++){
                menu.add(Menu.NONE,i,0,menuItem[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemId= item.getItemId();
        String [] menuItem = getResources().getStringArray(R.array.contextMenuItem);
        String menuName=menuItem[menuItemId];
        String filename = fileListStr[info.position];

        Toast.makeText(this,String.format("%s is selected for file %s.",menuName,filename),
                Toast.LENGTH_SHORT).show();
        String audioPath =filepath.getPath() +File.separator+filename;
        switch (menuItemId) {
            case 0:
                playAudio(audioPath);
                break;
            case 1:
                break;
            case 2:
                changeFileName(audioPath);
                break;
            case 3:
                deleteAudio(audioPath);
                break;
            default:
                break;
        }
        return true;
    }

    private void deleteAudio(String filepath) {
        final File file= new File(filepath);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setMessage(String.format("Are you sure to delete file %s",file.getName()))
                .setTitle("Warning");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                file.delete();
                refreshFileList();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        // Set other dialog properties

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void playAudio(String filepath) {
        final MediaPlayer mediaPlayer = new MediaPlayer();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_player, null);
        builder.setView(dialogView);

        final TextView tvStart = (TextView)dialogView.findViewById(R.id.text_start);
        TextView tvDuration = (TextView) dialogView.findViewById(R.id.text_duration);
        final SeekBar seekBar = (SeekBar)dialogView.findViewById(R.id.seekbar_player);
        class audioPlayTextUpdate extends AsyncTask<MediaPlayer,Integer,Void> {
            protected Void doInBackground(MediaPlayer... mps) {
                MediaPlayer mp = mps[0];
                while(mp!=null) {
                    if(mp.isPlaying())
                        publishProgress(mp.getCurrentPosition());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
            protected void onProgressUpdate(Integer... progress) {
                tvStart.setText(MyUtils.formatMilliToHMS(progress[0]));
                seekBar.setProgress(progress[0]);
            }
        }
        final AsyncTask<MediaPlayer,Integer,Void> asyncTask =new audioPlayTextUpdate();
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                asyncTask.cancel(true);
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        });
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                mp.start();
            }
        });
        try {
            mediaPlayer.setDataSource(filepath);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    tvStart.setText(MyUtils.formatMilliToHMS(seekBar.getProgress()));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mediaPlayer.pause();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
                    tvStart.setText(MyUtils.formatMilliToHMS(progress));
                    mediaPlayer.seekTo(progress);
                }
            });
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            tvDuration.setText(MyUtils.formatMilliToHMS(duration));
            seekBar.setMax(duration);
            tvStart.setText(MyUtils.formatMilliToHMS(mediaPlayer.getCurrentPosition()));
            mediaPlayer.start();
            builder.create().show();
            asyncTask.execute(mediaPlayer);
        } catch (IOException e) {
            Toast.makeText(this,
                    "can't play the media", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    private void refreshFileList(){
        fileListStr=filepath.list(myfilter);
        ArrayAdapter arrayAdapter=new ArrayAdapter<>(this,R.layout.audio_list,fileListStr);
        listView.setAdapter(arrayAdapter);
    }
    private void changeFileName(String filepath) {
        final File oldFilePath=new File(filepath);
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_rename_file, null);
        EditText editText=(EditText) dialogView.findViewById(R.id.edit_filename);
        editText.setText(oldFilePath.getName());
        editText.setSelection(0,oldFilePath.getName().length()-4);
        builder.setTitle("Enter new filename")
                .setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText=(EditText) dialogView.findViewById(R.id.edit_filename);
                        String newFilePath= oldFilePath.getParent()+
                                File.separator+
                                editText.getText().toString();
                        oldFilePath.renameTo(new File(newFilePath));
                        refreshFileList();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }
}
