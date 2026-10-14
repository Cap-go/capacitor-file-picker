package app.capgo.capacitor.filepicker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import android.content.Intent;
import android.provider.MediaStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Regression for issue #12: legacy pickImages used ACTION_PICK and only toggled
 * EXTRA_ALLOW_MULTIPLE, so limit did not cap selections on modern Android.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class PickImagesLimitRegressionTest {

    @Test
    public void fixedPickImagesUsesPhotoPickerMaxExtraNotLegacyActionPick() {
        Intent legacy = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        legacy.setType("image/*");
        legacy.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        Intent fixed = MediaPickerIntents.buildGalleryIntent("image/*", MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 5, true, 100);

        assertEquals(Intent.ACTION_PICK, legacy.getAction());
        assertFalse(legacy.hasExtra(MediaStore.EXTRA_PICK_IMAGES_MAX));
        assertNotEquals(legacy.getAction(), fixed.getAction());
        assertEquals(5, fixed.getIntExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, -1));
    }
}
