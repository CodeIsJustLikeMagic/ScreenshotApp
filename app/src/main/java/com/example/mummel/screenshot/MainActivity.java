package com.example.mummel.screenshot;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.net.URI;

public class MainActivity extends AppCompatActivity {
    public static final String MY_PREFS_NAME = "screenshotprefs";  // name of the preference file adresses and their names are saved in
    protected boolean standalone=true;  //saves whether this app was opened by another app (e.g. WhatsApp)
    private SharedPreferences epicpref;  // used to accses saved addresses and their names
    private SharedPreferences defaultpref; //used to accses the preferancec set in EpicSettings
    @Override

    public boolean onCreateOptionsMenu(android.view.Menu menu){ //set the menu "main" as the optionsmenu
        MenuInflater inflater = (MenuInflater)getMenuInflater();
        inflater.inflate(R.menu.epic_menu, menu);
        return true;
    }

    public void onClickOptionMenu(MenuItem menuItem){ //method is used in the menu "epic_main"; switches to epic_settings
        Intent intent = new Intent(this,EpicSettings.class);
        startActivity(intent);
    }


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = ((ListView)findViewById(R.id.listView));
        epicpref = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        defaultpref = PreferenceManager.getDefaultSharedPreferences(this);


        populateList(listView);//fills the list with the addresses and their names

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //if an item in the list is clicked the host and port field will be set to the address in the item
                View e = view;
                TextView t = (TextView) e.findViewById(android.R.id.text2);
                ((EditText) findViewById(R.id.Host)).setText(t.getText().toString());
            }
        });
        registerForContextMenu(listView);

        ((EditText)findViewById(R.id.Host)).setOnEditorActionListener(new EditText.OnEditorActionListener() { //when the user clicks enter on the keyboard, the keyboard dissapears
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    testIntent(null);
                    return true;
                }
                return false;
            }
        });
        if("android.media.action.IMAGE_CAPTURE".equals(getIntent().getAction())){ // if the app is started as an intent by another app
            standalone=false;
            String s = getIntent().getExtras().getString(MediaStore.EXTRA_OUTPUT);
            Uri givenuri = s==null?null:Uri.parse(s); //saves the uri it might have received from the app that started this app
            if(givenuri==null)
                givenuri=getIntent().getClipData().getItemAt(0).getUri();
            EditText sfed = ((EditText)findViewById(R.id.Dateiname));
            if(givenuri!=null){
                String wantedtarget = givenuri.getPath();
                sfed.setText(wantedtarget); // the path the uri indicates will be used as path for the image. The path is displayed in the "dateiname" edittext
            }else sfed.setText("no uri given");
            if(defaultpref.getBoolean("insta_on_intent",false)){  //if the user set he preference insta on intent to ture the app will instantly capture a picture and send it to the app that opened this app
                testIntent(null);
            }
        }

    }

    public void populateList(ListView listView){ //adds the items to the list view that are saved in SharedPreferences

        int size = epicpref.getInt("size", 0);
        final String[] name = new String[size];
        final String[] hostPort = new String[size];
        String array[] = new String[size];
        for(int i=0;i<size; i++) { //the prefs are saved in SharedPreference there are name and hostport pairs that are together one listitem
            name[i] = epicpref.getString("name_" + i, "empty"); //the first pair ist name_0 and hostport_0
            hostPort[i]= epicpref.getString("hostPort_"+i, "empty");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_2,android.R.id.text1,name){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(name[position]);

                text2.setText(hostPort[position]);
                return view;
            }
        };
        listView.setAdapter(adapter);
        //the first list entry is automatically inserted into the editText for ip and host
        ((EditText)findViewById(R.id.Host)).setText(epicpref.getString("hostPort_0", ""));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) { //context Menu is created when a listview ist longclicked
        if(v.getId()==R.id.listView){
            AdapterView.AdapterContextMenuInfo info =(AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.add(0,4,0,"move to top"); //all done
            menu.add(0,1,1,"move up");
            menu.add(0,2,2,"move down");
            menu.add(0,3,4,"Remove");
            menu.add(0,5,3, "Edit");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int selectedfav = info.position;
        int selectedentryincontextmenu = item.getItemId();
        if(selectedentryincontextmenu==3){ //if the context menu is "remove" we do epic stuff to remove the menuItem
            removeItem(selectedfav);
        }
        if(selectedentryincontextmenu==1){   //move the item up / swaps the item with the item above
            //System.out.println("selectedfav: "+selectedfav);

            if(selectedfav >0) { //0 is the first item in the list
                //reads the content of the item above
                String aboveName = epicpref.getString("name_" + (selectedfav - 1), "epmty");
                String aboveHost = epicpref.getString("hostPort_" + (selectedfav - 1), "empty");
                //reads the content of the selected item
                String currentName = epicpref.getString("name_" + (selectedfav), "epmty");
                String currentHost = epicpref.getString("hostPort_" + selectedfav, "empty");

                //overwrites the contents
                SharedPreferences.Editor e = epicpref.edit();
                e.putString("name_" + (selectedfav - 1), currentName);
                e.putString("hostPort_" + (selectedfav - 1), currentHost);
                e.putString("name_" + selectedfav, aboveName);
                e.putString("hostPort_" + selectedfav, aboveHost);

                e.commit();
            }
        }
        if(selectedentryincontextmenu==2) {   //move the item down / swaps the item with the item below
            //System.out.println("selectedfav: "+selectedfav);
            if (selectedfav < epicpref.getInt("size",0)) { //size-1 is the last item in the list
                //reads the content of the item below
                String belowName = epicpref.getString("name_" + (selectedfav + 1), "epmty");
                String belowHost = epicpref.getString("hostPort_" + (selectedfav + 1), "empty");
                //reads the content of the selected item
                String currentName = epicpref.getString("name_" + (selectedfav), "epmty");
                String currentHost = epicpref.getString("hostPort_" + selectedfav, "empty");

                //overwrites the contents
                SharedPreferences.Editor e = epicpref.edit();
                e.putString("name_" + (selectedfav + 1), currentName);
                e.putString("hostPort_" + (selectedfav + 1), currentHost);
                e.putString("name_" + selectedfav, belowName);
                e.putString("hostPort_" + selectedfav, belowHost);

                e.commit();
            }
        }
        if(selectedentryincontextmenu==4){ //move to top
            //System.out.print("moving item no."+selectedfav+"to top.");

            if(selectedfav>0){
                SharedPreferences.Editor e = epicpref.edit();
                String beOnTopName = epicpref.getString("name_" + (selectedfav), "empty");
                String beOnTopHost = epicpref.getString("hostPort_"+ (selectedfav),"empty");

                for (int i = selectedfav; i >0 ;i--){ //overwrite the item with the item above do this until you reach the top
                    String aboveName = epicpref.getString("name_"+(i-1),"empty");
                    String aboveHost = epicpref.getString("hostPort_"+ (i-1),"empty");

                    //put the above content in the current item
                    e.putString("name_"+i,aboveName);
                    e.putString("hostPort_"+i,aboveHost);
                }
                //we've reached the first (0) we insert the beOnTop content
                e.putString("name_"+0, beOnTopName);
                e.putString("hostPort_"+0,beOnTopHost);

                e.commit();
            }
        }
        if(selectedentryincontextmenu==5){ //edit
            String name = epicpref.getString("name_"+ (selectedfav), "empty");
            String host = epicpref.getString("hostPort_"+ (selectedfav), "empty");
            removeItem(selectedfav);
            Intent intent= new Intent(this, Main2Activity.class);
            String[] a = new String[2];
            a[0] = name;
            a[1]= host;
            intent.setAction("zugangsdaten");
            intent.putExtra("zugangsdaten",a);
            startActivityForResult(intent, 107);

        }

        populateList(((ListView) findViewById(R.id.listView)));

        return true;
    }

    private void removeItem (int selectedfav){
        //System.out.println("selectedfav: "+selectedfav);
        int size = epicpref.getInt("size", 0);
        SharedPreferences.Editor e = epicpref.edit();
        for(int i=selectedfav; i<(size-1); i++) { //size is the number of items there are. items are counted from 0 to size-1. e.g. size 4; items 0,1,2,3

            String name =  epicpref.getString("name_" + (i + 1), "empty"); //moves up all the items
            String host = epicpref.getString("hostPort_" + (i + 1), "empty");
            e.putString("name_" + i, name);
            e.putString("hostPort_" + i, host);
        }

        e.remove("name_"+(size-1)); //removes the last item
        e.remove("hostPort_"+(size-1));
        e.putInt("size", size-1);
        e.commit();
    }

    public void testIntent(View v) { //gets called when a picture is requested
        if (ContextCompat.checkSelfPermission(this, //makes sure the user has granted permission. If it is not grated: requestPermission
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                //System.out.println("didnt get permission");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET},
                        0);
        } else{
            String[]parts = ((EditText)findViewById(R.id.Host)).getText().toString().split(":"); //figures out where to connect to...
            String host = parts[0];
            int port = Integer.parseInt(defaultpref.getString("default_port_preference","1000"));
            if(parts.length<0){
                try{
                    port = Integer.parseInt(parts[1]);
                }catch(NumberFormatException n){}
            }
            File f; //gets the file name from edittext
            File d = new File(((EditText)findViewById(R.id.Dateiname)).getText().toString());
            if(d.isAbsolute()){ //figures out if the pic should be saved in PICTURES or in a specific place
                f = d;
            }else {
                f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ((EditText) findViewById(R.id.Dateiname)).getText().toString());
            }

            new Client(this,host,port, f,Integer.parseInt(defaultpref.getString("image_quality_preference","100")), defaultpref.getString("image_file_preference","png")).execute((Void[]) null); //client is going to take care of downloading the Image . it will be saved in the specified file
        }
    }

    public void onImageDownloaded(File f,Bitmap b) { //f and b are the downloaded image
        if(b==null)
            return;
        if(standalone){

            if(defaultpref.getBoolean("crop_standalone_preference",true)){
                startcrop(f,b);
                //System.out.println("I am  cropping");
            }
            if(defaultpref.getBoolean("save_in_gallery",true)) {
                //System.out.println("trying to save the pic in the gallery");
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File g = new File(f.getAbsolutePath());
                Uri contentUri = Uri.fromFile(g);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
            }
            Toast.makeText(this,"Image saved to: "+f.getAbsolutePath(),Toast.LENGTH_LONG).show();
        } else{ //on intent
            if(defaultpref.getBoolean("crop_not_standalone_preference", false)){
                sendToCaller(startcrop(f, b));
                //System.out.println("I am cropping");

            }
            sendToCaller(b);
        }

    }

    private Bitmap startcrop(File file, Bitmap bitmap){
        //System.out.println("cropping...");
        File target = file;
        Uri uri = Uri.fromFile(file);
        Crop.of(uri, Uri.fromFile(target)).start(this);//overwrite the image in file with the cropped image
        bitmap = BitmapFactory.decodeFile(file.getPath()); //overwrite bitmap with the cropped bitmap
        //System.out.println("cropped");
        //Toast.makeText(this,"Image saved to: "+file.getAbsolutePath(),Toast.LENGTH_LONG).show();
        return bitmap;
    }

    private void sendToCaller(Bitmap b){
        if(b!=null) {
            Intent result = new Intent("com.mummel.screenshot.RESULT_ACTION");
            // the image needs to be scaled down
            double aspectRatio = b.getWidth() / (double) b.getHeight();
            int width = Integer.parseInt(defaultpref.getString("image_width_preference", "480"));
            int height = (int) Math.round(width / aspectRatio);
            //System.out.println("calculated height:" + height);
            b = Bitmap.createScaledBitmap(b, width, height, false);
            result.putExtra("data", b);
            setResult(Activity.RESULT_OK, result);
            finish(); //result is "returned" to the app that called this app
        }else Toast.makeText(this,"there is no image", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==107){ // when the Activity was Main2Activity
            populateList((ListView)findViewById(R.id.listView));
            //System.out.println("repopulated list");
        }
    }

    public void create(View view){
        Intent intent = new Intent(this, Main2Activity.class);
        startActivityForResult(intent,107);
    }

}