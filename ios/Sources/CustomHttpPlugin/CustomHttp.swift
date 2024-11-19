import Foundation
import Capacitor

@objc public class CustomHttp: NSObject, URLSessionTaskDelegate, URLSessionDataDelegate {
    private var progressCallbackId: String?
    private var customHttpPlugin: CustomHttpPlugin?
    @objc public func post(_ call: CAPPluginCall,_ chp: CustomHttpPlugin) -> Void {
        self.customHttpPlugin = chp;
        guard let serverUrl = call.getString("url"),
              let extra = call.getObject("body"),
              let files = call.getArray("files", JSObject.self) else {
            call.reject("Invalid parameters")
            return
        }
        
        // Extract file data and filenames
        var fileDataArray: [Data] = []
        var fileNames: [String] = []
        
        for file in files {
            guard let data = file["base64Files"] as? String,
                  let name = file["fileName"] as? String,
                  let fileData = Data(base64Encoded: data) else {
                call.reject("Invalid file data")
                return
            }
            fileDataArray.append(fileData)
            fileNames.append(name)
        }
        
        // Prepare the request
        guard let url = URL(string: serverUrl) else {
            call.reject("Invalid server URL")
            return
        }
        
        let request = createMultipartRequest(url: url, files: fileDataArray, fileNames: fileNames, parameterName: "files", extraParameters: extra)
        
        // Set up the session
        let session = URLSession(configuration: .default, delegate: self, delegateQueue: OperationQueue.main)
        let uploadTask = session.uploadTask(with: request, from: request.httpBody) { data, response, error in
            if let error = error {
                call.reject("Upload failed: \(error.localizedDescription)")
                return
            }
            guard let response = response as? HTTPURLResponse, response.statusCode == 200 else {
                call.reject("Upload failed with server error")
                return
            }
            call.resolve(["message": "Files uploaded successfully"])
        }
        
        // Start upload
        uploadTask.resume()
    }
    
    // Create a multipart request
    private func createMultipartRequest(url: URL, files: [Data], fileNames: [String], parameterName: String, extraParameters: JSObject) -> URLRequest {
            var request = URLRequest(url: url)
            request.httpMethod = "POST"
            
            let boundary = UUID().uuidString
            request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
            
            var body = Data()
            
            for (key, value) in extraParameters {
                if(key == "files"){
                    continue
                }
                body.append("--\(boundary)\r\n".data(using: .utf8)!)
                body.append("Content-Disposition: form-data; name=\"\(key)\"\r\n\r\n".data(using: .utf8)!)
                body.append("\(value)\r\n".data(using: .utf8)!)
            }
            
            for (index, fileData) in files.enumerated() {
                body.append("--\(boundary)\r\n".data(using: .utf8)!)
                body.append("Content-Disposition: form-data; name=\"\(parameterName)\"; filename=\"\(fileNames[index])\"\r\n".data(using: .utf8)!)
                body.append("Content-Type: application/octet-stream\r\n\r\n".data(using: .utf8)!)
                body.append(fileData)
                body.append("\r\n".data(using: .utf8)!)
            }
            
            body.append("--\(boundary)--\r\n".data(using: .utf8)!)
            request.httpBody = body
            
            return request
        }
        
        // Progress tracking delegate
        public func urlSession(_ session: URLSession, task: URLSessionTask, didSendBodyData bytesSent: Int64, totalBytesSent: Int64, totalBytesExpectedToSend: Int64) {
            let progress = Double(totalBytesSent) / Double(totalBytesExpectedToSend)
            if let progressCallbackId = progressCallbackId {
                self.customHttpPlugin?.notifyListeners(progressCallbackId, data: ["progressUpdate": progress])
            }
        }
}
