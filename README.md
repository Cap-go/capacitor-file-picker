# @capgo/capacitor-file-picker
<a href="https://capgo.app/"><img src="https://capgo.app/readme-banner.svg?repo=Cap-go/capacitor-file-picker" alt="Capgo - Instant updates for Capacitor" /></a>

<div align="center">
  <h2><a href="https://capgo.app/?ref=plugin_file_picker"> ➡️ Get Instant updates for your App with Capgo</a></h2>
  <h2><a href="https://capgo.app/consulting/?ref=plugin_file_picker"> Missing a feature? We'll build the plugin for you 💪</a></h2>
</div>

File picker Capacitor plugin - Pick files, images, videos, and directories

## Why Capacitor File Picker?

A comprehensive file picker plugin with **full native support** for iOS and Android:

- **Pick Files** - Select any file type with MIME type filtering
- **Pick Images** - Native photo library picker with multi-select
- **Pick Videos** - Native video picker with duration and dimensions
- **Pick Media** - Combined image and video picker
- **Pick Directory** - Select folders (Android/iOS)
- **HEIC to JPEG** - Convert HEIC images to JPEG (iOS only)
- **Copy Files** - Copy files to new locations
- **File Metadata** - Get size, dimensions, duration, and more


## Documentation

The most complete doc is available here: https://capgo.app/docs/plugins/file-picker/

## Compatibility

| Plugin version | Capacitor compatibility | Maintained |
| -------------- | ----------------------- | ---------- |
| v8.\*.\*       | v8.\*.\*                | ✅          |
| v7.\*.\*       | v7.\*.\*                | On demand   |
| v6.\*.\*       | v6.\*.\*                | ❌          |
| v5.\*.\*       | v5.\*.\*                | ❌          |

> **Note:** The major version of this plugin follows the major version of Capacitor. Use the version that matches your Capacitor installation (e.g., plugin v8 for Capacitor 8). Only the latest major version is actively maintained.

## Install

You can use our AI-Assisted Setup to install the plugin. Add the Capgo skills to your AI tool using the following command:

```bash
npx skills add https://github.com/cap-go/capacitor-skills --skill capacitor-plugins
```

Then use the following prompt:

```text
Use the `capacitor-plugins` skill from `cap-go/capacitor-skills` to install the `@capgo/capacitor-file-picker` plugin in my project.
```

If you prefer Manual Setup, install the plugin by running the following commands and follow the platform-specific instructions below:

```bash
npm install @capgo/capacitor-file-picker
npx cap sync
```

## Requirements

- iOS: iOS 15.0+
- Android: API 24+ (Android 7.0+)
- Web: Modern browsers with File API support

## Android media permissions and Google Play review

This plugin is designed around user-selected picker flows such as `pickFiles()`, `pickImages()`, `pickVideos()`, `pickMedia()`, and `pickDirectory()`. For one-time or infrequent file, image, or video selection, use these picker methods and avoid adding broad media permissions to your app manifest.

Google Play only allows `READ_MEDIA_IMAGES` and `READ_MEDIA_VIDEO` when photo or video access is directly related to your app's core purpose and picker alternatives are not sufficient. Do not request or retain these permissions only to let a user choose files, images, or videos.

Before publishing an app that declares these permissions:

- Make sure broad photo or video library access is required for the app's core functionality.
- Be ready to justify why Android Photo Picker or this plugin's picker methods are not sufficient.
- Remove broad media permissions if the app can work with user-selected files only.

If Google Play review rejects the app with a message like `Photo and Video Permissions policy: Permission use is not directly related to your app's core purpose`, remove the broad media permissions and rely on this plugin's picker APIs instead.

For apps coming from `@capgo/capacitor-file`, a custom media browser, or another broad-storage flow, the recommended alternative is `@capgo/capacitor-file-picker` with `pickImages()`, `pickVideos()`, `pickMedia()`, or `pickFiles()`.

See Google's [Photo and Video Permissions policy](https://support.google.com/googleplay/android-developer/answer/14115180) for the current review requirements.

## API

<docgen-index>

