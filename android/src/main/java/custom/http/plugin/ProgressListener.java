package custom.http.plugin;

public interface ProgressListener {
    void onProgress(long bytesWritten, long totalBytes, float progress);
}
