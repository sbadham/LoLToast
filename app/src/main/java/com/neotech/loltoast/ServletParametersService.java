package com.neotech.loltoast;

import android.app.Service;
import android.widget.TextView;

/**
 * Created by S840JPT on 16/04/2015.
 */
public class ServletParametersService {

        String inputRegion;
        String inputName;
        Service host;

        ServletParametersService(String inputRegion, String inputName, Service host){
            this.inputRegion = inputRegion;
            this.inputName = inputName;
            this.host = host;
        }

        public String getInputRegion(){
            return this.inputRegion;
        }

        public String getInputName(){
            return this.inputName;
        }

        public Service getHost(){
            return this.host;
        }

}
