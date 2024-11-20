package custom.http.plugin;

import android.os.Build;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.plugin.util.HttpRequestHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
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
    public static void request(CustomHttpPlugin customHttpPlugin, PluginCall call) {
        String urlString = call.getString("url", "");
        JSObject data = call.getObject("body");
        String filesJson = data.getString("files");
        JSONArray filesArray = null;
        try {
            filesArray = new JSONArray(filesJson);
        } catch (JSONException e) {
            CustomHttpHandler.rejectCallWithData(call, e.getLocalizedMessage());
        }
        // Create a MultipartBody builder
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        for (int i = 0; i < filesArray.length(); i++) {
            JSObject file = null;
            try {
                file = JSObject.fromJSONObject(filesArray.getJSONObject(i));
            } catch (JSONException e) {
                call.reject("json_error", e.getLocalizedMessage());
            }
            String fileName = file.getString("fileName");
            String path = file.getString("path");
            File f = new File(path.replace("file://", ""));
            if (f != null) {
                try{
                    byte[] fileBytes = Files.readAllBytes(f.toPath());
                    // Create a request body for the file
                    RequestBody fileBody = RequestBody.create(fileBytes, MediaType.parse("application/octet-stream"));

                    // Add the file to the multipart request
                    multipartBuilder.addFormDataPart("file_" + i, fileName, fileBody); // Change file extension as needed
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // Add additional data to the request
        for (Iterator<String> it = data.keys(); it.hasNext(); ) {
            String key = it.next();
            if(!Objects.equals(key, "files")) {
                String value = data.getString(key);
                multipartBuilder.addFormDataPart(key, value);
            }
        }

        // Build the final MultipartBody
        MultipartBody multipartBody = multipartBuilder.build();

        ProgressRequestBody progressRequestBody = new ProgressRequestBody(multipartBody, (bytesWritten, totalBytes, progress) -> {
            Log.i("Upload progress: ", progress + "%");
            JSObject result = new JSObject();
            result.put("type",1);
            result.put("loaded",bytesWritten);
            result.put("total",totalBytes);
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
        JSObject headers = call.getObject("options", new JSObject());
        // Create a request
        assert headers != null;
        assert urlString != null;

        Request request = new Request.Builder()
                .url(urlString)
                .addHeader("Authorization", Objects.requireNonNull(headers.getString("Authorization")))
                .post(progressRequestBody)
                .build();

        // Execute the request asynchronously
        JSONArray finalFilesArray = filesArray;
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call1, IOException e) {
                e.printStackTrace();
                CustomHttpHandler.rejectCallWithData(call, e.getLocalizedMessage());
                // Handle the error
            }

            @Override
            public void onResponse(Call call2, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    CustomHttpHandler.rejectCallWithData(call, "error_500_server");
                }
                if(response.code() == 500){
                    CustomHttpHandler.rejectCallWithData(call, "error_500_server");
                }
                String responseBody = response.body().string();
                Log.i("Upload successful: ", responseBody);
                for (int i = 0; i < finalFilesArray.length(); i++) {
                    JSObject file = null;
                    try {
                        file = JSObject.fromJSONObject(finalFilesArray.getJSONObject(i));
                    } catch (JSONException e) {
                        call.reject("json_error", e.getLocalizedMessage());
                    }
                    String path = file.getString("path");
                    if(deleteFile(path.replace("file://", ""))){
                        Log.i("File deleted : ", path);
                    }
                }
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
                    CustomHttpHandler.rejectCallWithData(call, e.getLocalizedMessage());
                }
            }
        });
    }

    private static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete(); // Deletes the file and returns true if successful
        } else {
            Log.i("Upload File not found at: : ", filePath);
            return false;
        }
    }

    private static void rejectCallWithData(PluginCall call, String message){
        String urlString = call.getString("url", "");
        JSObject data = call.getObject("body");
        String filesJson = data.getString("files");
        JSONArray filesArray = null;
        try {
            filesArray = new JSONArray(filesJson);
        } catch (JSONException ex) {
            Log.e("json_error", ex.getLocalizedMessage());
            call.reject("json_error", ex.getLocalizedMessage());
        }
        JSONArray dataArray = new JSONArray();
        int i = 0;
        for (Iterator<String> it = data.keys(); it.hasNext(); ) {
            String key = it.next();
            if(!Objects.equals(key, "files")) {
                JSObject obj = new JSObject();
                String value = data.getString(key);
                try {
                    obj.put(key,value);
                    dataArray.put(i, obj);
                } catch (JSONException ex) {
                    Log.e("json_error", ex.getLocalizedMessage());
                    call.reject("json_error", ex.getLocalizedMessage());
                }
                i++;
            }
        }
        JSObject errorData = new JSObject();
        errorData.put("url", urlString);
        errorData.put("files", filesArray);
        errorData.put("params", dataArray   );
        call.reject(message, errorData);
    }
}