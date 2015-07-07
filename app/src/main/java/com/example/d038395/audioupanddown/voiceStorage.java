package com.example.d038395.audioupanddown;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by d038395 on 2015-07-07.
 */
public class voiceStorage implements Serializable{
    static Map<String,voiceStorage> voiceDict = new HashMap<>();
    static File app_dir=MainActivity.filepath;
    static File data_storage=new File(app_dir+File.separator+voiceStorage.class.toString());

    String question;
    String topic;

    String voiceText;
    String voiceFileName;
    String textFileName;
    String jsonFileName;
    String resultDir;

    String transcribedResult;

    File jsonPath;
    File textPath;
    File voicePath;
    File resultPath;


    public voiceStorage(String voiceFileName, String question, String topic) {
        this.voiceFileName = voiceFileName;
        voicePath =new File(app_dir+File.separator+this.voiceFileName);

        resultDir=this.voicePath.getAbsolutePath() +".result";
        resultPath=new File(resultDir);
        if(!resultPath.isDirectory())
            resultPath.mkdir();

        textFileName = this.voiceFileName +".txt";
        textPath= new File(resultPath.getPath()+File.separator+this.textFileName);

        jsonFileName =this.voiceFileName +".json";
        jsonPath = new File (resultPath.getPath()+File.separator+this.jsonFileName);



        this.question=question;
        this.topic=topic;

        transcribedResult=null;
        voiceText=null;
    }



    public voiceStorage(File voicePath,String question, String topic) {
        this.voicePath = voicePath;
        this.voiceFileName = voicePath.getName();

        resultDir=this.voicePath.getAbsolutePath() +".result";
        resultPath=new File(resultDir);
        if(!resultPath.isDirectory())
            resultPath.mkdir();

        textFileName = this.voiceFileName +".txt";
        textPath= new File(resultPath.getPath()+File.separator+this.textFileName);

        jsonFileName =this.voiceFileName +".json";
        jsonPath = new File (resultPath.getPath()+File.separator+this.jsonFileName);

        this.topic=topic;
        this.question=question;
        transcribedResult=null;
        voiceText=null;

    }

    public void put(){
        if(!voiceDict.containsKey(voiceFileName))
           voiceDict.put(this.voiceFileName,this);
    }

    public void putForce(AlertDialog.Builder builder) {
        if(voiceDict.containsKey(voiceFileName)) {
            builder.setTitle("Warning")
                    .setMessage(String.format("Do you want to reset the metadata of the %s?",
                            voiceFileName))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            voiceDict.put(voiceStorage.this.voiceFileName, voiceStorage.this);
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

    static public void buildFromFile(Context context){
        if(!data_storage.isFile()){
            Toast.makeText(context,"No previous data stored.",Toast.LENGTH_LONG).show();
        }
        else {
            try {
                FileInputStream fin = new FileInputStream(data_storage);
                ObjectInputStream objIn = new ObjectInputStream(fin);
                voiceDict=(HashMap<String ,voiceStorage>)objIn.readObject();
                Toast.makeText(context,"Read scuccessfully.",Toast.LENGTH_LONG).show();
                objIn.close();
                fin.close();
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
                Toast.makeText(context,e.toString(),Toast.LENGTH_LONG).show();
            }
        }
    }

    static public void initialize(){
        app_dir=MainActivity.filepath;
        data_storage=new File(app_dir+File.separator+"voiceStorage");
    }
    static public void storeToFile(Context context){

        try {
            FileOutputStream fout = new FileOutputStream(data_storage);
            ObjectOutputStream objOut = new ObjectOutputStream(fout);
            objOut.writeObject(voiceDict);
            Toast.makeText(context,"Stored successfully.",Toast.LENGTH_LONG).show();
            objOut.close();
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    static private void removeAll(){
        voiceDict.clear();
    }

    static public void removeAll(AlertDialog.Builder builder){
        builder.setTitle("Warning")
                .setMessage("Do you want to clear the array?")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeAll();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }
    private void remove(){
        if (voiceDict.containsKey(this.voiceFileName))
            voiceDict.remove(this.voiceFileName);
    }

    public void remove(AlertDialog.Builder builder){
        if(voiceDict.containsKey(this.voiceFileName)){
            builder.setTitle("Warning")
                    .setMessage(String.format(
                        "Do you want to remove \"%s\" from the array",
                        this.voiceFileName))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            remove();
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


    private void setQuestion(String question){
        this.question=question;
    }

    public void setQuestion(final String question, AlertDialog.Builder builder){
        if(this.question!=null) {
            builder.setTitle("Warning")
                    .setMessage(String.format(
                        "Do you want to change the question from \"%s\" to \" %s\" ",
                        this.question, question))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                         @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setQuestion(question);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.create().show();
        }
        else setQuestion(question);
    }

    private void setTopic(String topic){
        this.topic=topic;
    }

    public void setTopic(final String topic, AlertDialog.Builder builder){
        if(this.topic!=null) {
            builder.setTitle("Warning")
                    .setMessage(String.format(
                            "Do you want to change the topic from \"%s\" to \" %s\" ",
                            this.topic, topic))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setTopic(topic);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.create().show();
        }
        else setTopic(topic);
    }
    private void setVoiceText(String text){

        this.voiceText=text;
        try {
            FileOutputStream fout = new FileOutputStream(textPath);
            ObjectOutputStream objOut = new ObjectOutputStream(fout);
            objOut.writeObject(voiceText);
        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setVoiceText(final String text, AlertDialog.Builder builder){
        if(voiceText!=null) {
            builder.setTitle("Warning")
                    .setMessage("Do you want to change the text transcribed before?")
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setVoiceText(text);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.create().show();
        }
        else setVoiceText(text);
    }

    private void setJSON(JSONObject json){

        try {
            transcribedResult=json.toString();
            FileOutputStream fout = new FileOutputStream(jsonPath);
            ObjectOutputStream objOut = new ObjectOutputStream(fout);
            objOut.writeObject(transcribedResult);
        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setJSON(final JSONObject json, AlertDialog.Builder builder){
        if(transcribedResult!=null) {
            builder.setTitle("Warning")
                    .setMessage("Do you want to change the raw result transcribed before?")
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setJSON(json);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.create().show();
        }
        else setJSON(json);
    }

    static public boolean contains(String filename){
        return voiceDict.containsKey(filename);
    }
}
