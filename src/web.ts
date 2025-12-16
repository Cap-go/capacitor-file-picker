import { WebPlugin } from '@capacitor/core';

import type { PluginListenerHandle } from '@capacitor/core';

import type {
  CapgoFilePickerPlugin,
  ConvertHeicToJpegOptions,
  ConvertHeicToJpegResult,
  CopyFileOptions,
  PermissionStatus,
  PickedFile,
  PickDirectoryResult,
  PickFilesOptions,
  PickFilesResult,
  PickMediaOptions,
  PickerDismissedListener,
} from './definitions';

export class CapgoFilePickerWeb extends WebPlugin implements CapgoFilePickerPlugin {
  async pickFiles(options?: PickFilesOptions): Promise<PickFilesResult> {
    const files = await this.openFilePicker({
      types: options?.types,
      multiple: options?.limit !== 1,
    });
    const pickedFiles: PickedFile[] = [];
    for (const file of files) {
      const pickedFile: PickedFile = {
        name: file.name,
        mimeType: file.type || 'application/octet-stream',
        size: file.size,
        blob: file,
        modifiedAt: file.lastModified,
      };
      if (options?.readData) {
        pickedFile.data = await this.getDataFromFile(file);
      }
      if (file.type.startsWith('image/')) {
        const dimensions = await this.getImageDimensions(file);
        pickedFile.width = dimensions.width;
        pickedFile.height = dimensions.height;
      }
      pickedFiles.push(pickedFile);
    }
    return { files: pickedFiles };
  }

  async pickImages(options?: PickMediaOptions): Promise<PickFilesResult> {
    return this.pickFiles({
      types: ['image/*'],
      limit: options?.limit,
      readData: options?.readData,
    });
  }

  async pickVideos(options?: PickMediaOptions): Promise<PickFilesResult> {
    return this.pickFiles({
      types: ['video/*'],
      limit: options?.limit,
      readData: options?.readData,
    });
  }

  async pickMedia(options?: PickMediaOptions): Promise<PickFilesResult> {
    return this.pickFiles({
      types: ['image/*', 'video/*'],
      limit: options?.limit,
      readData: options?.readData,
    });
  }

  async pickDirectory(): Promise<PickDirectoryResult> {
    throw this.unimplemented('pickDirectory is not supported on web');
  }

  async convertHeicToJpeg(_options: ConvertHeicToJpegOptions): Promise<ConvertHeicToJpegResult> {
    throw this.unimplemented('convertHeicToJpeg is not supported on web');
  }

  async copyFile(_options: CopyFileOptions): Promise<void> {
    throw this.unimplemented('copyFile is not supported on web');
  }

  async checkPermissions(): Promise<PermissionStatus> {
    return {
      readExternalStorage: 'granted',
    };
  }

  async requestPermissions(): Promise<PermissionStatus> {
    return {
      readExternalStorage: 'granted',
    };
  }

  async addListener(
    _eventName: 'pickerDismissed',
    _listenerFunc: PickerDismissedListener,
  ): Promise<PluginListenerHandle> {
    return {
      remove: async () => {
        // No-op on web
      },
    };
  }

  async removeAllListeners(): Promise<void> {
    // No-op on web
  }

  async getPluginVersion(): Promise<{ version: string }> {
    return { version: 'web' };
  }

  private openFilePicker(options: { types?: string[]; multiple?: boolean }): Promise<File[]> {
    return new Promise((resolve) => {
      const input = document.createElement('input');
      input.type = 'file';
      input.multiple = options.multiple ?? true;
      if (options.types && options.types.length > 0) {
        input.accept = options.types.join(',');
      }
      input.style.display = 'none';
      document.body.appendChild(input);

      let resolved = false;

      const handleChange = () => {
        resolved = true;
        const files = Array.from(input.files || []);
        cleanup();
        resolve(files);
      };

      const handleCancel = () => {
        // Small delay to ensure change event fires first if files were selected
        setTimeout(() => {
          if (!resolved) {
            cleanup();
            resolve([]);
          }
        }, 300);
      };

      const cleanup = () => {
        input.removeEventListener('change', handleChange);
        window.removeEventListener('focus', handleCancel);
        document.body.removeChild(input);
      };

      input.addEventListener('change', handleChange);
      window.addEventListener('focus', handleCancel);

      input.click();
    });
  }

  private getDataFromFile(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const result = reader.result as string;
        // Remove data URL prefix to get pure base64
        const base64 = result.split(',')[1] || '';
        resolve(base64);
      };
      reader.onerror = () => reject(new Error('Failed to read file'));
      reader.readAsDataURL(file);
    });
  }

  private getImageDimensions(file: File): Promise<{ width: number; height: number }> {
    return new Promise((resolve) => {
      const img = new Image();
      img.onload = () => {
        URL.revokeObjectURL(img.src);
        resolve({ width: img.width, height: img.height });
      };
      img.onerror = () => {
        URL.revokeObjectURL(img.src);
        resolve({ width: 0, height: 0 });
      };
      img.src = URL.createObjectURL(file);
    });
  }
}
