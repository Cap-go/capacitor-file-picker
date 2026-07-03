import './style.css';
import { Capacitor } from '@capacitor/core';
import { CapacitorUpdater } from '@capgo/capacitor-updater';
import { CapgoFilePicker } from '@capgo/capacitor-file-picker';

const output = document.getElementById('output');
const setOutput = (value) => {
  output.textContent = typeof value === 'string' ? value : JSON.stringify(value, null, 2);
};

document.getElementById('pick-files').addEventListener('click', async () => {
  try {
    const result = await CapgoFilePicker.pickFiles({ types: ['*/*'], limit: 5 });
    setOutput(result);
  } catch (error) {
    setOutput(`Error: ${error?.message ?? error}`);
  }
});

document.getElementById('pick-media').addEventListener('click', async () => {
  try {
    const result = await CapgoFilePicker.pickMedia({ limit: 5 });
    setOutput(result);
  } catch (error) {
    setOutput(`Error: ${error?.message ?? error}`);
  }
});

document.getElementById('get-version').addEventListener('click', async () => {
  try {
    setOutput(await CapgoFilePicker.getPluginVersion());
  } catch (error) {
    setOutput(`Error: ${error?.message ?? error}`);
  }
});

if (Capacitor.isNativePlatform()) {
  CapacitorUpdater.notifyAppReady().catch((error) => console.error('Capgo notifyAppReady failed', error));
}
