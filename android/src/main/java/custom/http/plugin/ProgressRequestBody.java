package custom.http.plugin;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

import java.io.IOException;

public class ProgressRequestBody extends RequestBody {

    private final RequestBody requestBody;
    private final ProgressListener progressListener;
    private long totalBytesWritten = 0L;

    public ProgressRequestBody(RequestBody requestBody, ProgressListener progressListener) {
        this.requestBody = requestBody;
        this.progressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long contentLength = contentLength(); // Get the actual content length of the request
        CountingSink countingSink = new CountingSink(sink, contentLength);
        BufferedSink bufferedSink = Okio.buffer(countingSink);

        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    private class CountingSink extends ForwardingSink {

        private final long contentLength;

        public CountingSink(Sink delegate, long contentLength) {
            super(delegate);
            this.contentLength = contentLength;
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);

            // Increment the number of total bytes written so far
            totalBytesWritten += byteCount;

            // Avoid division by zero and ensure progress never exceeds 100%
            if (contentLength > 0) {
                // Calculate progress as a percentage (ensure it does not go above 100%)
                float progress = Math.min((float) totalBytesWritten / contentLength * 100, 100);

                // Send progress to the listener
                progressListener.onProgress(totalBytesWritten, contentLength, progress);
            }
        }
    }
}
