package app.capgo.capacitor.filepicker;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.ext.SdkExtensions;
import android.provider.MediaStore;
import java.util.ArrayList;
import java.util.List;

/** Builds gallery picker intents and applies selection limits on Android. */
final class MediaPickerIntents {

    private MediaPickerIntents() {}

    static boolean isPhotoPickerAvailable() {
        return isPhotoPickerAvailable(Build.VERSION.SDK_INT, getRExtensionVersion());
    }

    static boolean isPhotoPickerAvailable(int sdkInt, int rExtensionVersion) {
        if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        if (sdkInt >= Build.VERSION_CODES.R) {
            return rExtensionVersion >= 2;
        }
        return false;
    }

    private static int getRExtensionVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R);
        }
        return 0;
    }

    static Intent buildPickImagesIntent(int limit) {
        return buildGalleryIntent("image/*", MediaStore.Images.Media.EXTERNAL_CONTENT_URI, limit, isPhotoPickerAvailable());
    }

    static Intent buildPickVideosIntent(int limit) {
        return buildGalleryIntent("video/*", MediaStore.Video.Media.EXTERNAL_CONTENT_URI, limit, isPhotoPickerAvailable());
    }

    static Intent buildGalleryIntent(String mimeType, Uri legacyPickUri, int limit, boolean photoPickerAvailable) {
        return buildGalleryIntent(mimeType, legacyPickUri, limit, photoPickerAvailable, MediaStore.getPickImagesMaxLimit());
    }

    static Intent buildGalleryIntent(String mimeType, Uri legacyPickUri, int limit, boolean photoPickerAvailable, int pickImagesMaxLimit) {
        if (photoPickerAvailable) {
            Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            intent.setType(mimeType);
            if (limit > 1) {
                int capped = pickImagesMaxLimit > 0 ? Math.min(limit, pickImagesMaxLimit) : limit;
                intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, capped);
            }
            return intent;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, legacyPickUri);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, limit != 1);
        return intent;
    }

    static boolean shouldTruncateResultsToLimit(boolean photoPickerAvailable) {
        return !photoPickerAvailable;
    }

    static List<Uri> truncateUris(List<Uri> uris, int limit) {
        if (limit <= 0 || uris.size() <= limit) {
            return uris;
        }
        return new ArrayList<>(uris.subList(0, limit));
    }
}
