package custom.http.plugin;

import android.util.Log;

public class CustomHttp {

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }
}
