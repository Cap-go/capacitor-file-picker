package app.capgo.capacitor.filepicker;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.webkit.MimeTypeMap;
import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;

@CapacitorPlugin(
    name = "CapgoFilePicker",
    permissions = {
        @Permission(
            alias = "readExternalStorage",
            strings = {
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
            }
        ),
        @Permission(alias = "accessMediaLocation", strings = { Manifest.permission.ACCESS_MEDIA_LOCATION })
    }
)
public class CapgoFilePickerPlugin extends Plugin {

    private final String pluginVersion = "8.1.11";
    private boolean readData = false;

    // MARK: - Pick Files

    @PluginMethod
    public void pickFiles(PluginCall call) {
        readData = call.getBoolean("readData", false);
        int limit = call.getInt("limit", 0);

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");

        JSArray types = call.getArray("types");
        if (types != null && types.length() > 0) {
            try {
                String[] mimeTypes = new String[types.length()];
                for (int i = 0; i < types.length(); i++) {
                    String type = types.getString(i);
                    if (type.startsWith(".")) {
                        String ext = type.substring(1);
                        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                        mimeTypes[i] = mimeType != null ? mimeType : "*/*";
                    } else {
                        mimeTypes[i] = type;
                    }
                }
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            } catch (JSONException e) {
                // Use default type
            }
        }

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, limit != 1);

