import type { PluginListenerHandle } from '@capacitor/core';

/**
 * Options for picking files.
 *
 * @since 8.0.0
 */
export interface PickFilesOptions {
  /**
   * List of accepted MIME types or file extensions.
   * On iOS, only MIME types are supported.
   * Examples: ['image/*'], ['application/pdf'], ['.pdf', '.doc']
   */
  types?: string[];
  /**
   * Maximum number of files to pick.
   * Set to 0 for unlimited (platform default).
   * @default 0
   */
  limit?: number;
  /**
   * Whether to read the file data as base64.
   * Note: Reading large files may cause memory issues.
   * @default false
   */
  readData?: boolean;
}

/**
 * Options for picking media (images/videos).
 *
 * @since 8.0.0
 */
export interface PickMediaOptions {
  /**
   * Maximum number of files to pick.
   * Set to 0 for unlimited (platform default).
   * @default 0
   */
  limit?: number;
  /**
   * Whether to read the file data as base64.
   * Note: Reading large files may cause memory issues.
   * @default false
   */
  readData?: boolean;
  /**
   * iOS only: Skip transcoding of videos.
   * @default false
   */
  skipTranscoding?: boolean;
  /**
   * iOS 15+ only: Show ordered selection badges.
   * @default false
   */
  ordered?: boolean;
}

/**
 * Options for converting HEIC to JPEG.
 *
 * @since 8.0.0
 */
export interface ConvertHeicToJpegOptions {
  /** The path to the HEIC file to convert */
  path: string;
  /**
   * The compression quality for JPEG (0.0 - 1.0).
   * @default 0.9
   */
  quality?: number;
}

/**
 * Result of HEIC to JPEG conversion.
 *
 * @since 8.0.0
 */
export interface ConvertHeicToJpegResult {
  /** The path to the converted JPEG file */
  path: string;
}

/**
 * Options for copying a file.
 *
 * @since 8.0.0
 */
export interface CopyFileOptions {
  /** Source file path */
  from: string;
  /** Destination file path */
  to: string;
  /**
   * Whether to overwrite if destination exists.
   * @default false
   */
  overwrite?: boolean;
}

/**
 * Represents a picked file.
 *
 * @since 8.0.0
 */
export interface PickedFile {
  /** The name of the file */
  name: string;
  /** The path to the file */
  path?: string;
  /** The MIME type of the file */
  mimeType: string;
  /** The size of the file in bytes */
  size: number;
  /**
   * The base64 encoded data of the file.
   * Only present if readData was true.
   */
  data?: string;
  /**
   * The Blob instance of the file.
   * Web only.
   */
  blob?: Blob;
  /** Width in pixels (images/videos only) */
  width?: number;
  /** Height in pixels (images/videos only) */
  height?: number;
  /** Duration in seconds (videos only) */
  duration?: number;
  /** Last modified timestamp in milliseconds */
  modifiedAt?: number;
}

/**
 * Result of picking files.
 *
 * @since 8.0.0
 */
export interface PickFilesResult {
  /** Array of picked files */
  files: PickedFile[];
}

/**
 * Result of picking a directory.
 *
 * @since 8.0.0
 */
export interface PickDirectoryResult {
  /** The path to the selected directory */
  path: string;
}

/**
 * Permission status for file access.
 *
 * @since 8.0.0
 */
export interface PermissionStatus {
  /** Whether permission to read media files is granted */
  readExternalStorage: PermissionState;
  /** Whether permission to access media location is granted */
  accessMediaLocation?: PermissionState;
}

/**
 * Permission state values.
 *
 * @since 8.0.0
 */
export type PermissionState = 'prompt' | 'prompt-with-rationale' | 'granted' | 'denied';

/**
 * Listener callback for picker dismissed event.
 *
 * @since 8.0.0
 */
export type PickerDismissedListener = (event: null) => void;

/**
 * Capacitor File Picker Plugin interface for selecting files, images, videos, and directories.
 *
 * @since 8.0.0
 */
export interface CapgoFilePickerPlugin {
  /**
   * Pick one or more files from the device.
   *
   * @param options - Options for picking files
   * @returns Promise that resolves with the picked files
   * @since 8.0.0
   * @example
   * ```typescript
   * const result = await CapgoFilePicker.pickFiles({
   *   types: ['application/pdf', 'image/*'],
   *   limit: 5,
   *   readData: false
   * });
   * console.log('Picked files:', result.files);
   * ```
   */
  pickFiles(options?: PickFilesOptions): Promise<PickFilesResult>;

