package com.neotech.loltoast;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by S840JPT on 16/04/2015.
 */
public class ServletParameters {

        String inputRegion;
        String inputName;
        TextView showLevel;
        SharedPreferences.Editor editor;

        ServletParameters(String inputRegion, String inputName, TextView showLevel, SharedPreferences.Editor editor){
            this.inputRegion = inputRegion;
            this.inputName = inputName;
            this.showLevel = showLevel;
            this.editor = editor;
        }

        public String getInputRegion(){
            return this.inputRegion;
        }

        public String getInputName(){
            return this.inputName;
        }

        public TextView getTextView(){
            return this.showLevel;
        }

        public SharedPreferences.Editor getEditor(){
        return this.editor;
    }

}
