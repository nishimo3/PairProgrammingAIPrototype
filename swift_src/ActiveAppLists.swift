import Cocoa

let runningApps = NSWorkspace.shared().runningApplications as [NSRunningApplication]
let regularApps = runningApps.filter {
    $0.activationPolicy == NSApplicationActivationPolicy.regular
}

let appNames = regularApps.map { $0.localizedName! }

for app in appNames {
    print(app)
}


