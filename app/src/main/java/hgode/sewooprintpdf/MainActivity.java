package hgode.sewooprintpdf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "SewooPDFprint";
    File pdfFile=null;
    Bitmap pdfImage=null;
    String pdfText=null;
    PDFprint pdFprint;
    String btAddress="00:03:7a:31:9c:3a";

    private static final int REQUEST_ENABLE_BT = 2;
    private static final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "//temp";
    private static final String fileName = dir + "/SewooPrintPDF";
    private String lastConnAddr;

    TextView txtFile;
    Button btnFile;
    TextView txtLog;
    EditText btAddrBox;
    Button btnPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check and request permissions
        PermissionsClass permissions = new PermissionsClass(this);
        permissions.checkPermissions();

        loadSettingFile();

        txtFile=(TextView)findViewById(R.id.txtFile);
        btnFile=(Button)findViewById(R.id.btnFile);
        txtLog=(TextView)findViewById(R.id.txtLog);
        btAddrBox=(EditText)findViewById(R.id.btAddrBox);
        btnPrint=(Button)findViewById(R.id.btnPrint);

        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                btnPrint.setEnabled(false);
                btAddress=btAddrBox.getText().toString();
                String sFile=txtFile.getText().toString();
                launchIntentService(sFile,btAddress, (float)3.0);
            }

        });
        //have we launched by an Intent?
        Uri data = getIntent().getData();
        if (data != null) {
            getIntent().setData(null);
            try {
                pdfFile = new File(data.getPath());
                String fileName = pdfFile.getPath();
                txtFile.setText(fileName);
                Toast.makeText(this, TAG + " " + fileName, Toast.LENGTH_LONG);
                Log.d(TAG, "data=" + data);
                updateStatus("Launched by Intent: "+fileName);
                //start IntentService to convert PDF and print
                float scale=(float)3.0;
                launchIntentService(fileName, btAddress, scale );
            } catch (Exception e) {
                // warn user about bad data here
                Log.d(TAG, "Exception: " + e.getMessage());
                finish();
//                return;
            }
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(CONSTANTS.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, filter);
    }

    public void launchIntentService(String sFile, String sBTaddress, float scale) {
        // Construct our Intent specifying the Service
        Intent i = new Intent(this, myIntentService.class);
        // Add extras to the bundle
        i.putExtra(CONSTANTS.IntentServiceData_Filename, sFile);
        i.putExtra(CONSTANTS.IntentServiceData_BTaddress, sBTaddress);
        i.putExtra(CONSTANTS.IntentServiceData_Scale, scale);
        // Start the service
        updateStatus("Starting Print...");
        startService(i);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        saveSettingFile();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener when the application is paused
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        // or `unregisterReceiver(testReceiver)` for a normal broadcast
    }
    private void loadSettingFile()
    {
        int rin = 0;
        char [] buf = new char[128];
        try
        {
            FileReader fReader = new FileReader(fileName);
            rin = fReader.read(buf);
            if(rin > 0)
            {
                lastConnAddr = new String(buf,0,rin);
                btAddrBox.setText(lastConnAddr);
            }
            fReader.close();
        }
        catch (FileNotFoundException e)
        {
            Log.i(TAG, "Connection history not exists.");
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
    }
    void updateStatus(final String status){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                txtLog.setText(status);
                Log.d(TAG,status);
            }
        });
    }
    private void saveSettingFile()
    {
        try
        {
            File tempDir = new File(dir);
            if(!tempDir.exists())
            {
                tempDir.mkdir();
            }
            FileWriter fWriter = new FileWriter(fileName);
            if(lastConnAddr != null)
                fWriter.write(lastConnAddr);
            fWriter.close();
        }
        catch (FileNotFoundException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    // Define the callback for what to do when data is received
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getIntExtra(CONSTANTS.IntentServiceData_RESULT_BITMAP_OK, RESULT_CANCELED);
            if (resultCode == RESULT_OK) {
                updateStatus("Print completed OK");
                String resultValue = intent.getStringExtra("resultValue");
                Toast.makeText(MainActivity.this, resultValue, Toast.LENGTH_SHORT).show();
            }
            else if(resultCode==RESULT_CANCELED){
                String resultValue = intent.getStringExtra(CONSTANTS.IntentServiceData_RESULT_MESSAGE);
                Toast.makeText(MainActivity.this, resultValue, Toast.LENGTH_LONG).show();
                updateStatus("Print FAILED: "+resultValue);
            }
        }
    };
}
