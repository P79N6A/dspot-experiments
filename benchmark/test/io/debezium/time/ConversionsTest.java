/**
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.time;


import Conversions.EPOCH;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 *
 * @author Randall Hauch
 */
public class ConversionsTest {
    @Test
    public void shouldReturnNullIfNullIsSupplied() {
        assertThat(Conversions.toLocalDate(null)).isNull();
        assertThat(Conversions.toLocalDateTime(null)).isNull();
        assertThat(Conversions.toLocalTime(null)).isNull();
    }

    @Test
    public void shouldReturnSameLocalDateInstanceWhenConvertingToLocalDate() {
        LocalDate now = LocalDate.now();
        assertThat(Conversions.toLocalDate(now)).isSameAs(now);
    }

    @Test
    public void shouldReturnLocalDateInstanceWhenConvertingLocalDateTimeToLocalDate() {
        LocalDateTime now = LocalDateTime.now();
        assertThat(Conversions.toLocalDate(now)).isEqualTo(now.toLocalDate());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void shouldReturnLocalDateInstanceWhenConvertingUtilDateToLocalDate() {
        LocalDate now = LocalDate.now();
        Date date = new Date(((now.getYear()) - 1900), ((now.getMonthValue()) - 1), now.getDayOfMonth());
        assertThat(Conversions.toLocalDate(date)).isEqualTo(now);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void shouldReturnLocalDateInstanceWhenConvertingSqlDateToLocalDate() {
        LocalDate now = LocalDate.now();
        java.sql.Date date = new java.sql.Date(((now.getYear()) - 1900), ((now.getMonthValue()) - 1), now.getDayOfMonth());
        assertThat(Conversions.toLocalDate(date)).isEqualTo(now);
    }

    @Test
    public void shouldThrowExceptionWhenConvertingSqlTimeToLocalDate() {
        Time time = new Time(1);
        try {
            Conversions.toLocalDate(time);
            Assert.fail("Should not accept java.sql.Time values");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void shouldReturnLocalDateInstanceWhenConvertingLongToLocalDate() {
        LocalDate now = LocalDate.now();
        long epochDay = now.toEpochDay();
        assertThat(Conversions.toLocalDate(epochDay)).isEqualTo(now);
    }

    @Test
    public void shouldReturnSameLocalDateTimeInstanceWhenConvertingToLocalDateTime() {
        LocalDateTime now = LocalDateTime.now();
        assertThat(Conversions.toLocalDateTime(now)).isSameAs(now);
    }

    @Test
    public void shouldReturnLocalDateTimeInstanceWhenConvertingLocalDateToLocalDateTime() {
        LocalDate now = LocalDate.now();
        assertThat(Conversions.toLocalDateTime(now)).isEqualTo(LocalDateTime.of(now, LocalTime.MIDNIGHT));
    }

    @Test
    public void shouldReturnLocalDateTimeInstanceWhenConvertingLocalTimeToLocalDateTime() {
        LocalTime now = LocalTime.now();
        assertThat(Conversions.toLocalDateTime(now)).isEqualTo(LocalDateTime.of(EPOCH, now));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void shouldReturnLocalDateTimeInstanceWhenConvertingUtilTimeToLocalDateTime() {
        LocalDateTime now = LocalDateTime.now();
        Date date = new Date(((now.getYear()) - 1900), ((now.getMonthValue()) - 1), now.getDayOfMonth(), now.getHour(), now.getMinute(), now.getSecond());// 0 nanos!

        assertThat(Conversions.toLocalDateTime(date)).isEqualTo(now.withNano(0));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void shouldReturnLocalDateTimeInstanceWhenConvertingSqlDateToLocalDateTime() {
        LocalDate now = LocalDate.now();
        java.sql.Date date = new java.sql.Date(((now.getYear()) - 1900), ((now.getMonthValue()) - 1), now.getDayOfMonth());
        assertThat(Conversions.toLocalDateTime(date)).isEqualTo(LocalDateTime.of(now, LocalTime.MIDNIGHT));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void shouldReturnLocalDateTimeInstanceWhenConvertingSqlTimeToLocalDateTime() {
        LocalTime now = LocalTime.now();
        Time time = new Time(now.getHour(), now.getMinute(), now.getSecond());// 0 nanos!

        assertThat(Conversions.toLocalDateTime(time)).isEqualTo(LocalDateTime.of(EPOCH, now.withNano(0)));
    }

    @Test
    public void shouldReturnLocalDateTimeInstanceWhenConvertingLongToLocalDateTime() {
        try {
            Conversions.toLocalDateTime(Long.valueOf(1));
            Assert.fail("Should not accept Long values");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void shouldReturnSameLocalTimeInstanceWhenConvertingToLocalTime() {
        LocalTime now = LocalTime.now();
        assertThat(Conversions.toLocalTime(now)).isSameAs(now);
    }

    @Test
    public void shouldReturnLocalTimeInstanceWhenConvertingLocalDateTimeToLocalTime() {
        LocalDateTime now = LocalDateTime.now();
        assertThat(Conversions.toLocalTime(now)).isEqualTo(now.toLocalTime());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void shouldReturnLocalTimeInstanceWhenConvertingUtilTimeToLocalTime() {
        LocalTime now = LocalTime.now();
        Date date = new Date(0, 0, 1, now.getHour(), now.getMinute(), now.getSecond());// 0 nanos!

        assertThat(Conversions.toLocalTime(date)).isEqualTo(now.withNano(0));
    }

    @Test
    public void shouldThrowExceptionWhenConvertingSqlDateToLocalTime() {
        java.sql.Date date = new java.sql.Date(1);
        try {
            Conversions.toLocalTime(date);
            Assert.fail("Should not accept java.sql.Date values");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void shouldReturnLocalTimeInstanceWhenConvertingSqlTimeToLocalTime() {
        LocalTime now = LocalTime.now();
        Time time = new Time(now.getHour(), now.getMinute(), now.getSecond());// 0 nanos!

        assertThat(Conversions.toLocalTime(time)).isEqualTo(now.withNano(0));
    }

    @Test
    public void shouldReturnLocalTimeInstanceWhenConvertingLongToLocalTime() {
        LocalTime now = LocalTime.now();
        long nanoOfDay = now.toNanoOfDay();
        assertThat(Conversions.toLocalTime(nanoOfDay)).isEqualTo(now);
    }
}
