package app.capgo.capacitor.filepicker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
    public void pickImagesIntentUsesPhotoPickerWithMaxLimit() {
        Intent intent = MediaPickerIntents.buildPickImagesIntent(5, true);

        assertEquals(MediaStore.ACTION_PICK_IMAGES, intent.getAction());
        assertEquals("image/*", intent.getType());
        assertEquals(5, intent.getIntExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, -1));
        assertFalse(intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false));
    }
}
