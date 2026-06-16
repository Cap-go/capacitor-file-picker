import AVFoundation
import Capacitor
import Foundation
import MobileCoreServices
import PhotosUI
import UniformTypeIdentifiers

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(CapgoFilePickerPlugin)
public class CapgoFilePickerPlugin: CAPPlugin, CAPBridgedPlugin {
    private let pluginVersion: String = "8.1.11"
    public let identifier = "CapgoFilePickerPlugin"
    public let jsName = "CapgoFilePicker"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "pickFiles", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "pickImages", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "pickVideos", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "pickMedia", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "pickDirectory", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "convertHeicToJpeg", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "copyFile", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "checkPermissions", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "requestPermissions", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getPluginVersion", returnType: CAPPluginReturnPromise)
    ]

    private var pendingCall: CAPPluginCall?
    private var readData: Bool = false
    private var skipTranscoding: Bool = false

    // MARK: - Pick Files

    @objc func pickFiles(_ call: CAPPluginCall) {
        let types = call.getArray("types", String.self) ?? []
        let limit = call.getInt("limit") ?? 0
        readData = call.getBool("readData") ?? false

        DispatchQueue.main.async {
            self.pendingCall = call
            let documentTypes = self.convertToUTTypes(from: types)
            let picker = UIDocumentPickerViewController(forOpeningContentTypes: documentTypes)
            picker.delegate = self
            picker.allowsMultipleSelection = limit != 1
            self.bridge?.viewController?.present(picker, animated: true)
        }
    }

    // MARK: - Pick Images

    @objc func pickImages(_ call: CAPPluginCall) {
        pickMedia(call, filter: .images)
    }

    // MARK: - Pick Videos

    @objc func pickVideos(_ call: CAPPluginCall) {
        pickMedia(call, filter: .videos)
    }

    // MARK: - Pick Media

    @objc func pickMedia(_ call: CAPPluginCall) {
        pickMedia(call, filter: nil)
    }

    private func pickMedia(_ call: CAPPluginCall, filter: PHPickerFilter?) {
        let limit = call.getInt("limit") ?? 0
        readData = call.getBool("readData") ?? false
        skipTranscoding = call.getBool("skipTranscoding") ?? false
        let ordered = call.getBool("ordered") ?? false

        DispatchQueue.main.async {
            self.pendingCall = call
            var config = PHPickerConfiguration(photoLibrary: .shared())
            config.selectionLimit = limit
            if let filter = filter {
                config.filter = filter
            } else {
                config.filter = .any(of: [.images, .videos])
            }
            if #available(iOS 15.0, *), ordered {
                config.selection = .ordered
            }
            let picker = PHPickerViewController(configuration: config)
            picker.delegate = self
            self.bridge?.viewController?.present(picker, animated: true)
        }
    }

    // MARK: - Pick Directory

    @objc func pickDirectory(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.pendingCall = call
            let picker = UIDocumentPickerViewController(forOpeningContentTypes: [.folder])
            picker.delegate = self
            self.bridge?.viewController?.present(picker, animated: true)
        }
    }

    // MARK: - Convert HEIC to JPEG

    @objc func convertHeicToJpeg(_ call: CAPPluginCall) {
        guard let path = call.getString("path") else {
            call.reject("path is required")
            return
        }
        let quality = call.getFloat("quality") ?? 0.9

        DispatchQueue.global(qos: .userInitiated).async {
            let url = URL(fileURLWithPath: path)
            guard let imageSource = CGImageSourceCreateWithURL(url as CFURL, nil),
                  let cgImage = CGImageSourceCreateImageAtIndex(imageSource, 0, nil) else {
                call.reject("Failed to load image")
                return
            }

            let uiImage = UIImage(cgImage: cgImage)
            guard let jpegData = uiImage.jpegData(compressionQuality: CGFloat(quality)) else {
                call.reject("Failed to convert to JPEG")
                return
            }

            let outputPath = path.replacingOccurrences(of: ".heic", with: ".jpg", options: .caseInsensitive)
            let outputURL = URL(fileURLWithPath: outputPath)

            do {
                try jpegData.write(to: outputURL)
                call.resolve(["path": outputPath])
            } catch {
                call.reject("Failed to write JPEG: \(error.localizedDescription)")
            }
        }
    }

    // MARK: - Copy File

    @objc func copyFile(_ call: CAPPluginCall) {
        guard let from = call.getString("from"), let to = call.getString("to") else {
            call.reject("from and to are required")
            return
        }
        let overwrite = call.getBool("overwrite") ?? false

        DispatchQueue.global(qos: .userInitiated).async {
            let fileManager = FileManager.default
            let fromURL = URL(fileURLWithPath: from)
            let toURL = URL(fileURLWithPath: to)

            do {
                if fileManager.fileExists(atPath: to) {
                    if overwrite {
                        try fileManager.removeItem(at: toURL)
                    } else {
                        call.reject("Destination file already exists")
                        return
                    }
                }
                try fileManager.copyItem(at: fromURL, to: toURL)
                call.resolve()
            } catch {
                call.reject("Failed to copy file: \(error.localizedDescription)")
            }
        }
    }

    // MARK: - Permissions

    override public func checkPermissions(_ call: CAPPluginCall) {
        let status = PHPhotoLibrary.authorizationStatus(for: .readWrite)
        call.resolve(["readExternalStorage": permissionStateString(from: status)])
    }

    override public func requestPermissions(_ call: CAPPluginCall) {
        PHPhotoLibrary.requestAuthorization(for: .readWrite) { status in
            call.resolve(["readExternalStorage": self.permissionStateString(from: status)])
        }
    }

    private func permissionStateString(from status: PHAuthorizationStatus) -> String {
        switch status {
        case .authorized, .limited:
            return "granted"
        case .denied, .restricted:
            return "denied"
        case .notDetermined:
            return "prompt"
        @unknown default:
            return "prompt"
        }
    }

    // MARK: - Plugin Version

    @objc func getPluginVersion(_ call: CAPPluginCall) {
        call.resolve(["version": self.pluginVersion])
    }

    // MARK: - Helpers

    private func convertToUTTypes(from types: [String]) -> [UTType] {
        if types.isEmpty {
            return [.item]
        }
        var utTypes: [UTType] = []
        for type in types {
            if type == "*/*" {
                return [.item]
            } else if type.hasSuffix("/*") {
                let prefix = String(type.dropLast(2))
                switch prefix {
                case "image":
                    utTypes.append(.image)
                case "video":
                    utTypes.append(.movie)
                case "audio":
                    utTypes.append(.audio)
                case "text":
                    utTypes.append(.text)
                default:
                    utTypes.append(.item)
                }
            } else if type.hasPrefix(".") {
                if let utType = UTType(filenameExtension: String(type.dropFirst())) {
                    utTypes.append(utType)
                }
            } else if let utType = UTType(mimeType: type) {
                utTypes.append(utType)
            }
        }
        return utTypes.isEmpty ? [.item] : utTypes
    }

    private func getFileInfo(from url: URL, readData: Bool) -> [String: Any] {
        var result: [String: Any] = [
            "name": url.lastPathComponent,
            "path": url.path
        ]

        if let mimeType = UTType(filenameExtension: url.pathExtension)?.preferredMIMEType {
            result["mimeType"] = mimeType
        } else {
            result["mimeType"] = "application/octet-stream"
        }

        if let attributes = try? FileManager.default.attributesOfItem(atPath: url.path) {
            if let size = attributes[.size] as? Int64 {
                result["size"] = size
            }
            if let modifiedDate = attributes[.modificationDate] as? Date {
                result["modifiedAt"] = Int64(modifiedDate.timeIntervalSince1970 * 1000)
            }
        }

        if readData, let data = try? Data(contentsOf: url) {
            result["data"] = data.base64EncodedString()
        }

        // Get dimensions for images
        if let mimeType = result["mimeType"] as? String, mimeType.hasPrefix("image/") {
            if let imageSource = CGImageSourceCreateWithURL(url as CFURL, nil),
               let properties = CGImageSourceCopyPropertiesAtIndex(imageSource, 0, nil) as? [String: Any] {
                if let width = properties[kCGImagePropertyPixelWidth as String] as? Int {
                    result["width"] = width
                }
                if let height = properties[kCGImagePropertyPixelHeight as String] as? Int {
                    result["height"] = height
                }
            }
        }

        return result
    }
}

