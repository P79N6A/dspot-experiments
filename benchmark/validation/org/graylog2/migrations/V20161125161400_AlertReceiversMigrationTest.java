/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog2.migrations;


import EmailAlarmCallback.CK_EMAIL_RECEIVERS;
import EmailAlarmCallback.CK_USER_RECEIVERS;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.graylog2.alarmcallbacks.AlarmCallbackConfiguration;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationImpl;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationService;
import org.graylog2.alarmcallbacks.EmailAlarmCallback;
import org.graylog2.alarmcallbacks.HTTPAlarmCallback;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamImpl;
import org.graylog2.streams.StreamService;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;


public class V20161125161400_AlertReceiversMigrationTest {
    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    private V20161125161400_AlertReceiversMigration alertReceiversMigration;

    @Mock
    private ClusterConfigService clusterConfigService;

    @Mock
    private StreamService streamService;

    @Mock
    private AlarmCallbackConfigurationService alarmCallbackConfigurationService;

    @Mock
    private DBCollection dbCollection;

    @Test
    public void doNotMigrateAnythingWithoutStreams() throws Exception {
        Mockito.when(this.streamService.loadAll()).thenReturn(Collections.emptyList());
        this.alertReceiversMigration.upgrade();
        Mockito.verify(this.alarmCallbackConfigurationService, Mockito.never()).getForStream(ArgumentMatchers.any());
        Mockito.verify(this.dbCollection, Mockito.never()).update(ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyMigrationCompletedWasPosted();
    }

    @Test
    public void doNotMigrateAnythingWithoutQualifyingStreams() throws Exception {
        final Stream stream1 = Mockito.mock(Stream.class);
        Mockito.when(stream1.getAlertReceivers()).thenReturn(Collections.emptyMap());
        final Stream stream2 = Mockito.mock(Stream.class);
        Mockito.when(stream2.getAlertReceivers()).thenReturn(ImmutableMap.of("users", Collections.emptyList(), "emails", Collections.emptyList()));
        Mockito.when(this.streamService.loadAll()).thenReturn(ImmutableList.of(stream1, stream2));
        this.alertReceiversMigration.upgrade();
        Mockito.verify(this.streamService, Mockito.never()).getAlertConditions(ArgumentMatchers.any());
        Mockito.verify(this.alarmCallbackConfigurationService, Mockito.never()).getForStream(ArgumentMatchers.any());
        Mockito.verify(this.alarmCallbackConfigurationService, Mockito.never()).save(ArgumentMatchers.any());
        Mockito.verify(this.dbCollection, Mockito.never()).update(ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyMigrationCompletedWasPosted();
    }

    @Test
    public void doMigrateSingleQualifyingStream() throws Exception {
        final String matchingStreamId = new ObjectId().toHexString();
        final Stream stream1 = Mockito.mock(Stream.class);
        Mockito.when(stream1.getAlertReceivers()).thenReturn(Collections.emptyMap());
        final Stream stream2 = Mockito.mock(Stream.class);
        Mockito.when(stream2.getAlertReceivers()).thenReturn(ImmutableMap.of("users", ImmutableList.of("foouser"), "emails", ImmutableList.of("foo@bar.com")));
        Mockito.when(stream2.getId()).thenReturn(matchingStreamId);
        Mockito.when(this.streamService.loadAll()).thenReturn(ImmutableList.of(stream1, stream2));
        final AlertCondition alertCondition = Mockito.mock(AlertCondition.class);
        Mockito.when(this.streamService.getAlertConditions(ArgumentMatchers.eq(stream2))).thenReturn(ImmutableList.of(alertCondition));
        final String alarmCallbackId = new ObjectId().toHexString();
        final AlarmCallbackConfiguration alarmCallback = AlarmCallbackConfigurationImpl.create(alarmCallbackId, matchingStreamId, EmailAlarmCallback.class.getCanonicalName(), "Email Alert Notification", new HashMap(), new Date(), "admin");
        Mockito.when(alarmCallbackConfigurationService.getForStream(ArgumentMatchers.eq(stream2))).thenReturn(ImmutableList.of(alarmCallback));
        Mockito.when(alarmCallbackConfigurationService.save(ArgumentMatchers.eq(alarmCallback))).thenReturn(alarmCallbackId);
        Mockito.when(this.dbCollection.update(ArgumentMatchers.any(BasicDBObject.class), ArgumentMatchers.any(BasicDBObject.class))).thenReturn(new WriteResult(1, true, matchingStreamId));
        this.alertReceiversMigration.upgrade();
        final ArgumentCaptor<AlarmCallbackConfiguration> configurationArgumentCaptor = ArgumentCaptor.forClass(AlarmCallbackConfiguration.class);
        Mockito.verify(this.alarmCallbackConfigurationService, Mockito.times(1)).save(configurationArgumentCaptor.capture());
        final AlarmCallbackConfiguration updatedConfiguration = configurationArgumentCaptor.getValue();
        assertThat(updatedConfiguration).isEqualTo(alarmCallback);
        assertThat(updatedConfiguration.getType()).isEqualTo(EmailAlarmCallback.class.getCanonicalName());
        assertThat(((List) (updatedConfiguration.getConfiguration().get(CK_EMAIL_RECEIVERS))).size()).isEqualTo(1);
        assertThat(((List) (updatedConfiguration.getConfiguration().get(CK_EMAIL_RECEIVERS))).get(0)).isEqualTo("foo@bar.com");
        assertThat(((List) (updatedConfiguration.getConfiguration().get(CK_USER_RECEIVERS))).size()).isEqualTo(1);
        assertThat(((List) (updatedConfiguration.getConfiguration().get(CK_USER_RECEIVERS))).get(0)).isEqualTo("foouser");
        final ArgumentCaptor<BasicDBObject> queryCaptor = ArgumentCaptor.forClass(BasicDBObject.class);
        final ArgumentCaptor<BasicDBObject> updateCaptor = ArgumentCaptor.forClass(BasicDBObject.class);
        Mockito.verify(this.dbCollection, Mockito.times(1)).update(queryCaptor.capture(), updateCaptor.capture());
        assertThat(queryCaptor.getValue().toJson()).isEqualTo((("{ \"_id\" : { \"$oid\" : \"" + matchingStreamId) + "\" } }"));
        assertThat(updateCaptor.getValue().toJson()).isEqualTo((("{ \"$unset\" : { \"" + (StreamImpl.FIELD_ALERT_RECEIVERS)) + "\" : \"\" } }"));
        verifyMigrationCompletedWasPosted(ImmutableMap.of(matchingStreamId, Optional.of(alarmCallbackId)));
    }

    @Test
    public void doMigrateMultipleQualifyingStreams() throws Exception {
        final String matchingStreamId1 = new ObjectId().toHexString();
        final String matchingStreamId2 = new ObjectId().toHexString();
        final Stream stream1 = Mockito.mock(Stream.class);
        Mockito.when(stream1.getAlertReceivers()).thenReturn(Collections.emptyMap());
        final Stream stream2 = Mockito.mock(Stream.class);
        Mockito.when(stream2.getAlertReceivers()).thenReturn(ImmutableMap.of("users", ImmutableList.of("foouser"), "emails", ImmutableList.of("foo@bar.com")));
        Mockito.when(stream2.getId()).thenReturn(matchingStreamId1);
        final Stream stream3 = Mockito.mock(Stream.class);
        Mockito.when(stream3.getAlertReceivers()).thenReturn(ImmutableMap.of("users", ImmutableList.of("foouser2")));
        Mockito.when(stream3.getId()).thenReturn(matchingStreamId2);
        Mockito.when(this.streamService.loadAll()).thenReturn(ImmutableList.of(stream1, stream2, stream3));
        final AlertCondition alertCondition1 = Mockito.mock(AlertCondition.class);
        final AlertCondition alertCondition2 = Mockito.mock(AlertCondition.class);
        Mockito.when(this.streamService.getAlertConditions(ArgumentMatchers.eq(stream2))).thenReturn(ImmutableList.of(alertCondition1));
        Mockito.when(this.streamService.getAlertConditions(ArgumentMatchers.eq(stream3))).thenReturn(ImmutableList.of(alertCondition2));
        final String alarmCallbackId1 = new ObjectId().toHexString();
        final AlarmCallbackConfiguration alarmCallback1 = AlarmCallbackConfigurationImpl.create(alarmCallbackId1, matchingStreamId1, EmailAlarmCallback.class.getCanonicalName(), "Email Alert Notification", new HashMap(), new Date(), "admin");
        final String alarmCallbackId2 = new ObjectId().toHexString();
        final AlarmCallbackConfiguration alarmCallback2 = AlarmCallbackConfigurationImpl.create(alarmCallbackId2, matchingStreamId2, EmailAlarmCallback.class.getCanonicalName(), "Email Alert Notification", new HashMap(), new Date(), "admin");
        final String alarmCallbackId3 = new ObjectId().toHexString();
        final AlarmCallbackConfiguration alarmCallback3 = AlarmCallbackConfigurationImpl.create(alarmCallbackId3, matchingStreamId2, EmailAlarmCallback.class.getCanonicalName(), "Email Alert Notification", new HashMap(), new Date(), "admin");
        final String alarmCallbackId4 = new ObjectId().toHexString();
        final AlarmCallbackConfiguration alarmCallback4 = AlarmCallbackConfigurationImpl.create(alarmCallbackId4, matchingStreamId2, HTTPAlarmCallback.class.getCanonicalName(), "Email Alert Notification", new HashMap(), new Date(), "admin");
        Mockito.when(alarmCallbackConfigurationService.getForStream(ArgumentMatchers.eq(stream2))).thenReturn(ImmutableList.of(alarmCallback1));
        Mockito.when(alarmCallbackConfigurationService.getForStream(ArgumentMatchers.eq(stream3))).thenReturn(ImmutableList.of(alarmCallback2, alarmCallback3, alarmCallback4));
        Mockito.when(alarmCallbackConfigurationService.save(ArgumentMatchers.eq(alarmCallback1))).thenReturn(alarmCallbackId1);
        Mockito.when(alarmCallbackConfigurationService.save(ArgumentMatchers.eq(alarmCallback2))).thenReturn(alarmCallbackId2);
        Mockito.when(alarmCallbackConfigurationService.save(ArgumentMatchers.eq(alarmCallback3))).thenReturn(alarmCallbackId3);
        Mockito.when(this.dbCollection.update(ArgumentMatchers.any(BasicDBObject.class), ArgumentMatchers.any(BasicDBObject.class))).thenReturn(new WriteResult(1, true, matchingStreamId1));
        Mockito.when(this.dbCollection.update(ArgumentMatchers.any(BasicDBObject.class), ArgumentMatchers.any(BasicDBObject.class))).thenReturn(new WriteResult(1, true, matchingStreamId2));
        this.alertReceiversMigration.upgrade();
        final ArgumentCaptor<AlarmCallbackConfiguration> configurationArgumentCaptor = ArgumentCaptor.forClass(AlarmCallbackConfiguration.class);
        Mockito.verify(this.alarmCallbackConfigurationService, Mockito.times(3)).save(configurationArgumentCaptor.capture());
        final List<AlarmCallbackConfiguration> configurationValues = configurationArgumentCaptor.getAllValues();
        assertThat(configurationValues).isNotNull().isNotEmpty().hasSize(3).contains(alarmCallback1).contains(alarmCallback2).contains(alarmCallback3);
        for (AlarmCallbackConfiguration configurationValue : configurationValues) {
            if (configurationValue.getStreamId().equals(matchingStreamId1)) {
                assertThat(((List) (configurationValue.getConfiguration().get(CK_EMAIL_RECEIVERS))).size()).isEqualTo(1);
                assertThat(((List) (configurationValue.getConfiguration().get(CK_EMAIL_RECEIVERS))).get(0)).isEqualTo("foo@bar.com");
                assertThat(((List) (configurationValue.getConfiguration().get(CK_USER_RECEIVERS))).size()).isEqualTo(1);
                assertThat(((List) (configurationValue.getConfiguration().get(CK_USER_RECEIVERS))).get(0)).isEqualTo("foouser");
            }
            if (configurationValue.getStreamId().equals(matchingStreamId2)) {
                assertThat(configurationValue.getConfiguration().get(CK_EMAIL_RECEIVERS)).isNull();
                assertThat(((List) (configurationValue.getConfiguration().get(CK_USER_RECEIVERS))).size()).isEqualTo(1);
                assertThat(((List) (configurationValue.getConfiguration().get(CK_USER_RECEIVERS))).get(0)).isEqualTo("foouser2");
            }
        }
        final ArgumentCaptor<BasicDBObject> queryCaptor = ArgumentCaptor.forClass(BasicDBObject.class);
        final ArgumentCaptor<BasicDBObject> updateCaptor = ArgumentCaptor.forClass(BasicDBObject.class);
        Mockito.verify(this.dbCollection, Mockito.times(2)).update(queryCaptor.capture(), updateCaptor.capture());
        final List<BasicDBObject> queries = queryCaptor.getAllValues();
        for (BasicDBObject query : queries) {
            final String streamId = ((queries.indexOf(query)) == 0) ? matchingStreamId1 : matchingStreamId2;
            assertThat(query.toJson()).isEqualTo((("{ \"_id\" : { \"$oid\" : \"" + streamId) + "\" } }"));
        }
        updateCaptor.getAllValues().forEach(( update) -> assertThat(update.toJson()).isEqualTo((("{ \"$unset\" : { \"" + StreamImpl.FIELD_ALERT_RECEIVERS) + "\" : \"\" } }")));
        verifyMigrationCompletedWasPosted(ImmutableMap.of(matchingStreamId1, Optional.of(alarmCallbackId1), matchingStreamId2, Optional.of(((alarmCallbackId2 + ", ") + alarmCallbackId3))));
    }
}