* [`pickFiles(...)`](#pickfiles)
* [`pickImages(...)`](#pickimages)
* [`pickVideos(...)`](#pickvideos)
* [`pickMedia(...)`](#pickmedia)
* [`pickDirectory()`](#pickdirectory)
* [`convertHeicToJpeg(...)`](#convertheictojpeg)
* [`copyFile(...)`](#copyfile)
* [`checkPermissions()`](#checkpermissions)
* [`requestPermissions()`](#requestpermissions)
* [`addListener('pickerDismissed', ...)`](#addlistenerpickerdismissed-)
* [`removeAllListeners()`](#removealllisteners)
* [`getPluginVersion()`](#getpluginversion)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

Capacitor File Picker Plugin interface for selecting files, images, videos, and directories.

### pickFiles(...)

```typescript
pickFiles(options?: PickFilesOptions | undefined) => Promise<PickFilesResult>
```

Pick one or more files from the device.

| Param         | Type                                                          | Description                 |
| ------------- | ------------------------------------------------------------- | --------------------------- |
| **`options`** | <code><a href="#pickfilesoptions">PickFilesOptions</a></code> | - Options for picking files |

**Returns:** <code>Promise&lt;<a href="#pickfilesresult">PickFilesResult</a>&gt;</code>

**Since:** 8.0.0

--------------------


### pickImages(...)

```typescript
pickImages(options?: PickMediaOptions | undefined) => Promise<PickFilesResult>
```

Pick one or more images from the gallery.
Android/iOS only.

| Param         | Type                                                          | Description                  |
| ------------- | ------------------------------------------------------------- | ---------------------------- |
| **`options`** | <code><a href="#pickmediaoptions">PickMediaOptions</a></code> | - Options for picking images |

**Returns:** <code>Promise&lt;<a href="#pickfilesresult">PickFilesResult</a>&gt;</code>

**Since:** 8.0.0

--------------------


### pickVideos(...)

```typescript
pickVideos(options?: PickMediaOptions | undefined) => Promise<PickFilesResult>
```

Pick one or more videos from the gallery.
Android/iOS only.

| Param         | Type                                                          | Description                  |
| ------------- | ------------------------------------------------------------- | ---------------------------- |
| **`options`** | <code><a href="#pickmediaoptions">PickMediaOptions</a></code> | - Options for picking videos |

**Returns:** <code>Promise&lt;<a href="#pickfilesresult">PickFilesResult</a>&gt;</code>

**Since:** 8.0.0

--------------------


### pickMedia(...)

```typescript
pickMedia(options?: PickMediaOptions | undefined) => Promise<PickFilesResult>
```

Pick one or more images or videos from the gallery.
Android/iOS only.

| Param         | Type                                                          | Description                 |
| ------------- | ------------------------------------------------------------- | --------------------------- |
| **`options`** | <code><a href="#pickmediaoptions">PickMediaOptions</a></code> | - Options for picking media |

**Returns:** <code>Promise&lt;<a href="#pickfilesresult">PickFilesResult</a>&gt;</code>

**Since:** 8.0.0

--------------------


### pickDirectory()

```typescript
pickDirectory() => Promise<PickDirectoryResult>
```

Pick a directory from the device.
Android/iOS only.

**Returns:** <code>Promise&lt;<a href="#pickdirectoryresult">PickDirectoryResult</a>&gt;</code>

**Since:** 8.0.0

--------------------


### convertHeicToJpeg(...)

```typescript
convertHeicToJpeg(options: ConvertHeicToJpegOptions) => Promise<ConvertHeicToJpegResult>
```

Convert a HEIC image to JPEG format.
iOS only.

| Param         | Type                                                                          | Description                  |
| ------------- | ----------------------------------------------------------------------------- | ---------------------------- |
| **`options`** | <code><a href="#convertheictojpegoptions">ConvertHeicToJpegOptions</a></code> | - Options for the conversion |

**Returns:** <code>Promise&lt;<a href="#convertheictojpegresult">ConvertHeicToJpegResult</a>&gt;</code>

**Since:** 8.0.0

--------------------


### copyFile(...)

```typescript
copyFile(options: CopyFileOptions) => Promise<void>
```

Copy a file to a new location.

| Param         | Type                                                        | Description                    |
| ------------- | ----------------------------------------------------------- | ------------------------------ |
| **`options`** | <code><a href="#copyfileoptions">CopyFileOptions</a></code> | - Options for copying the file |

**Since:** 8.0.0

--------------------


### checkPermissions()

```typescript
checkPermissions() => Promise<PermissionStatus>
```

Check broad storage or media permission state.
Picker-only flows should not gate `pickFiles()`, `pickImages()`,
`pickVideos()`, or `pickMedia()` on this permission on Android 13+.
Android only.

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

**Since:** 8.0.0

--------------------


### requestPermissions()

```typescript
requestPermissions() => Promise<PermissionStatus>
```

Request broad storage or media permissions.
Do not request or declare `READ_MEDIA_IMAGES` or `READ_MEDIA_VIDEO`
only to use picker APIs. Google Play allows these permissions only
when picker alternatives are not sufficient for core app functionality.
Use `@capgo/capacitor-file-picker` picker methods instead for
user-selected file, image, or video access.
Android only.

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

**Since:** 8.0.0

--------------------


### addListener('pickerDismissed', ...)

```typescript
addListener(eventName: 'pickerDismissed', listenerFunc: PickerDismissedListener) => Promise<PluginListenerHandle>
```

Add a listener for the picker dismissed event.
iOS only.

| Param              | Type                                                                        | Description             |
| ------------------ | --------------------------------------------------------------------------- | ----------------------- |
| **`eventName`**    | <code>'pickerDismissed'</code>                                              | - The event name        |
| **`listenerFunc`** | <code><a href="#pickerdismissedlistener">PickerDismissedListener</a></code> | - The listener function |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 8.0.0

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

Remove all listeners for this plugin.

**Since:** 8.0.0

--------------------


### getPluginVersion()

```typescript
getPluginVersion() => Promise<{ version: string; }>
```

Get the native Capacitor plugin version.

**Returns:** <code>Promise&lt;{ version: string; }&gt;</code>

**Since:** 8.0.0

--------------------


### Interfaces


#### PickFilesResult

Result of picking files.

| Prop        | Type                      | Description           |
| ----------- | ------------------------- | --------------------- |
| **`files`** | <code>PickedFile[]</code> | Array of picked files |


#### PickedFile

Represents a picked file.

| Prop             | Type                | Description                                                             |
| ---------------- | ------------------- | ----------------------------------------------------------------------- |
| **`name`**       | <code>string</code> | The name of the file                                                    |
| **`path`**       | <code>string</code> | The path to the file                                                    |
| **`mimeType`**   | <code>string</code> | The MIME type of the file                                               |
| **`size`**       | <code>number</code> | The size of the file in bytes                                           |
| **`data`**       | <code>string</code> | The base64 encoded data of the file. Only present if readData was true. |
| **`blob`**       | <code>Blob</code>   | The Blob instance of the file. Web only.                                |
| **`width`**      | <code>number</code> | Width in pixels (images/videos only)                                    |
| **`height`**     | <code>number</code> | Height in pixels (images/videos only)                                   |
| **`duration`**   | <code>number</code> | Duration in seconds (videos only)                                       |
| **`modifiedAt`** | <code>number</code> | Last modified timestamp in milliseconds                                 |


#### PickFilesOptions

Options for picking files.

| Prop           | Type                  | Description                                                                                                                                         | Default            |
| -------------- | --------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------ |
| **`types`**    | <code>string[]</code> | List of accepted MIME types or file extensions. On iOS, only MIME types are supported. Examples: ['image/*'], ['application/pdf'], ['.pdf', '.doc'] |                    |
| **`limit`**    | <code>number</code>   | Maximum number of files to pick. Set to 0 for unlimited (platform default).                                                                         | <code>0</code>     |
| **`readData`** | <code>boolean</code>  | Whether to read the file data as base64. Note: Reading large files may cause memory issues.                                                         | <code>false</code> |


#### PickMediaOptions

Options for picking media (images/videos).

| Prop                  | Type                 | Description                                                                                 | Default            |
| --------------------- | -------------------- | ------------------------------------------------------------------------------------------- | ------------------ |
| **`limit`**           | <code>number</code>  | Maximum number of files to pick. Set to 0 for unlimited (platform default).                 | <code>0</code>     |
| **`readData`**        | <code>boolean</code> | Whether to read the file data as base64. Note: Reading large files may cause memory issues. | <code>false</code> |
| **`skipTranscoding`** | <code>boolean</code> | iOS only: Skip transcoding of videos.                                                       | <code>false</code> |
| **`ordered`**         | <code>boolean</code> | iOS 15+ only: Show ordered selection badges.                                                | <code>false</code> |


#### PickDirectoryResult

Result of picking a directory.

| Prop       | Type                | Description                        |
| ---------- | ------------------- | ---------------------------------- |
| **`path`** | <code>string</code> | The path to the selected directory |


#### ConvertHeicToJpegResult

Result of HEIC to JPEG conversion.

| Prop       | Type                | Description                         |
| ---------- | ------------------- | ----------------------------------- |
| **`path`** | <code>string</code> | The path to the converted JPEG file |


#### ConvertHeicToJpegOptions

Options for converting HEIC to JPEG.

| Prop          | Type                | Description                                   | Default          |
| ------------- | ------------------- | --------------------------------------------- | ---------------- |
| **`path`**    | <code>string</code> | The path to the HEIC file to convert          |                  |
| **`quality`** | <code>number</code> | The compression quality for JPEG (0.0 - 1.0). | <code>0.9</code> |


#### CopyFileOptions

Options for copying a file.

| Prop            | Type                 | Description                                 | Default            |
| --------------- | -------------------- | ------------------------------------------- | ------------------ |
| **`from`**      | <code>string</code>  | Source file path                            |                    |
| **`to`**        | <code>string</code>  | Destination file path                       |                    |
| **`overwrite`** | <code>boolean</code> | Whether to overwrite if destination exists. | <code>false</code> |


#### PermissionStatus

Permission status for file access.

| Prop                      | Type                                                        | Description                                                                                                                                            |
| ------------------------- | ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **`readExternalStorage`** | <code><a href="#permissionstate">PermissionState</a></code> | Whether broad external storage or media permission is granted. Picker APIs that return user-selected files usually do not require this on Android 13+. |
| **`accessMediaLocation`** | <code><a href="#permissionstate">PermissionState</a></code> | Whether permission to access media location is granted                                                                                                 |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


### Type Aliases


#### PermissionState

<code>'prompt' | 'prompt-with-rationale' | 'granted' | 'denied'</code>


#### PickerDismissedListener

Listener callback for picker dismissed event.

<code>(event: null): void</code>

</docgen-api>