  /**
   * Pick one or more images from the gallery.
   * Android/iOS only.
   *
   * @param options - Options for picking images
   * @returns Promise that resolves with the picked images
   * @since 8.0.0
   * @example
   * ```typescript
   * const result = await CapgoFilePicker.pickImages({
   *   limit: 10,
   *   readData: false
   * });
   * console.log('Picked images:', result.files);
   * ```
   */
  pickImages(options?: PickMediaOptions): Promise<PickFilesResult>;

  /**
   * Pick one or more videos from the gallery.
   * Android/iOS only.
   *
   * @param options - Options for picking videos
   * @returns Promise that resolves with the picked videos
   * @since 8.0.0
   * @example
   * ```typescript
   * const result = await CapgoFilePicker.pickVideos({
   *   limit: 3,
   *   skipTranscoding: true
   * });
   * console.log('Picked videos:', result.files);
   * ```
   */
  pickVideos(options?: PickMediaOptions): Promise<PickFilesResult>;

  /**
   * Pick one or more images or videos from the gallery.
   * Android/iOS only.
   *
   * @param options - Options for picking media
   * @returns Promise that resolves with the picked media files
   * @since 8.0.0
   * @example
   * ```typescript
   * const result = await CapgoFilePicker.pickMedia({
   *   limit: 5,
   *   readData: true
   * });
   * console.log('Picked media:', result.files);
   * ```
   */
  pickMedia(options?: PickMediaOptions): Promise<PickFilesResult>;

  /**
   * Pick a directory from the device.
   * Android/iOS only.
   *
   * @returns Promise that resolves with the picked directory path
   * @since 8.0.0
   * @example
   * ```typescript
   * const result = await CapgoFilePicker.pickDirectory();
   * console.log('Selected directory:', result.path);
   * ```
   */
  pickDirectory(): Promise<PickDirectoryResult>;

  /**
   * Convert a HEIC image to JPEG format.
   * iOS only.
   *
   * @param options - Options for the conversion
   * @returns Promise that resolves with the converted file path
   * @since 8.0.0
   * @example
   * ```typescript
   * const result = await CapgoFilePicker.convertHeicToJpeg({
   *   path: '/path/to/image.heic',
   *   quality: 0.9
   * });
   * console.log('Converted file:', result.path);
   * ```
   */
  convertHeicToJpeg(options: ConvertHeicToJpegOptions): Promise<ConvertHeicToJpegResult>;

  /**
   * Copy a file to a new location.
   *
   * @param options - Options for copying the file
   * @returns Promise that resolves when the copy is complete
   * @since 8.0.0
   * @example
   * ```typescript
   * await CapgoFilePicker.copyFile({
   *   from: '/source/file.pdf',
   *   to: '/destination/file.pdf',
   *   overwrite: true
   * });
   * ```
   */
  copyFile(options: CopyFileOptions): Promise<void>;

  /**
   * Check permissions for reading files.
   * Android only.
   *
   * @returns Promise that resolves with the permission status
   * @since 8.0.0
   * @example
   * ```typescript
   * const status = await CapgoFilePicker.checkPermissions();
   * console.log('Read permission:', status.readExternalStorage);
   * ```
   */
  checkPermissions(): Promise<PermissionStatus>;

  /**
   * Request permissions for reading files.
   * Android only.
   *
   * @returns Promise that resolves with the permission status
   * @since 8.0.0
   * @example
   * ```typescript
   * const status = await CapgoFilePicker.requestPermissions();
   * if (status.readExternalStorage === 'granted') {
   *   console.log('Permission granted');
   * }
   * ```
   */
  requestPermissions(): Promise<PermissionStatus>;

  /**
   * Add a listener for the picker dismissed event.
   * iOS only.
   *
   * @param eventName - The event name
   * @param listenerFunc - The listener function
   * @returns Promise that resolves with the listener handle
   * @since 8.0.0
   */
  addListener(eventName: 'pickerDismissed', listenerFunc: PickerDismissedListener): Promise<PluginListenerHandle>;

  /**
   * Remove all listeners for this plugin.
   *
   * @since 8.0.0
   */
  removeAllListeners(): Promise<void>;

  /**
   * Get the native Capacitor plugin version.
   *
   * @returns Promise that resolves with the plugin version
   * @since 8.0.0
   * @example
   * ```typescript
   * const { version } = await CapgoFilePicker.getPluginVersion();
   * console.log('Plugin version:', version);
   * ```
   */
  getPluginVersion(): Promise<{ version: string }>;
}
