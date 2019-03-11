package com.pushtorefresh.storio3.contentresolver.queries;


import android.net.Uri;
import com.pushtorefresh.storio3.contentresolver.BuildConfig;
import com.pushtorefresh.storio3.test.ToStringChecker;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;


// Required for correct Uri impl
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class InsertQueryTest {
    @Test
    public void shouldNotAllowNullUriObject() {
        try {
            // noinspection ConstantConditions
            InsertQuery.builder().uri(((Uri) (null)));
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        } catch (NullPointerException expected) {
            assertThat(expected).hasMessage("Please specify uri").hasNoCause();
        }
    }

    @Test
    public void shouldNotAllowNullUriString() {
        try {
            // noinspection ConstantConditions
            InsertQuery.builder().uri(((String) (null)));
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        } catch (NullPointerException expected) {
            assertThat(expected).hasMessage("Uri should not be null").hasNoCause();
        }
    }

    @Test
    public void shouldNotAllowEmptyUriString() {
        try {
            InsertQuery.builder().uri("");
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException expected) {
            assertThat(expected).hasMessage("Uri should not be null").hasNoCause();
        }
    }

    @Test
    public void completeBuilderShouldNotAllowNullUriObject() {
        try {
            // noinspection ConstantConditions
            InsertQuery.builder().uri(Mockito.mock(Uri.class)).uri(((Uri) (null)));
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        } catch (NullPointerException expected) {
            assertThat(expected).hasMessage("Please specify uri").hasNoCause();
        }
    }

    @Test
    public void completeBuilderShouldNotAllowNullUriString() {
        try {
            // noinspection ConstantConditions
            InsertQuery.builder().uri(Mockito.mock(Uri.class)).uri(((String) (null)));
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        } catch (NullPointerException expected) {
            assertThat(expected).hasMessage("Uri should not be null").hasNoCause();
        }
    }

    @Test
    public void completeBuilderShouldNotAllowEmptyUriString() {
        try {
            InsertQuery.builder().uri(Mockito.mock(Uri.class)).uri("");
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException expected) {
            assertThat(expected).hasMessage("Uri should not be null").hasNoCause();
        }
    }

    @Test
    public void completeBuilderShouldUpdateUriObject() {
        Uri oldUri = Mockito.mock(Uri.class);
        Uri newUri = Mockito.mock(Uri.class);
        InsertQuery query = InsertQuery.builder().uri(oldUri).uri(newUri).build();
        assertThat(query.uri()).isSameAs(newUri);
    }

    @Test
    public void completeBuilderShouldUpdateUriString() {
        Uri oldUri = Uri.parse("content://1");
        String newUri = "content://2";
        InsertQuery query = InsertQuery.builder().uri(oldUri).uri(newUri).build();
        assertThat(query.uri()).isEqualTo(Uri.parse(newUri));
    }

    @Test
    public void createdThroughToBuilderQueryShouldBeEqual() {
        final Uri uri = Mockito.mock(Uri.class);
        final InsertQuery firstQuery = InsertQuery.builder().uri(uri).build();
        final InsertQuery secondQuery = firstQuery.toBuilder().build();
        assertThat(secondQuery).isEqualTo(firstQuery);
    }

    @Test
    public void buildWithNormalValues() {
        final Uri uri = Mockito.mock(Uri.class);
        final InsertQuery insertQuery = InsertQuery.builder().uri(uri).build();
        assertThat(insertQuery.uri()).isEqualTo(uri);
    }

    @Test
    public void verifyEqualsAndHashCodeImplementation() {
        EqualsVerifier.forClass(InsertQuery.class).allFieldsShouldBeUsed().withPrefabValues(Uri.class, Uri.parse("content://1"), Uri.parse("content://2")).verify();
    }

    @Test
    public void checkToStringImplementation() {
        ToStringChecker.forClass(InsertQuery.class).check();
    }
}
