package com.example.mummel.screenshot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.telecom.ConnectionService;

/**
 * Created by Mummel on 17.04.2016.
 */
public class EpicSettings extends AppCompatActivity {
    private MyEpicPreferanceFragment epicPreferanceFragment;


    protected void onCreate(Bundle savedInsateceState){
        //System.out.println("It's ALIIIVVVEEEEEE!!!!!");
        super.onCreate(savedInsateceState);
        setContentView(R.layout.epic_settings);
        epicPreferanceFragment = new MyEpicPreferanceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, epicPreferanceFragment).commit();

    }

    public static class MyEpicPreferanceFragment extends PreferenceFragment {
        public void onCreate(final Bundle savesInstanceState){
            super.onCreate(savesInstanceState);
            addPreferencesFromResource(R.xml.pref_epic);
        }
    };
}
