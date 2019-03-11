/**
 * Copyright 2011-2015 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.parceler;


import android.databinding.ObservableArrayList;
import android.databinding.ObservableArrayMap;
import android.databinding.ObservableField;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;


/**
 *
 *
 * @author John Ericksen
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ObservableFieldTest {
    @Parcel
    public static class ObservableTestTarget {
        ObservableField<ObservableFieldTest.TestParcel> test;

        ObservableArrayList<ObservableFieldTest.TestParcel> list;

        ObservableArrayMap<String, ObservableFieldTest.TestParcel> map;
    }

    @Parcel
    public static class TestParcel {
        String value;
    }

    @Test
    public void testObservable() {
        ObservableFieldTest.ObservableTestTarget target = new ObservableFieldTest.ObservableTestTarget();
        target.test = new ObservableField<ObservableFieldTest.TestParcel>(new ObservableFieldTest.TestParcel());
        target.test.get().value = "test";
        ObservableFieldTest.ObservableTestTarget output = Parcels.unwrap(ParcelsTestUtil.wrap(target));
        Assert.assertEquals("test", output.test.get().value);
    }
}