        startActivityForResult(call, intent, "pickFilesResult");
    }

    // MARK: - Pick Images

    @PluginMethod
    public void pickImages(PluginCall call) {
        readData = call.getBoolean("readData", false);
        int limit = call.getInt("limit", 0);

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, limit != 1);

        startActivityForResult(call, intent, "pickMediaResult");
    }

    // MARK: - Pick Videos

    @PluginMethod
    public void pickVideos(PluginCall call) {
        readData = call.getBoolean("readData", false);
        int limit = call.getInt("limit", 0);

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, limit != 1);

        startActivityForResult(call, intent, "pickMediaResult");
    }

    // MARK: - Pick Media

    @PluginMethod
    public void pickMedia(PluginCall call) {
        readData = call.getBoolean("readData", false);
        int limit = call.getInt("limit", 0);

        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] { "image/*", "video/*" });
            if (limit > 1) {
                intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, limit);
            }
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] { "image/*", "video/*" });
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, limit != 1);
        }

        startActivityForResult(call, intent, "pickMediaResult");
    }

    // MARK: - Pick Directory

    @PluginMethod
    public void pickDirectory(PluginCall call) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        startActivityForResult(call, intent, "pickDirectoryResult");
    }

    // MARK: - Convert HEIC to JPEG

    @PluginMethod
    public void convertHeicToJpeg(PluginCall call) {
        call.reject("convertHeicToJpeg is not supported on Android");
    }

    // MARK: - Copy File

    @PluginMethod
    public void copyFile(PluginCall call) {
        String from = call.getString("from");
        String to = call.getString("to");
        boolean overwrite = call.getBoolean("overwrite", false);

        if (from == null || to == null) {
            call.reject("from and to are required");
            return;
        }

        try {
            File fromFile = new File(from);
            File toFile = new File(to);

            if (!fromFile.exists()) {
                call.reject("Source file does not exist");
                return;
            }

            if (toFile.exists() && !overwrite) {
                call.reject("Destination file already exists");
                return;
            }

            // Create parent directories if needed
            File parentDir = toFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (InputStream in = new FileInputStream(fromFile); OutputStream out = new FileOutputStream(toFile)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }

            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to copy file: " + e.getMessage());
        }
    }

    // MARK: - Permissions

    @PluginMethod
    public void checkPermissions(PluginCall call) {
        JSObject result = new JSObject();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            result.put("readExternalStorage", "granted");
        } else {
            String state = getPermissionState("readExternalStorage").toString().toLowerCase();
            result.put("readExternalStorage", state);
        }
        call.resolve(result);
    }

    @PluginMethod
    public void requestPermissions(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            JSObject result = new JSObject();
            result.put("readExternalStorage", "granted");
            call.resolve(result);
        } else {
            requestPermissionForAlias("readExternalStorage", call, "permissionCallback");
        }
    }

    @PermissionCallback
    private void permissionCallback(PluginCall call) {
        JSObject result = new JSObject();
        String state = getPermissionState("readExternalStorage").toString().toLowerCase();
        result.put("readExternalStorage", state);
        call.resolve(result);
    }

    // MARK: - Plugin Version

    @PluginMethod
    public void getPluginVersion(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("version", this.pluginVersion);
        call.resolve(ret);
    }

    // MARK: - Activity Results

    @ActivityCallback
    private void pickFilesResult(PluginCall call, ActivityResult result) {
        handleFilePickerResult(call, result);
    }

    @ActivityCallback
    private void pickMediaResult(PluginCall call, ActivityResult result) {
        handleFilePickerResult(call, result);
    }

    private void handleFilePickerResult(PluginCall call, ActivityResult result) {
        if (call == null) {
            return;
        }

        if (result.getResultCode() != Activity.RESULT_OK) {
            notifyListeners("pickerDismissed", new JSObject());
            JSObject ret = new JSObject();
            ret.put("files", new JSArray());
            call.resolve(ret);
            return;
        }

        Intent data = result.getData();
        if (data == null) {
            JSObject ret = new JSObject();
            ret.put("files", new JSArray());
            call.resolve(ret);
            return;
        }

        List<Uri> uris = new ArrayList<>();

        ClipData clipData = data.getClipData();
        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                uris.add(clipData.getItemAt(i).getUri());
            }
        } else if (data.getData() != null) {
            uris.add(data.getData());
        }

        JSArray files = new JSArray();
        for (Uri uri : uris) {
            JSObject fileInfo = getFileInfo(uri);
            if (fileInfo != null) {
                files.put(fileInfo);
            }
        }

        JSObject ret = new JSObject();
        ret.put("files", files);
        call.resolve(ret);
    }

    @ActivityCallback
    private void pickDirectoryResult(PluginCall call, ActivityResult result) {
        if (call == null) {
            return;
        }

        if (result.getResultCode() != Activity.RESULT_OK) {
            notifyListeners("pickerDismissed", new JSObject());
            call.reject("User cancelled");
            return;
        }

        Intent data = result.getData();
        if (data == null || data.getData() == null) {
            call.reject("No directory selected");
            return;
        }

        Uri uri = data.getData();
        String path = getPathFromUri(uri);

        JSObject ret = new JSObject();
        ret.put("path", path != null ? path : uri.toString());
        call.resolve(ret);
    }

    // MARK: - Helpers

    @Nullable
    private JSObject getFileInfo(Uri uri) {
        try {
            JSObject result = new JSObject();

            // Get name and size from content resolver
            try (Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

                    if (nameIndex >= 0) {
                        result.put("name", cursor.getString(nameIndex));
                    }
                    if (sizeIndex >= 0) {
                        result.put("size", cursor.getLong(sizeIndex));
                    }
                }
            }

            // Get MIME type
            String mimeType = getContext().getContentResolver().getType(uri);
            result.put("mimeType", mimeType != null ? mimeType : "application/octet-stream");

            // Get path
            String path = getPathFromUri(uri);
            result.put("path", path != null ? path : uri.toString());

            // Get modified date
            Long modifiedAt = getModifiedAt(uri);
            if (modifiedAt != null) {
                result.put("modifiedAt", modifiedAt);
            }

            // Get dimensions for images
            if (mimeType != null && mimeType.startsWith("image/")) {
                try (InputStream is = getContext().getContentResolver().openInputStream(uri)) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(is, null, options);
                    result.put("width", options.outWidth);
                    result.put("height", options.outHeight);
                } catch (Exception e) {
                    // Ignore dimension errors
                }
            }

            // Get dimensions and duration for videos
            if (mimeType != null && mimeType.startsWith("video/")) {
                try {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(getContext(), uri);

                    String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                    String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                    String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

                    int w = width != null ? Integer.parseInt(width) : 0;
                    int h = height != null ? Integer.parseInt(height) : 0;
                    int rot = rotation != null ? Integer.parseInt(rotation) : 0;

                    // Swap dimensions if rotated 90 or 270 degrees
                    if (rot == 90 || rot == 270) {
                        result.put("width", h);
                        result.put("height", w);
                    } else {
                        result.put("width", w);
                        result.put("height", h);
                    }

                    if (duration != null) {
                        result.put("duration", Long.parseLong(duration) / 1000.0);
                    }

                    retriever.release();
                } catch (Exception e) {
                    // Ignore metadata errors
                }
            }

            // Read data if requested
            if (readData) {
                try (InputStream is = getContext().getContentResolver().openInputStream(uri)) {
                    if (is != null) {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        byte[] data = new byte[8192];
                        int length;
                        while ((length = is.read(data)) != -1) {
                            buffer.write(data, 0, length);
                        }
                        result.put("data", Base64.encodeToString(buffer.toByteArray(), Base64.NO_WRAP));
                    }
                } catch (Exception e) {
                    // Ignore data read errors
                }
            }

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private String getPathFromUri(Uri uri) {
        if (uri == null) {
            return null;
        }

        // Try to get actual file path
        if ("file".equals(uri.getScheme())) {
            return uri.getPath();
        }

        // For content URIs, return the URI string
        return uri.toString();
    }

    @Nullable
    private Long getModifiedAt(Uri uri) {
        try {
            String[] projection = { MediaStore.MediaColumns.DATE_MODIFIED };
            try (Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED);
                    if (columnIndex >= 0) {
                        return cursor.getLong(columnIndex) * 1000; // Convert to milliseconds
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}
