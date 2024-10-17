package custom.http.plugin;

import static custom.http.plugin.CustomHttpHandler.request;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginConfig;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Objects;

@CapacitorPlugin(name = "CustomHttp")
public class CustomHttpPlugin extends Plugin {
    public void sendProgress(JSObject data){
        notifyListeners("progressUpdate", data);
    }
    private void http(final PluginCall call) {
        //throw new Exception("error test");
        request(this,call);
    }
    @PluginMethod
    public void post(final PluginCall call) {
        this.http(call);
    }
}
