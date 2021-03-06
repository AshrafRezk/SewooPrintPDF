package hgode.sewooprintpdf;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by E841719 on 24.11.2017.
 */

public class myIntentService extends IntentService {

    final String TAG="myIntentService";
    File bitmapfile=null;
    String bitmapFilename="";
    File textfile=null;
    String textFilename="";

    public myIntentService(){
        super("myIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
    }

    @Override
    protected void onHandleIntent(Intent intent){
        //prepare DATA
        String sFile=intent.getStringExtra(CONSTANTS.IntentServiceData_Filename);
        String sBTaddress=intent.getStringExtra(CONSTANTS.IntentServiceData_BTaddress);
        float fScale=intent.getFloatExtra(CONSTANTS.IntentServiceData_Scale, 3);

        //assign files
        try {
            bitmapfile = File.createTempFile("bitmap", ".bmp", this.getCacheDir());
            bitmapFilename=bitmapfile.getPath();
            Log.d(TAG, "using bitmapfile: "+bitmapFilename);
        }catch (IOException ex){
            Log.d(TAG,"Bitmap createTempFile failed: "+ex.getMessage());
            bitmapfile=null;
        }
        try{
            textfile=File.createTempFile("text",".txt",this.getCacheDir());
            textFilename=textfile.getPath();
            Log.d(TAG, "using textfile: "+textFilename);
        }catch (IOException ex){
            Log.d(TAG,"Text createTempFile failed: "+ex.getMessage());
            textfile=null;
        }

        //test bitmap file is ready
        if(bitmapfile==null){
            //return error
            Intent intentError=new Intent(CONSTANTS.ACTION);
            intentError.putExtra(CONSTANTS.IntentServiceData_RESULT_BITMAP_OK, Activity.RESULT_CANCELED);
            intentError.putExtra(CONSTANTS.IntentServiceData_RESULT_MESSAGE, "could not create bitmap");
            return;
        }

        //convert file to bitmap
        try{
            PDFprint pdFprint=new PDFprint(this.getApplicationContext());
            Bitmap bmp = pdFprint.renderFile(sFile, fScale, bitmapFilename);
            if(bmp!=null) {
                ; //Convert OK
                BTprinter bTprinter=new BTprinter();
                bTprinter.doPrint(bmp, sBTaddress);
            }
            else{
                //return
                Intent intentError=new Intent(CONSTANTS.ACTION);
                intentError.putExtra(CONSTANTS.IntentServiceData_RESULT_BITMAP_OK, Activity.RESULT_CANCELED);
                intentError.putExtra(CONSTANTS.IntentServiceData_RESULT_MESSAGE, "bitmap conversion FAILED");
                return;
            }
        }catch(Exception ex){

        }
        //print to Bluetooth
        try{

        }catch(Exception ex){

        }
        //WE ARE DONE, inform main activity
        // Construct an Intent tying it to the ACTION (arbitrary event namespace)
        Intent in = new Intent(CONSTANTS.ACTION);
        // Put extras into the intent as usual
        in.putExtra(CONSTANTS.IntentServiceData_RESULT_BITMAP_OK, Activity.RESULT_OK);
        in.putExtra(CONSTANTS.IntentServiceData_RESULT_BITMAP_FILE, bitmapfile);
        // Fire the broadcast with intent packaged
        LocalBroadcastManager.getInstance(this).sendBroadcast(in);
        // or sendBroadcast(in) for a normal broadcast;

    }
    void updateStatus(String sMsg){
        Log.d(TAG, sMsg);
    }
}