// MARK: - UIDocumentPickerDelegate

extension CapgoFilePickerPlugin: UIDocumentPickerDelegate {
    public func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        guard let call = pendingCall else { return }
        pendingCall = nil

        // Check if this was a directory picker
        if urls.count == 1, urls[0].hasDirectoryPath {
            let granted = urls[0].startAccessingSecurityScopedResource()
            if granted {
                call.resolve(["path": urls[0].path])
                urls[0].stopAccessingSecurityScopedResource()
            } else {
                call.resolve(["path": urls[0].path])
            }
            return
        }

        var files: [[String: Any]] = []
        for url in urls {
            let granted = url.startAccessingSecurityScopedResource()
            defer { if granted { url.stopAccessingSecurityScopedResource() } }
            files.append(getFileInfo(from: url, readData: readData))
        }
        call.resolve(["files": files])
    }

    public func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
        guard let call = pendingCall else { return }
        pendingCall = nil
        notifyListeners("pickerDismissed", data: [:])
        call.resolve(["files": []])
    }
}

// MARK: - PHPickerViewControllerDelegate

extension CapgoFilePickerPlugin: PHPickerViewControllerDelegate {
    public func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        picker.dismiss(animated: true)

        guard let call = pendingCall else { return }

        if results.isEmpty {
            pendingCall = nil
            notifyListeners("pickerDismissed", data: [:])
            call.resolve(["files": []])
            return
        }

