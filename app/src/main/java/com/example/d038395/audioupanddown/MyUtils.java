package com.example.d038395.audioupanddown;

import android.content.Context;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.StaticLayout;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by d038395 on 2015-07-06.
 */

public class MyUtils {

    //format the milliseconds in to hh:mm:ss
    static String formatMilliToHMS(int milli){
        int hour, minute,second;
        second=milli/1000;
        minute=second/60;
        second%=60;
        if (minute<60)
            return String.format("%02d:%02d",minute,second);
        hour=minute/60;
        minute%=60;
        return String.format("%d:%02d:%02d",hour,minute,second);
    }

    static class TTs{
        Context context;
        TextToSpeech textToSpeech;
        public TTs(final Context context) {
            this.context = context;
            textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        Toast.makeText(context, "TTs initialized successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        public void SpeakText(String text, String utteranceId ) {
            textToSpeech.setLanguage(Locale.US);
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null,utteranceId);
        }

        public TextToSpeech getTTs(){
            return textToSpeech;
        }
    }
}
