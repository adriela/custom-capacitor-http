import Foundation
import Capacitor

struct FileJSon: Codable {
    let fileName: String
    let path: String
}

struct HttpError: Error {
    let message: String
    let code: String
}

@objc public class CustomHttp: NSObject, URLSessionTaskDelegate, URLSessionDataDelegate {
    private var customHttpPlugin: CustomHttpPlugin?
    @objc public func post(_ call: CAPPluginCall,_ chp: CustomHttpPlugin) -> Void {
        self.customHttpPlugin = chp;
        guard let serverUrl = call.getString("url"),
              let datas = call.getObject("body"),
              let headers = call.getObject("options") else {
            call.reject("Invalid parameters")
            return
        }
        
        // Extract file data and filenames
        var files : [FileJSon] = []
        
        for (key, value) in datas {
            if(key == "files"){
                if let jsonData = (value as! String).data(using: .utf8) {
                    do {
                        // Initialize JSONDecoder
                        let decoder = JSONDecoder()
                        // Decode the JSON into the array of FileJSon struct
                        files = try decoder.decode([FileJSon].self, from: jsonData)
                    } catch {
                        print("Failed to decode JSON: \(error.localizedDescription)")
                    }
                }
            }
        }
        
        // Prepare the request
        guard let url = URL(string: serverUrl) else {
            call.reject("Invalid server URL")
            return
        }
        
        let request = createMultipartRequest(url: url, files: files, parameterName: "file_", extraParameters: datas, authorization: headers["Authorization"] as! String)

        // Set up the session
        let session = URLSession(configuration: .default, delegate: self, delegateQueue: OperationQueue.main)
        let uploadTask = session.uploadTask(with: request, from: request.httpBody) { data, response, error in
            if let error = error {
                self.rejectAll(call, msg: error.localizedDescription)
                return
            }
            guard let response = response as? HTTPURLResponse, response.statusCode == 200 else {
                if let stringResponse = String(data: data!, encoding: .utf8) {
                    self.rejectAll(call, msg: stringResponse)
                } else {
                    self.rejectAll(call, msg: "Failed to convert error data to String")
                }
                return
            }
            do {
                let jsonObject = try JSONSerialization.jsonObject(with: data!, options: [])
                if let dictionary = jsonObject as? [String: Any] {
                    call.resolve(dictionary)
                }
            } catch {
                print("Failed to parse JSON data: \(error.localizedDescription)")
                call.resolve(["message":"uploaded but maybe server error"])
            }
            for file in files {
                let fileURL = URL(string: file.path)
                self.deleteFile(at: fileURL!)
            }
        }
        
        // Start upload
        uploadTask.resume()
    }
    
    private func rejectAll(_ call: CAPPluginCall, msg: String){
        var ret : [String: Any] = [:]
        guard let serverUrl = call.getString("url"),
              let datas = call.getObject("body"),
              let headers = call.getObject("options") else {
            call.reject("Invalid parameters")
            return
        }
        // Extract file data and filenames
        var files : [FileJSon] = []
        var pFiles: [Any] = []
        var params : [String: Any] = [:]
        for (key, value) in datas {
            if(key == "files"){
                if let jsonData = (value as! String).data(using: .utf8) {
                    do {
                        // Initialize JSONDecoder
                        let decoder = JSONDecoder()
                        // Decode the JSON into the array of FileJSon struct
                        files = try decoder.decode([FileJSon].self, from: jsonData)
                        for file in files{
                            pFiles.append(["fileName":file.fileName, "path":file.path])
                        }
                    } catch {
                        print("Failed to decode JSON: \(error.localizedDescription)")
                    }
                }
            } else {
                params[key]=value
            }
        }
        ret["url"]=serverUrl
        ret["files"]=pFiles
        ret["params"]=params
        call.reject(msg,"501",HttpError(message: msg, code: "501"),ret)
    }
    
    private func deleteFile(at fileURL: URL) {
        let fileManager = FileManager.default
        
        do {
            try fileManager.removeItem(at: fileURL)
            print("File successfully deleted: \(fileURL)")
        } catch {
            print("Failed to delete file: \(error.localizedDescription)")
        }
    }
    
    // Create a multipart request
    private func createMultipartRequest(url: URL, files: [FileJSon], parameterName: String, extraParameters: JSObject, authorization: String) -> URLRequest {
            var request = URLRequest(url: url)
            request.httpMethod = "POST"

            request.setValue(authorization, forHTTPHeaderField: "Authorization")
            
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
            var i = 0;
            for file in files {
                let fileURL = URL(string: file.path)
                let fileName = file.fileName
                let fileData = try? Data(contentsOf: fileURL!)
                body.append("--\(boundary)\r\n".data(using: .utf8)!)
                body.append("Content-Disposition: form-data; name=\"\(parameterName)\(i)\"; filename=\"\(fileName)\"\r\n".data(using: .utf8)!)
                body.append("Content-Type: application/octet-stream\r\n\r\n".data(using: .utf8)!)
                body.append(fileData!)
                body.append("\r\n".data(using: .utf8)!)
                i = i + 1
            }
            
            body.append("--\(boundary)--\r\n".data(using: .utf8)!)
            request.httpBody = body
            
            return request
        }
        
        // Progress tracking delegate
        public func urlSession(_ session: URLSession, task: URLSessionTask, didSendBodyData bytesSent: Int64, totalBytesSent: Int64, totalBytesExpectedToSend: Int64) {
            let progress = Double(totalBytesSent) / Double(totalBytesExpectedToSend)
            var data : [String: Any] = [:]
            data["type"]=1
            data["loaded"]=totalBytesSent
            data["total"]=totalBytesExpectedToSend
            print("upload progress \(progress) %")
            self.customHttpPlugin?.notifyListeners("progressUpdate", data: data)
        }
}
