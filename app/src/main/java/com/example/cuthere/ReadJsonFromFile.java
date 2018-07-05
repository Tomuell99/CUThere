package com.example.cuthere;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadJsonFromFile {


    public String read(String fileName , Context context){

        StringBuilder returnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;

        try {
            fIn = context.getResources().getAssets().open(fileName, Context.MODE_WORLD_READABLE);
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line = "";

            while ((line = input.readLine()) != null){
                returnString.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
                if (fIn != null){
                    fIn.close();
                }
                if (input != null){
                    input.close();
                }
            }catch (Exception e2){
                e2.getMessage();
            }
        }
        return returnString.toString();
    }
}