        let group = DispatchGroup()
        var files: [[String: Any]] = []
        let filesLock = NSLock()

        for result in results {
            group.enter()

            let itemProvider = result.itemProvider

            // Determine the UTType to load
            let typeIdentifier: String
            if itemProvider.hasItemConformingToTypeIdentifier(UTType.movie.identifier) {
                typeIdentifier = UTType.movie.identifier
            } else if itemProvider.hasItemConformingToTypeIdentifier(UTType.image.identifier) {
                typeIdentifier = UTType.image.identifier
            } else {
                typeIdentifier = itemProvider.registeredTypeIdentifiers.first ?? UTType.data.identifier
            }

            itemProvider.loadFileRepresentation(forTypeIdentifier: typeIdentifier) { [weak self] url, _ in
                guard let self = self, let url = url else {
                    group.leave()
                    return
                }

                // Copy to temp directory to keep the file accessible
                let tempDir = FileManager.default.temporaryDirectory
                let tempURL = tempDir.appendingPathComponent(UUID().uuidString + "_" + url.lastPathComponent)

                do {
                    try FileManager.default.copyItem(at: url, to: tempURL)
                    var fileInfo = self.getFileInfo(from: tempURL, readData: self.readData)

                    // Get video duration if applicable
                    if let mimeType = fileInfo["mimeType"] as? String, mimeType.hasPrefix("video/") {
                        let asset = AVURLAsset(url: tempURL)
                        let duration = CMTimeGetSeconds(asset.duration)
                        if duration.isFinite {
                            fileInfo["duration"] = duration
                        }
                        // Get video dimensions
                        if let track = asset.tracks(withMediaType: .video).first {
                            let size = track.naturalSize.applying(track.preferredTransform)
                            fileInfo["width"] = abs(Int(size.width))
                            fileInfo["height"] = abs(Int(size.height))
                        }
                    }

                    filesLock.lock()
                    files.append(fileInfo)
                    filesLock.unlock()
                } catch {
                    // Silently fail for individual files
                }
                group.leave()
            }
        }

        group.notify(queue: .main) { [weak self] in
            self?.pendingCall = nil
            call.resolve(["files": files])
        }
    }
}
