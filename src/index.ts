import { registerPlugin } from '@capacitor/core';

import type { CapgoFilePickerPlugin } from './definitions';

const CapgoFilePicker = registerPlugin<CapgoFilePickerPlugin>('CapgoFilePicker', {
  web: () => import('./web').then((m) => new m.CapgoFilePickerWeb()),
});

export * from './definitions';
export { CapgoFilePicker };
