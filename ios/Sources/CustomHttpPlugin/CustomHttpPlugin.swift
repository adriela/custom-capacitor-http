import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(CustomHttpPlugin)
public class CustomHttpPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "CustomHttpPlugin"
    public let jsName = "CustomHttp"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "post", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = CustomHttp()

    @objc func post(_ call: CAPPluginCall) {
        implementation.post(call, self)
    }
}
