package org.robolectric.shadows;


import RatingBar.OnRatingBarChangeListener;
import android.widget.RatingBar;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class ShadowRatingBarTest {
    private RatingBar ratingBar;

    private OnRatingBarChangeListener listener;

    private List<String> transcript;

    @Test
    public void testOnSeekBarChangedListener() {
        assertThat(ratingBar.getOnRatingBarChangeListener()).isSameAs(listener);
        ratingBar.setOnRatingBarChangeListener(null);
        assertThat(ratingBar.getOnRatingBarChangeListener()).isNull();
    }

    @Test
    public void testOnChangeNotification() {
        ratingBar.setRating(5.0F);
        assertThat(transcript).containsExactly("onRatingChanged() - 5.0");
    }

    private class TestRatingBarChangedListener implements RatingBar.OnRatingBarChangeListener {
        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            transcript.add(("onRatingChanged() - " + rating));
        }
    }
}
