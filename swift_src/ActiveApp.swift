import Cocoa

let runningApps = NSWorkspace.shared().runningApplications as [NSRunningApplication]
for app in runningApps {
    if app.isActive {
        print(app.localizedName!)
    }
}
