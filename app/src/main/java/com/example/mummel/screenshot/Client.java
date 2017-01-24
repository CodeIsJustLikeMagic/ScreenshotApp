package com.example.mummel.screenshot;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client extends AsyncTask <Void, String, Bitmap> {
	String host;
	int port;
	File f;
    MainActivity b;
	int quality;
	String def; //default file ending

	Client(MainActivity b,String phost, int pport, File pf, int q, String d){ //gets all sorts of info from MainActivity
        this.b=b;
		host = phost;
		port = pport;
		f = pf;
		quality = q;
		def = d;
	}

	public Bitmap doInBackground(Void... b){ //downloads the image
        //System.out.println("Connecting...");
		Bitmap fad = null;
		try {
			Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 2000); //socket tries to connect to the computer indicated by host and port
			InputStream input = socket.getInputStream(); //receives the image as inputstream
            //System.out.println("Connected");
			fad = BitmapFactory.decodeStream(input); //saves the image as bitmap
			//System.out.println("got the input stream");
			String abs = f.getAbsolutePath(); //figuring out what format (fmt) the image is going to be saved / compressed as...
			int pointpos = abs.lastIndexOf('.');
			Bitmap.CompressFormat fmt=null;
			if(pointpos!=-1 && pointpos!=abs.length()-1){
				try {
					fmt = Bitmap.CompressFormat.valueOf(abs.substring(pointpos + 1).toUpperCase().replaceAll("JPG","JPEG"));
				}catch(IllegalArgumentException e){}
			}
			if(fmt==null){ //if no format is specified the image will be saved as default
				fmt=Bitmap.CompressFormat.valueOf(def.toUpperCase().replaceAll("JPG","JPEG"));
				f=new File(abs+"."+def);
			}
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(f)); //the image is saved in output
			fad.compress(fmt,quality,output); //compressed the image with the indicated format; image quality is specified
			input.close();
			output.close();
			socket.close();
            //System.out.println("Socket closed");
		}catch(IOException e){
			//System.out.println("Something went awry");
            e.printStackTrace();
			publishProgress(e.toString());


		}
		return fad; //returns the compressed image as bitmap
	}

    @Override
    public void onProgressUpdate(String... e){ //e[0]--> java.net.SocketTimeOutException : failed to Connect to/{ip}(port {port})after 2000ms
        if(e[0].equals("java.net.SocketTimeoutException: failed to connect to /"+host+" (port "+port+") after 2000ms")) {
			Toast.makeText(b, "unable to connect to ScreenshotServer", Toast.LENGTH_LONG).show();
		} else 	Toast.makeText(b, e[0], Toast.LENGTH_LONG).show();
		//System.out.print("onProgressUpdate");
    }

    @Override
    public void onPostExecute(Bitmap a){
        //System.out.println("File transfer complete");
        b.onImageDownloaded(f,a); // starts the method onImageDownload in MainActivity when done with downloading
    }
}
