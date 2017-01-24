package com.example.mummel.screenshot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Main2Activity extends AppCompatActivity {
    EditText editTextname;
    EditText editTexthost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ((EditText)findViewById(R.id.host)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    save(null);
                    return true;
                }
                return false;
            }
        });
        editTextname =(EditText)findViewById(R.id.name);
        editTexthost = (EditText)findViewById(R.id.host);
        if(getIntent()!=null&&("zugangsdaten".equals(getIntent().getAction()))){ //this is the case if Main2Activity was started by editing a menuItem
            String [] a = getIntent().getExtras().getStringArray("zugangsdaten");
            editTextname.setText(a[0]);
            editTexthost.setText(a[1]);
        }
    }

    public void save(View view){ //saves the content of the editTexts in Sharedpreference
        String name = editTextname.getText().toString();
        String host = editTexthost.getText().toString();
        if(name !=null && name.length()>0 && host != null && host.length() >0){
            SharedPreferences pref = getSharedPreferences(MainActivity.MY_PREFS_NAME,MODE_PRIVATE);
            int size = pref.getInt("size", 0);
            SharedPreferences.Editor e = pref.edit();
            e.putString("name_" + size, name);
            e.putString("hostPort_" + size, host);
            e.putInt("size", size + 1);
            e.commit();

            finish();
        }
    }

    public void readQrCode(View view){
        try { //form stackoverflow
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); //tells the reader that we want qr code to be scanned

            startActivityForResult(intent, 0);
        }catch (Exception e){
            Toast.makeText(this,"couldnt start activity",Toast.LENGTH_LONG).show();
        }

    }

    protected void onActivityResult(int resquestCode, int resultCode, Intent data){
        super.onActivityResult(resquestCode, resultCode, data);
        if (resquestCode==0){
            if(resultCode == RESULT_OK){
                String content = data.getStringExtra("SCAN_RESULT");
                editTexthost.setText(content);
            }
            if(resultCode==RESULT_CANCELED){
                Toast.makeText(this,"canceled",Toast.LENGTH_LONG).show();
            }
        }
    }
}
