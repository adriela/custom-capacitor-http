package custom.http.plugin;

import static custom.http.plugin.CustomHttpHandler.request;

import android.webkit.JavascriptInterface;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginConfig;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "CustomHttp")
public class CustomHttpPlugin extends Plugin {
    public void sendProgress(JSObject data){
        notifyListeners("progressUpdate", data);
    }
    private void http(final PluginCall call) {
        try {
            request(this,call);
        } catch (Exception e) {
            call.reject(e.getLocalizedMessage(), e.getClass().getSimpleName(), e);
        }
    }
    @PluginMethod
    public void post(final PluginCall call) {
        this.http(call);
    }
}
