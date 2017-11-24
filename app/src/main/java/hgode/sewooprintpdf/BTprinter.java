package hgode.sewooprintpdf;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.sewoo.jpos.command.CPCLConst;
import com.sewoo.jpos.printer.CPCLPrinter;
import com.sewoo.port.android.BluetoothPort;
import com.sewoo.request.android.RequestHandler;

import java.io.IOException;

import javax.security.auth.Destroyable;

/**
 * Created by E841719 on 24.11.2017.
 */

public class BTprinter {
    final static  String TAG="BTprinter";
    private BluetoothPort bluetoothPort;
    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothAdapter mBluetoothAdapter;
    protected CPCLPrinter cpclPrinter;
    private Thread hThread;

    public  BTprinter(){
        updateStatus("BTprinter()...");
        bluetoothPort = BluetoothPort.getInstance();
        updateStatus("bluetoothPort initialized");
        if(cpclPrinter==null)
            cpclPrinter=new CPCLPrinter();
        updateStatus("new CPCLPrinter created");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
        {
            // Device does not support Bluetooth
            updateStatus("BluetoothAdapter.getDefaultAdapter() FAILED");
            return;
        }
/*
        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            Activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
*/
    }

    public boolean doPrint(Bitmap pageImage, String sBTaddr){
        boolean bRet=false;
        updateStatus("doPrint started with BT: "+sBTaddr);
        try {
            updateStatus("bt.connect()...");
            bluetoothPort.connect(sBTaddr);
            RequestHandler rh = new RequestHandler();
            hThread = new Thread(rh);
            hThread.start();
            //...
            updateStatus("CPCLprinter start form");
            int paperType = CPCLConst.LK_CPCL_CONTINUOUS;
            int lblHeigth=pageImage.getHeight()+10;
            int count=1;
            cpclPrinter.setForm(0, 200, 200, lblHeigth, count);
            cpclPrinter.setMedia(paperType);
            try {
                updateStatus("CPCLprinter start printBitmap");
                cpclPrinter.printBitmap(pageImage,1,1);
                cpclPrinter.printForm();
                updateStatus("printBitmap done");
            }catch (Exception ex){
                updateStatus("PrintBitmap FAILED: "+ex.getMessage());
            }
            bluetoothPort.disconnect();
        }catch(IOException ex){
            updateStatus("IOException: "+ex.getMessage());
        }
        catch (InterruptedException ex){
            updateStatus("InterruptedException: "+ex.getMessage());
        }
        return  bRet;
    }


    @Override
    protected void finalize() throws Throwable {
        updateStatus("Finalize");
        super.finalize();
        this.Close();
    }

    void Close()
    {
        updateStatus("Close()");
        try
        {

            if(bluetoothPort.isConnected() == true)
                bluetoothPort.disconnect();
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        catch (InterruptedException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        if((hThread != null) && (hThread.isAlive()))
        {
            hThread.interrupt();
            hThread = null;
        }
    }
    void updateStatus(String s){
        Log.d(TAG,s);
    }

}
