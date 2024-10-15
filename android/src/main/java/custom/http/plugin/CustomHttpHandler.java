package custom.http.plugin;

import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.plugin.util.HttpRequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class CustomHttpHandler extends HttpRequestHandler {
    /**
     * Makes an Http Request based on the PluginCall parameters
     *
     * @param customHttpPlugin Current Custom Plugin instance
     * @param call             The Capacitor PluginCall that contains the options need for an Http request
     */
    public static void request(CustomHttpPlugin customHttpPlugin, PluginCall call) throws JSONException {
        String urlString = call.getString("url", "");
        JSArray filesArray = call.getArray("files");
        JSObject data = call.getObject("data");
        // Create a ProgressRequestBody from the raw data
        assert filesArray != null;
        assert data != null;
        // Create a MultipartBody builder
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        for (int i = 0; i < filesArray.length(); i++) {
            JSObject file = JSObject.fromJSONObject(filesArray.getJSONObject(i));
            String fileName = file.getString("fileName");
            String base64Data = file.getString("base64Data");
            if (base64Data != null) {; // Get only the base64 part
                byte[] fileBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);

                // Create a request body for the file
                RequestBody fileBody = RequestBody.create(fileBytes, MediaType.parse("application/octet-stream"));

                // Add the file to the multipart request
                multipartBuilder.addFormDataPart("file_" + i, fileName, fileBody); // Change file extension as needed
            }
        }

        // Add additional data to the request
        for (Iterator<String> it = data.keys(); it.hasNext(); ) {
            String key = it.next();
            String value = data.getString(key);
            multipartBuilder.addFormDataPart(key, value);
        }

        // Build the final MultipartBody
        MultipartBody multipartBody = multipartBuilder.build();

        ProgressRequestBody progressRequestBody = new ProgressRequestBody(multipartBody, (bytesWritten, totalBytes, progress) -> {
            Log.i("Upload progress: ", progress + "%");
            JSObject result = new JSObject();
            result.put("type",1);
            result.put("progress",progress);
            customHttpPlugin.sendProgress(result);
        });
        // Create an OkHttpClient instance
        // HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                //.addInterceptor(logging)
                .connectTimeout(300, TimeUnit.SECONDS)  // Custom connection timeout
                .readTimeout(3000, TimeUnit.SECONDS)     // Custom read timeout
                .writeTimeout(3000, TimeUnit.SECONDS)    // Custom write timeout
                .retryOnConnectionFailure(true)
                .build();
        JSObject headers = call.getObject("headers", new JSObject());
        // Create a request
        assert headers != null;
        assert urlString != null;

        Request request = new Request.Builder()
                .url(urlString)
                .addHeader("Authorization", Objects.requireNonNull(headers.getString("Authorization")))
                .post(progressRequestBody)
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call1, IOException e) {
                e.printStackTrace();
                call.reject(e.getLocalizedMessage());
                // Handle the error
            }

            @Override
            public void onResponse(Call call2, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    call.reject(response.body().string());
                }
                String responseBody = response.body().string();
                Log.i("Upload successful: ", responseBody);
                JSObject jsObject  = new JSObject();
                try {
                    JSONObject jsonObject = new JSONObject(responseBody);
                    // Iterate through the keys in the JSONObject
                    for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                        String key = it.next();
                        // Get the value and add it to the JSObject
                        Object value = jsonObject.get(key);
                        jsObject.put(key, value); // Directly put value
                    }
                    call.resolve(jsObject);
                } catch (JSONException e) {
                    call.reject(e.getLocalizedMessage());
                }
            }
        });
    }
}
