// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CustomHttpPlugin",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "CustomHttpPlugin",
            targets: ["CustomHttpPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main")
    ],
    targets: [
        .target(
            name: "CustomHttpPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/CustomHttpPlugin"),
        .testTarget(
            name: "CustomHttpPluginTests",
            dependencies: ["CustomHttpPlugin"],
            path: "ios/Tests/CustomHttpPluginTests")
    ]
)