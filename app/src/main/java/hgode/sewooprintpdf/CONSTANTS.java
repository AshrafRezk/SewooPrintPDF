package hgode.sewooprintpdf;

/**
 * Created by E841719 on 24.11.2017.
 */

public class CONSTANTS {
    public static final String ACTION = "myIntentService";

    public static String IntentServiceData_Filename = "IntentServiceData_Filename";
    public static String IntentServiceData_BTaddress = "IntentServiceData_BTaddress";
    public static String IntentServiceData_Scale = "IntentServiceData_Scale";

    //launched by IntentService to inform main thread
    public static String IntentServiceData_RESULT_MESSAGE = "IntentServiceData_RESULT_MESSAGE";
    public static String IntentServiceData_RESULT_TEXT_OK = "IntentServiceData_RESULT_TEXT_OK";
    public static String IntentServiceData_RESULT_BITMAP_OK = "IntentServiceData_RESULT_BITMAP_OK";

    //transmit the file name
    public static String IntentServiceData_RESULT_TEXT_FILE = "IntentServiceData_RESULT_TEXT_FILE";
    public static String IntentServiceData_RESULT_BITMAP_FILE = "IntentServiceData_RESULT_BITMAP_FILE";
}
