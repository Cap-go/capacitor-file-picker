package app.capgo.capacitor.filepicker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class MediaPickerIntentsTest {

    private static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private static final Uri VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

    @Test
    public void photoPickerAvailableOnApi33() {
        assertTrue(MediaPickerIntents.isPhotoPickerAvailable(33, 0));
    }

    @Test
    public void photoPickerAvailableOnApi30WithRExtension2() {
        assertTrue(MediaPickerIntents.isPhotoPickerAvailable(30, 2));
        assertFalse(MediaPickerIntents.isPhotoPickerAvailable(30, 1));
    }

    @Test
    public void pickImagesUsesSystemPhotoPickerWithMaxLimit() {
        Intent intent = MediaPickerIntents.buildGalleryIntent("image/*", IMAGE_URI, 5, true, 100);

        assertEquals(MediaStore.ACTION_PICK_IMAGES, intent.getAction());
        assertEquals("image/*", intent.getType());
        assertEquals(5, intent.getIntExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, -1));
        assertFalse(intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false));
    }

    @Test
    public void pickImagesCapsMaxLimit() {
        Intent intent = MediaPickerIntents.buildGalleryIntent("image/*", IMAGE_URI, 50, true, 20);

        assertEquals(20, intent.getIntExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, -1));
    }

    @Test
    public void pickImagesUnlimitedUsesDeviceMaxOnPhotoPicker() {
        Intent intent = MediaPickerIntents.buildGalleryIntent("image/*", IMAGE_URI, 0, true, 100);

        assertEquals(MediaStore.ACTION_PICK_IMAGES, intent.getAction());
        assertEquals(100, intent.getIntExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, -1));
    }

    @Test
    public void resolvePhotoPickerMaxLimitTreatsZeroAsUnlimited() {
        assertEquals(100, MediaPickerIntents.resolvePhotoPickerMaxLimit(0, 100));
        assertEquals(5, MediaPickerIntents.resolvePhotoPickerMaxLimit(5, 100));
        assertEquals(20, MediaPickerIntents.resolvePhotoPickerMaxLimit(50, 20));
    }

    @Test
    public void pickImagesSingleSelectionOmitsMaxExtra() {
        Intent intent = MediaPickerIntents.buildGalleryIntent("image/*", IMAGE_URI, 1, true, 100);

        assertEquals(MediaStore.ACTION_PICK_IMAGES, intent.getAction());
        assertFalse(intent.hasExtra(MediaStore.EXTRA_PICK_IMAGES_MAX));
    }

    @Test
    public void pickVideosUsesSystemPhotoPickerWithVideoMimeType() {
        Intent intent = MediaPickerIntents.buildGalleryIntent("video/*", VIDEO_URI, 3, true, 100);

        assertEquals(MediaStore.ACTION_PICK_IMAGES, intent.getAction());
        assertEquals("video/*", intent.getType());
        assertEquals(3, intent.getIntExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, -1));
    }

    @Test
    public void legacyFallbackUsesActionPickAndAllowMultiple() {
        Intent intent = MediaPickerIntents.buildGalleryIntent("image/*", IMAGE_URI, 5, false, 100);

        assertEquals(Intent.ACTION_PICK, intent.getAction());
        assertTrue(intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false));
        assertFalse(intent.hasExtra(MediaStore.EXTRA_PICK_IMAGES_MAX));
    }

    @Test
    public void truncateUrisEnforcesLimit() {
        Uri a = Uri.parse("content://a");
        Uri b = Uri.parse("content://b");
        Uri c = Uri.parse("content://c");
        List<Uri> truncated = MediaPickerIntents.truncateUris(Arrays.asList(a, b, c), 2);

        assertEquals(2, truncated.size());
        assertEquals(a, truncated.get(0));
        assertEquals(b, truncated.get(1));
    }

    @Test
    public void truncateUrisNoOpWhenLimitZero() {
        Uri a = Uri.parse("content://a");
        List<Uri> result = MediaPickerIntents.truncateUris(Arrays.asList(a), 0);
        assertEquals(1, result.size());
    }
}
