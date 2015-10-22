package diego.bezerra.com.touchcall;

import android.util.Log;

/**
 * Created by diegobezerrasouza on 20/02/15.
 */
public class LogApp {

    private static final String LOG_TAG = "touchCallTagId";

    public static void i(String value) {
        Log.i(LOG_TAG, value);
    }
}
