package com.example.d038395.audioupanddown;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.UUID;

public class MainActivity extends ActionBarActivity {

    private  ListView listView;
    public static  File filepath;
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
        voiceStorage.initialize();
        voiceStorage.buildFromFile(this);
        refreshFileList();
        registerForContextMenu(listView);


    }

    @Override
    protected void onStop() {
        voiceStorage.storeToFile(this);
        super.onStop();
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
        String textPath = filepath.getPath()+File.separator+filename+".str";
        String resultPath = filepath.getPath()+File.separator+filename+".json";
        switch (menuItemId) {
            case 0:
                playAudio(audioPath);
                break;
            case 1:
                transcribeAudio(audioPath,textPath);
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
    private class myParas{
        URL url;
        UUID uuid;
        File file;
        byte [] buffer;

        myParas(URL url,UUID uuid,File file,byte buffer[]){
            this.url=url;
            this.uuid=uuid;
            this.file=file;
            this.buffer=buffer;
        }
    }
    private void transcribeAudio(String filepath, String resultPath) {
        //MediaPlayer mp =MediaPlayer.create(this, Uri.parse(filepath));
        //int duration = mp.getDuration();

        UUID uuid= UUID.randomUUID();
        File file= new File(filepath);
        long fileLength=file.length();
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
            final byte[] buffer= new byte[(int)fileLength];
            fis.read(buffer);
            String urlString = getString(R.string.server_url);
            URL url= new URL(urlString);
            myParas mp=new myParas(url,uuid,file,buffer);
            new taskExecute().execute(mp);
            Toast.makeText(this,"Transcribing in background.",Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Toast.makeText(this,"File not found.",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(this,"Read file error.",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void playAudio(String filepath) {

        String filename =new File(filepath).getName();
        final MediaPlayer mediaPlayer = new MediaPlayer();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_player, null);
        builder.setView(dialogView);
        final TextView tvStart = (TextView)dialogView.findViewById(R.id.text_start);
        TextView tvDuration = (TextView) dialogView.findViewById(R.id.text_duration);
        final SeekBar seekBar = (SeekBar)dialogView.findViewById(R.id.seekbar_player);
        final TextView tvFilename = ((TextView)dialogView.findViewById(R.id.text_filname));
        TextView para = (TextView)dialogView.findViewById(R.id.text_wholeStory);
        para.setText(voiceStorage.voiceDict.get(filename).voiceText);
        tvFilename.setText(filename);
        final ImageButton imageButton = (ImageButton)dialogView.findViewById(R.id.btn_control);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    imageButton.setImageResource(R.drawable.ic_action_play);
                }
                else {
                    mediaPlayer.start();
                    imageButton.setImageResource(R.drawable.ic_action_pause);
                }

            }
        });
        class audioPlayTextUpdate extends AsyncTask<MediaPlayer,Integer,Void> {
            protected Void doInBackground(MediaPlayer... mps) {
                MediaPlayer mp = mps[0];
                while(mp!=null) {
                    if(mp.isPlaying())
                        publishProgress(mp.getCurrentPosition());
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
                imageButton.setImageResource(R.drawable.ic_action_pause);
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
                    imageButton.setImageResource(R.drawable.ic_action_play);
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
        for(String str:fileListStr){
            if(!voiceStorage.contains(str))
                new voiceStorage(str,null,null).put();
        }
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

    class taskExecute extends AsyncTask<myParas, Float, String> {
        protected String doInBackground(myParas... params) {
            float progress=0;
            HttpURLConnection httpConn=null;
            String result="";
            try {
                URL url=params[0].url;
                byte[] buffer=params[0].buffer;
                UUID uuid=params[0].uuid;
                File file = params[0].file;
                result+=file.getName()+"\nResult:\n";
                httpConn=(HttpURLConnection)url.openConnection();
                httpConn.setConnectTimeout(7000);
                httpConn.setRequestMethod("POST");
                httpConn.setRequestProperty("id", uuid.toString());
                httpConn.setRequestProperty("audioname", file.getName());
                httpConn.setRequestProperty("portBias",
                        Integer.toString(Character.getNumericValue(uuid.toString().charAt(0))));
                httpConn.setUseCaches(false);
                httpConn.setDoInput(true);
                httpConn.setDoOutput(true);
                DataOutputStream wr =new DataOutputStream(httpConn.getOutputStream());
                wr.write(buffer);
                wr.close();

                InputStream is =httpConn.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while((line=rd.readLine())!=null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();


                /*
                Get data
                 */
                boolean loopGetResult=false;
                if (httpConn.getResponseCode() == httpConn.HTTP_OK) {
                    loopGetResult=true;
                    httpConn.disconnect();
                    String getURL=getResources().getString(R.string.server_url)+
                            "/status/"+uuid.toString();
                    url= new URL(getURL);
                }

                while (loopGetResult) {
                    httpConn=(HttpURLConnection)url.openConnection();
                    httpConn.setRequestProperty("portBias",
                            Integer.toString(Character.getNumericValue(uuid.toString().charAt(0))));
                    is=httpConn.getInputStream();
                    rd = new BufferedReader(new InputStreamReader(is));
                    response = new StringBuilder();
                    while((line=rd.readLine())!=null) {
                        response.append(line);
                        response.append('\r');
                    }
                    rd.close();
                    JSONObject jsObj= new JSONObject(response.toString());
                    String status=jsObj.getString("status");
                    switch (status) {
                        case "TRANSCRIBED":
                            result+="!!!TRANSCRIBED SUCCESSFULLY!!!";
                            AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
                            if (voiceStorage.contains(file.getName())) {
                                voiceStorage.voiceDict.get(file.getName()).setJSON(jsObj, builder);
                            }
                            else new voiceStorage(file.getName(),null,null).setJSON(jsObj,builder);
                            String wholePare=readJson(jsObj);
                            voiceStorage.voiceDict.get(file.getName()).setVoiceText(wholePare,builder);
                            //writeIntoFile;
                            publishProgress(progress);
                            return result;
                        case "FAILED":
                            result+= "!!!TRANSCRIBED FAILED!!!";
                            publishProgress(progress);
                            return result;
                        case "QUEUED":
                        case "TRANSCRIBING":
                            publishProgress(progress);
                            Thread.sleep(1000);
                            break;
                        default:
                            result+= "!!!UNKNOWN ERROR!!!";
                            publishProgress(progress);
                            return result;
                    }
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this,
                        "TimeOut when connect server.",Toast.LENGTH_SHORT).show();
                if(httpConn!=null){
                    httpConn.disconnect();
                    httpConn=null;
                }
            }
            return result;
        }

        protected void onProgressUpdate(Float... progress) {

        }

        protected void onPostExecute(String result) {
            Toast.makeText(MainActivity.this,result,Toast.LENGTH_LONG).show();
        }


        private String readJson(JSONObject jsObj) {
            JSONObject jsBlock;
            String key;
            StringBuilder sBuffer=new StringBuilder();
            try {
                JSONObject js=jsObj.getJSONObject("channels").
                        getJSONObject("firstChannelLabel").
                        getJSONObject("lattice").getJSONObject("1").
                        getJSONObject("links");
                Iterator<?> keys = js.keys();
                while (keys.hasNext()) {
                    key=(String)keys.next();
                    jsBlock=js.getJSONObject(key);
                    if (jsBlock.getBoolean("best_path")) {
                        key=jsBlock.getString("word");
                        if (key.charAt(0)=='!')
                            continue;
                        sBuffer.append(key).append(' ');
                    }
                }
            } catch (JSONException e) {
                Log.e("error", "caused by json.");
            }
            return sBuffer.toString();
        }
    }
}
