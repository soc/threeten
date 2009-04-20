/*
 * Copyright (c) 2007-2009, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javax.time.calendar;

import static org.testng.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.time.CalendricalException;
import javax.time.calendar.field.DayOfMonth;
import javax.time.calendar.field.DayOfWeek;
import javax.time.calendar.field.DayOfYear;
import javax.time.calendar.field.MonthOfYear;
import javax.time.calendar.field.QuarterOfYear;
import javax.time.calendar.field.WeekBasedYear;
import javax.time.calendar.field.WeekOfWeekBasedYear;
import javax.time.calendar.field.Year;
import javax.time.calendar.format.CalendricalParseException;
import javax.time.period.MockPeriodProviderReturnsNull;
import javax.time.period.Period;
import javax.time.period.PeriodProvider;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test LocalDate.
 *
 * @author Michael Nascimento Santos
 * @author Stephen Colebourne
 */
@Test
public class TestLocalDate {

    private static final String MIN_YEAR_STR = Integer.toString(Year.MIN_YEAR);
    private static final String MAX_YEAR_STR = Integer.toString(Year.MAX_YEAR);
    private static final ZoneOffset OFFSET_PTWO = ZoneOffset.zoneOffset(2);
    private static final TimeZone ZONE_PARIS = TimeZone.timeZone("Europe/Paris");
    private static final TimeZone ZONE_GAZA = TimeZone.timeZone("Asia/Gaza");
    
    private LocalDate TEST_2007_07_15;
    private long MAX_VALID_EPOCHDAYS;
    private long MIN_VALID_EPOCHDAYS;
    private long MAX_VALID_MJDAYS;
    private long MIN_VALID_MJDAYS;

    @BeforeMethod
    public void setUp() {
        TEST_2007_07_15 = LocalDate.date(2007, 7, 15);
        
        LocalDate max = LocalDate.date(Year.MAX_YEAR, 12, 31);
        LocalDate min = LocalDate.date(Year.MIN_YEAR, 1, 1);
        MAX_VALID_EPOCHDAYS = max.toEpochDays();
        MIN_VALID_EPOCHDAYS = min.toEpochDays();
        MAX_VALID_MJDAYS = max.toModifiedJulianDays();
        MIN_VALID_MJDAYS = min.toModifiedJulianDays();
    }

    //-----------------------------------------------------------------------
    public void test_interfaces() {
        assertTrue(TEST_2007_07_15 instanceof CalendricalProvider);
        assertTrue(TEST_2007_07_15 instanceof Serializable);
        assertTrue(TEST_2007_07_15 instanceof Comparable);
        assertTrue(TEST_2007_07_15 instanceof DateProvider);
        assertTrue(TEST_2007_07_15 instanceof DateMatcher);
    }

    public void test_serialization() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(TEST_2007_07_15);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                baos.toByteArray()));
        assertEquals(ois.readObject(), TEST_2007_07_15);
    }

    public void test_immutable() {
        Class<LocalDate> cls = LocalDate.class;
        assertTrue(Modifier.isPublic(cls.getModifiers()));
        assertTrue(Modifier.isFinal(cls.getModifiers()));
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            assertTrue(Modifier.isPrivate(field.getModifiers()));
            assertTrue(Modifier.isFinal(field.getModifiers()));
        }
    }

    //-----------------------------------------------------------------------
    private void check(LocalDate test_2008_02_29, int y, int m, int d) {
        assertEquals(test_2008_02_29.getYear(), y);
        assertEquals(test_2008_02_29.getMonthOfYear().getValue(), m);
        assertEquals(test_2008_02_29.getDayOfMonth(), d);
    }

    public void factory_date_objects() {
        assertEquals(TEST_2007_07_15, LocalDate.date(Year.isoYear(2007), MonthOfYear.JULY, DayOfMonth.dayOfMonth(15)));
    }

    public void factory_date_objects_leapYear() {
        LocalDate test_2008_02_29 = LocalDate.date(Year.isoYear(2008), MonthOfYear.FEBRUARY, DayOfMonth.dayOfMonth(29));
        check(test_2008_02_29, 2008, 2, 29);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_date_objects_nullYear() {
        LocalDate.date(null, MonthOfYear.JULY, DayOfMonth.dayOfMonth(15));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_date_objects_nullMonth() {
        LocalDate.date(Year.isoYear(2007), null, DayOfMonth.dayOfMonth(15));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_date_objects_nullDay() {
        LocalDate.date(Year.isoYear(2007), MonthOfYear.JULY, null);
    }

    @Test(expectedExceptions=InvalidCalendarFieldException.class)
    public void factory_date_objects_nonleapYear() {
        LocalDate.date(Year.isoYear(2007), MonthOfYear.FEBRUARY, DayOfMonth.dayOfMonth(29));
    }

    @Test(expectedExceptions=InvalidCalendarFieldException.class)
    public void factory_date_objects_dayTooBig() {
        LocalDate.date(Year.isoYear(2007), MonthOfYear.APRIL, DayOfMonth.dayOfMonth(31));
    }

    //-----------------------------------------------------------------------
    public void factory_date_intsMonth() {
        assertEquals(TEST_2007_07_15, LocalDate.date(2007, MonthOfYear.JULY, 15));
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_date_intsMonth_dayTooLow() {
        LocalDate.date(2007, MonthOfYear.JANUARY, 0);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_date_intsMonth_dayTooHigh() {
        LocalDate.date(2007, MonthOfYear.JANUARY, 32);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_date_intsMonth_nullMonth() {
        LocalDate.date(2007, null, 30);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_date_intsMonth_yearTooLow() {
        LocalDate.date(Integer.MIN_VALUE, MonthOfYear.JANUARY, 1);
    }

    //-----------------------------------------------------------------------
    public void factory_date_ints() {
        check(TEST_2007_07_15, 2007, 7, 15);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_date_ints_dayTooLow() {
        LocalDate.date(2007, 1, 0);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_date_ints_dayTooHigh() {
        LocalDate.date(2007, 1, 32);
    }


    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_date_ints_monthTooLow() {
        LocalDate.date(2007, 0, 1);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_date_ints_monthTooHigh() {
        LocalDate.date(2007, 13, 1);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_date_ints_yearTooLow() {
        LocalDate.date(Integer.MIN_VALUE, 1, 1);
    }

    //-----------------------------------------------------------------------
    public void factory_date_DateProvider() {
        assertSame(LocalDate.date(TEST_2007_07_15), TEST_2007_07_15);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_date_DateProvider_null() {
        LocalDate.date(null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_date_DateProvider_null_toLocalDate() {
        LocalDate.date(new MockDateProviderReturnsNull());
    }

    //-----------------------------------------------------------------------
    public void factory_date_multiProvider_checkAmbiguous() {
        MockMultiProvider mmp = new MockMultiProvider(2008, 6, 30, 11, 30, 10, 500);
        LocalDate test = LocalDate.date(mmp);
        check(test, 2008, 6, 30);
    }

    //-----------------------------------------------------------------------
    // Since plusDays/minusDays actually depends on MJDays, it cannot be used for testing
    private LocalDate next(LocalDate date) {
        int newDayOfMonth = date.getDayOfMonth() + 1;
        if (newDayOfMonth <= date.getMonthOfYear().lengthInDays(date.getYear())) {
            return date.withDayOfMonth(newDayOfMonth);
        }
        date = date.withDayOfMonth(1);
        if (date.getMonthOfYear() == MonthOfYear.DECEMBER) {
            date = date.with(date.toYear().next());
        }
        return date.with(date.getMonthOfYear().next());
    }

    private LocalDate previous(LocalDate date) {
        int newDayOfMonth = date.getDayOfMonth() - 1;
        if (newDayOfMonth > 0) {
            return date.withDayOfMonth(newDayOfMonth);
        }
        date = date.with(date.getMonthOfYear().previous());
        if (date.getMonthOfYear() == MonthOfYear.DECEMBER) {
            date = date.with(date.toYear().previous());
        }
        return date.with(date.getMonthOfYear().getLastDayOfMonth(date.toYear()));
    }

    //-----------------------------------------------------------------------
    // fromEpochDays()
    //-----------------------------------------------------------------------
    public void factory_fromEpochDays() {
        long date_0000_01_01 = -678941 - 40587;
        assertEquals(LocalDate.fromEpochDays(0), LocalDate.date(1970, 1, 1));
        assertEquals(LocalDate.fromEpochDays(date_0000_01_01), LocalDate.date(0, 1, 1));
        assertEquals(LocalDate.fromEpochDays(date_0000_01_01 - 1), LocalDate.date(-1, 12, 31));
        assertEquals(LocalDate.fromEpochDays(MAX_VALID_EPOCHDAYS), LocalDate.date(Year.MAX_YEAR, 12, 31));
        assertEquals(LocalDate.fromEpochDays(MIN_VALID_EPOCHDAYS), LocalDate.date(Year.MIN_YEAR, 1, 1));
        
        LocalDate test = LocalDate.date(0, 1, 1);
        for (long i = date_0000_01_01; i < 700000; i++) {
            assertEquals(LocalDate.fromEpochDays(i), test);
            test = next(test);
        }
        test = LocalDate.date(0, 1, 1);
        for (long i = date_0000_01_01; i > -2000000; i--) {
            assertEquals(LocalDate.fromEpochDays(i), test);
            test = previous(test);
        }
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_fromEpochDays_aboveMax() {
        LocalDate.fromEpochDays(MAX_VALID_EPOCHDAYS + 1);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_fromEpochDays_belowMin() {
        LocalDate.fromEpochDays(MIN_VALID_EPOCHDAYS - 1);
    }

    //-----------------------------------------------------------------------
    // fromModifiedJulianDays()
    //-----------------------------------------------------------------------
    public void factory_fromModifiedJulianDays() {
        long date_0000_01_01 = -678941;
        assertEquals(LocalDate.fromModifiedJulianDays(40587), LocalDate.date(1970, 1, 1));
        assertEquals(LocalDate.fromModifiedJulianDays(date_0000_01_01), LocalDate.date(0, 1, 1));
        assertEquals(LocalDate.fromModifiedJulianDays(date_0000_01_01 - 1), LocalDate.date(-1, 12, 31));
        assertEquals(LocalDate.fromModifiedJulianDays(MAX_VALID_MJDAYS), LocalDate.date(Year.MAX_YEAR, 12, 31));
        assertEquals(LocalDate.fromModifiedJulianDays(MIN_VALID_MJDAYS), LocalDate.date(Year.MIN_YEAR, 1, 1));
        
        LocalDate test = LocalDate.date(0, 1, 1);
        for (long i = date_0000_01_01; i < 700000; i++) {
            assertEquals(LocalDate.fromModifiedJulianDays(i), test);
            test = next(test);
        }
        test = LocalDate.date(0, 1, 1);
        for (long i = date_0000_01_01; i > -2000000; i--) {
            assertEquals(LocalDate.fromModifiedJulianDays(i), test);
            test = previous(test);
        }
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_fromModifiedJulianDays_aboveMax() {
        LocalDate.fromModifiedJulianDays(MAX_VALID_MJDAYS + 1);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_fromModifiedJulianDays_belowMin() {
        LocalDate.fromModifiedJulianDays(MIN_VALID_MJDAYS - 1);
    }

    //-----------------------------------------------------------------------
    // parse()
    //-----------------------------------------------------------------------
    @Test(dataProvider="sampleToString")
    public void factory_parse_validText(int y, int m, int d, String parsable) {
        LocalDate t = LocalDate.parse(parsable);
        assertNotNull(t, parsable);
        assertEquals(t.getYear(), y, parsable);
        assertEquals(t.getMonthOfYear().getValue(), m, parsable);
        assertEquals(t.getDayOfMonth(), d, parsable);
    }

    @DataProvider(name="sampleBadParse")
    Object[][] provider_sampleBadParse() {
        return new Object[][]{
                {"2008/07/05"},
                {"10000-01-01"},
                {"2008-1-1"},
                {"2008--01"},
                {"ABCD-02-01"},
                {"2008-AB-01"},
                {"2008-02-AB"},
                {"-0000-02-01"},
                {"2008-02-01Z"},
                {"2008-02-01+01:00"},
                {"2008-02-01+01:00[Europe/Paris]"},
        };
    }

    @Test(dataProvider="sampleBadParse", expectedExceptions={CalendricalParseException.class})
    public void factory_parse_invalidText(String unparsable) {
        LocalDate.parse(unparsable);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_parse_illegalValue() {
        LocalDate.parse("2008-06-32");
    }

    @Test(expectedExceptions=InvalidCalendarFieldException.class)
    public void factory_parse_invalidValue() {
        LocalDate.parse("2008-06-31");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_parse_nullText() {
        LocalDate.parse((String) null);
    }

    //-----------------------------------------------------------------------
    // getChronology()
    //-----------------------------------------------------------------------
    public void test_getChronology() {
        assertSame(ISOChronology.INSTANCE, TEST_2007_07_15.getChronology());
    }

    //-----------------------------------------------------------------------
    // isSupported(DateTimeFieldRule)
    //-----------------------------------------------------------------------
    public void test_isSupported() {
        assertTrue(TEST_2007_07_15.isSupported(ISOChronology.yearRule()));
        assertTrue(TEST_2007_07_15.isSupported(ISOChronology.quarterOfYearRule()));
        assertTrue(TEST_2007_07_15.isSupported(ISOChronology.monthOfYearRule()));
        assertTrue(TEST_2007_07_15.isSupported(ISOChronology.monthOfQuarterRule()));
        assertTrue(TEST_2007_07_15.isSupported(ISOChronology.dayOfMonthRule()));
        assertTrue(TEST_2007_07_15.isSupported(ISOChronology.dayOfWeekRule()));
        assertTrue(TEST_2007_07_15.isSupported(ISOChronology.dayOfYearRule()));
        assertTrue(TEST_2007_07_15.isSupported(ISOChronology.weekOfMonthRule()));
        assertTrue(TEST_2007_07_15.isSupported(ISOChronology.weekOfWeekBasedYearRule()));
        assertTrue(TEST_2007_07_15.isSupported(ISOChronology.weekBasedYearRule()));
        
        assertFalse(TEST_2007_07_15.isSupported(ISOChronology.hourOfDayRule()));
        assertFalse(TEST_2007_07_15.isSupported(ISOChronology.minuteOfHourRule()));
        assertFalse(TEST_2007_07_15.isSupported(ISOChronology.secondOfMinuteRule()));
        assertFalse(TEST_2007_07_15.isSupported(ISOChronology.nanoOfSecondRule()));
        assertFalse(TEST_2007_07_15.isSupported(ISOChronology.hourOfAmPmRule()));
        assertFalse(TEST_2007_07_15.isSupported(ISOChronology.amPmOfDayRule()));
        
        assertFalse(TEST_2007_07_15.isSupported(null));
    }

    //-----------------------------------------------------------------------
    // get(DateTimeFieldRule)
    //-----------------------------------------------------------------------
    public void test_get() {
        assertEquals(TEST_2007_07_15.get(ISOChronology.yearRule()), TEST_2007_07_15.getYear());
        assertEquals(TEST_2007_07_15.get(ISOChronology.quarterOfYearRule()), TEST_2007_07_15.getMonthOfYear().getQuarterOfYear().getValue());
        assertEquals(TEST_2007_07_15.get(ISOChronology.monthOfYearRule()), TEST_2007_07_15.getMonthOfYear().getValue());
        assertEquals(TEST_2007_07_15.get(ISOChronology.monthOfQuarterRule()), TEST_2007_07_15.getMonthOfYear().getMonthOfQuarter());
        assertEquals(TEST_2007_07_15.get(ISOChronology.dayOfMonthRule()), TEST_2007_07_15.getDayOfMonth());
        assertEquals(TEST_2007_07_15.get(ISOChronology.dayOfWeekRule()), TEST_2007_07_15.getDayOfWeek().getValue());
        assertEquals(TEST_2007_07_15.get(ISOChronology.dayOfYearRule()), TEST_2007_07_15.getDayOfYear());
        assertEquals(TEST_2007_07_15.get(ISOChronology.weekOfWeekBasedYearRule()), WeekOfWeekBasedYear.weekOfWeekyear(TEST_2007_07_15).getValue());
        assertEquals(TEST_2007_07_15.get(ISOChronology.weekBasedYearRule()), WeekBasedYear.weekyear(TEST_2007_07_15).getValue());
    }

    @Test(expectedExceptions=UnsupportedCalendarFieldException.class)
    public void test_get_unsupported() {
        TEST_2007_07_15.get(ISOChronology.hourOfDayRule());
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="sampleDates")
    Object[][] provider_sampleDates() {
        return new Object[][] {
            {2008, 7, 5},
            {2007, 7, 5},
            {2006, 7, 5},
            {2005, 7, 5},
            {2004, 1, 1},
            {-1, 1, 2},
        };
    }

    //-----------------------------------------------------------------------
    // get*()
    //-----------------------------------------------------------------------
    @Test(dataProvider="sampleDates")
    public void test_getYearMonth(int y, int m, int d) {
        assertEquals(LocalDate.date(y, m, d).getYearMonth(), YearMonth.yearMonth(y, m));
    }

    @Test(dataProvider="sampleDates")
    public void test_getMonthDay(int y, int m, int d) {
        assertEquals(LocalDate.date(y, m, d).getMonthDay(), MonthDay.monthDay(m, d));
    }

    @Test(dataProvider="sampleDates")
    public void test_get(int y, int m, int d) {
        LocalDate a = LocalDate.date(y, m, d);
        assertEquals(a.getYear(), y);
        assertEquals(a.getMonthOfYear(), MonthOfYear.monthOfYear(m));
        assertEquals(a.getDayOfMonth(), d);
        
        assertEquals(a.toYear(), Year.isoYear(y));
        assertEquals(a.toMonthOfYear(), MonthOfYear.monthOfYear(m));
        assertEquals(a.toDayOfMonth(), DayOfMonth.dayOfMonth(d));
    }

    @Test(dataProvider="sampleDates")
    public void test_getDOY(int y, int m, int d) {
        LocalDate a = LocalDate.date(y, m, d);
        int total = 0;
        for (int i = 1; i < m; i++) {
            total += MonthOfYear.monthOfYear(i).lengthInDays(y);
        }
        int doy = total + d;
        assertEquals(a.getDayOfYear(), doy);
        assertEquals(a.toDayOfYear(), DayOfYear.dayOfYear(doy));
    }

    //-----------------------------------------------------------------------
    // getDayOfWeek()
    //-----------------------------------------------------------------------
    public void test_getDayOfWeek() {
        DayOfWeek dow = DayOfWeek.MONDAY;
        for (MonthOfYear month : MonthOfYear.values()) {
            int length = month.lengthInDays(2007);
            for (int i = 1; i <= length; i++) {
                LocalDate d = LocalDate.date(2007, month, i);
                assertSame(d.getDayOfWeek(), dow);
                assertSame(d.toDayOfWeek(), dow);
                dow = dow.next();
            }
        }
    }

//    //-----------------------------------------------------------------------
//    // isLeapYear()
//    //-----------------------------------------------------------------------
//    public void test_isLeapYear() {
//        assertEquals(LocalDate.date(1999, 1, 1).isLeapYear(), false);
//        assertEquals(LocalDate.date(2000, 1, 1).isLeapYear(), true);
//        assertEquals(LocalDate.date(2001, 1, 1).isLeapYear(), false);
//        assertEquals(LocalDate.date(2002, 1, 1).isLeapYear(), false);
//        assertEquals(LocalDate.date(2003, 1, 1).isLeapYear(), false);
//        assertEquals(LocalDate.date(2004, 1, 1).isLeapYear(), true);
//        assertEquals(LocalDate.date(2005, 1, 1).isLeapYear(), false);
//        
//        assertEquals(LocalDate.date(1500, 1, 1).isLeapYear(), false);
//        assertEquals(LocalDate.date(1600, 1, 1).isLeapYear(), true);
//        assertEquals(LocalDate.date(1700, 1, 1).isLeapYear(), false);
//        assertEquals(LocalDate.date(1800, 1, 1).isLeapYear(), false);
//        assertEquals(LocalDate.date(1900, 1, 1).isLeapYear(), false);
//    }

    //-----------------------------------------------------------------------
    // with()
    //-----------------------------------------------------------------------
    public void test_with() {
        DateAdjuster dateAdjuster = DateAdjusters.lastDayOfMonth();
        assertEquals(TEST_2007_07_15.with(dateAdjuster), dateAdjuster.adjustDate(TEST_2007_07_15));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_with_null() {
        TEST_2007_07_15.with((DateAdjuster) null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_with_null_adjustDate() {
        TEST_2007_07_15.with(new MockDateAdjusterReturnsNull());
    }

    //-----------------------------------------------------------------------
    // withYear()
    //-----------------------------------------------------------------------
    public void test_withYear_int_normal() {
        LocalDate t = TEST_2007_07_15.withYear(2008);
        assertEquals(t, LocalDate.date(2008, 7, 15));
    }

    public void test_withYear_int_noChange() {
        LocalDate t = TEST_2007_07_15.withYear(2007);
        assertSame(t, TEST_2007_07_15);
    }
    
    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_withYear_int_invalid() {
        TEST_2007_07_15.withYear(Year.MIN_YEAR - 1);
    }

    public void test_withYear_int_adjustDay() {
        LocalDate t = LocalDate.date(2008, 2, 29).withYear(2007);
        LocalDate expected = LocalDate.date(2007, 2, 28);
        assertEquals(t, expected);
    }

    public void test_withYear_int_DateResolver_normal() {
        LocalDate t = TEST_2007_07_15.withYear(2008, DateResolvers.strict());
        assertEquals(t, LocalDate.date(2008, 7, 15));
    }

    public void test_withYear_int_DateResolver_noChange() {
        LocalDate t = TEST_2007_07_15.withYear(2007, DateResolvers.strict());
        assertSame(t, TEST_2007_07_15);
    }
    
    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_withYear_int_DateResolver_invalid() {
        TEST_2007_07_15.withYear(Year.MIN_YEAR - 1, DateResolvers.nextValid());
    }

    public void test_withYear_int_DateResolver_adjustDay() {
        LocalDate t = LocalDate.date(2008, 2, 29).withYear(2007, DateResolvers.nextValid());
        LocalDate expected = LocalDate.date(2007, 3, 1);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_withYear_int_DateResolver_null_adjustDay() {
        TEST_2007_07_15.withYear(2008, new MockDateResolverReturnsNull());
    }

    @Test(expectedExceptions=InvalidCalendarFieldException.class)
    public void test_withYear_int_DateResolver_adjustDay_invalid() {
        LocalDate.date(2008, 2, 29).withYear(2007, DateResolvers.strict());
    }

    //-----------------------------------------------------------------------
    // withMonthOfYear()
    //-----------------------------------------------------------------------
    public void test_withMonthOfYear_int_normal() {
        LocalDate t = TEST_2007_07_15.withMonthOfYear(1);
        assertEquals(t, LocalDate.date(2007, 1, 15));
    }

    public void test_withMonthOfYear_int_noChange() {
        LocalDate t = TEST_2007_07_15.withMonthOfYear(7);
        assertSame(t, TEST_2007_07_15);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_withMonthOfYear_int_invalid() {
        TEST_2007_07_15.withMonthOfYear(13);
    }

    public void test_withMonthOfYear_int_adjustDay() {
        LocalDate t = LocalDate.date(2007, 12, 31).withMonthOfYear(11);
        LocalDate expected = LocalDate.date(2007, 11, 30);
        assertEquals(t, expected);
    }

    public void test_withMonthOfYear_int_DateResolver_normal() {
        LocalDate t = TEST_2007_07_15.withMonthOfYear(1, DateResolvers.strict());
        assertEquals(t, LocalDate.date(2007, 1, 15));
    }

    public void test_withMonthOfYear_int_DateResolver_noChange() {
        LocalDate t = TEST_2007_07_15.withMonthOfYear(7, DateResolvers.strict());
        assertSame(t, TEST_2007_07_15);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_withMonthOfYear_int_DateResolver_invalid() {
        TEST_2007_07_15.withMonthOfYear(13, DateResolvers.nextValid());
    }

    public void test_withMonthOfYear_int_DateResolver_adjustDay() {
        LocalDate t = LocalDate.date(2007, 12, 31).withMonthOfYear(11, DateResolvers.nextValid());
        LocalDate expected = LocalDate.date(2007, 12, 1);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_withMonthOfYear_int_DateResolver_null_adjustDay() {
        TEST_2007_07_15.withMonthOfYear(1, new MockDateResolverReturnsNull());
    }

    @Test(expectedExceptions=InvalidCalendarFieldException.class)
    public void test_withMonthOfYear_int_DateResolver_adjustDay_invalid() {
        LocalDate.date(2007, 12, 31).withMonthOfYear(11, DateResolvers.strict());
    }

    //-----------------------------------------------------------------------
    // withDayOfMonth()
    //-----------------------------------------------------------------------
    public void test_withDayOfMonth_normal() {
        LocalDate t = TEST_2007_07_15.withDayOfMonth(1);
        assertEquals(t, LocalDate.date(2007, 7, 1));
    }

    public void test_withDayOfMonth_noChange() {
        LocalDate t = TEST_2007_07_15.withDayOfMonth(15);
        assertSame(t, TEST_2007_07_15);
    }

    @Test(expectedExceptions=InvalidCalendarFieldException.class)
    public void test_withDayOfMonth_invalid() {
        LocalDate.date(2007, 11, 30).withDayOfMonth(31);
    }

    //-----------------------------------------------------------------------
    // plus(PeriodProvider)
    //-----------------------------------------------------------------------
    public void test_plus_PeriodProvider() {
        PeriodProvider provider = Period.period(1, 2, 3, 4, 5, 6, 7);
        LocalDate t = TEST_2007_07_15.plus(provider);
        assertEquals(t, LocalDate.date(2008, 9, 18));
    }

    public void test_plus_PeriodProvider_timeIgnored() {
        PeriodProvider provider = Period.period(1, 2, 3, Integer.MAX_VALUE, 5, 6, 7);
        LocalDate t = TEST_2007_07_15.plus(provider);
        assertEquals(t, LocalDate.date(2008, 9, 18));
    }

    public void test_plus_PeriodProvider_zero() {
        LocalDate t = TEST_2007_07_15.plus(Period.ZERO);
        assertSame(t, TEST_2007_07_15);
    }

    public void test_plus_PeriodProvider_previousValidResolver_oneMonth() {
        PeriodProvider provider = Period.months(1);
        LocalDate t = LocalDate.date(2008, 1, 31).plus(provider);
        assertEquals(t, LocalDate.date(2008, 2, 29));
    }

    public void test_plus_PeriodProvider_previousValidResolver_oneMonthOneDay() {
        PeriodProvider provider = Period.yearsMonthsDays(0, 1, 1);
        LocalDate t = LocalDate.date(2008, 1, 31).plus(provider);
        assertEquals(t, LocalDate.date(2008, 3, 1));
    }

//    public void test_plus_PeriodProvider_previousValidResolver_oneMonthMinusOneDay() {
//        PeriodProvider provider = Period.yearsMonthsDays(0, 1, -1);
//        LocalDate t = LocalDate.date(2008, 1, 31).plus(provider);
//        assertEquals(t, LocalDate.date(2008, 2, 29));  // TODO: what is the correct result
//    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_plus_PeriodProvider_null() {
        TEST_2007_07_15.plus((PeriodProvider) null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_plus_PeriodProvider_badProvider() {
        TEST_2007_07_15.plus(new MockPeriodProviderReturnsNull());
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plus_PeriodProvider_invalidTooLarge() {
        PeriodProvider provider = Period.years(1);
        LocalDate.date(Year.MAX_YEAR, 1, 1).plus(provider);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plus_PeriodProvider_invalidTooSmall() {
        PeriodProvider provider = Period.years(-1);
        LocalDate.date(Year.MIN_YEAR, 1, 1).plus(provider);
    }

    //-----------------------------------------------------------------------
    // plusYears()
    //-----------------------------------------------------------------------
    public void test_plusYears_int_normal() {
        LocalDate t = TEST_2007_07_15.plusYears(1);
        assertEquals(t, LocalDate.date(2008, 7, 15));
    }

    public void test_plusYears_int_noChange() {
        LocalDate t = TEST_2007_07_15.plusYears(0);
        assertSame(t, TEST_2007_07_15);
    }

    public void test_plusYears_int_negative() {
        LocalDate t = TEST_2007_07_15.plusYears(-1);
        assertEquals(t, LocalDate.date(2006, 7, 15));
    }

    public void test_plusYears_int_adjustDay() {
        LocalDate t = LocalDate.date(2008, 2, 29).plusYears(1);
        LocalDate expected = LocalDate.date(2009, 2, 28);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plusYears_int_invalidTooLarge() {
        try {
            LocalDate.date(Year.MAX_YEAR, 1, 1).plusYears(1);
            fail();
        } catch (CalendricalException ex) {
            assertTrue(ex.getMessage().contains("exceeds the supported year range"));
            throw ex;
        }
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plusYears_int_invalidTooSmall_validInt() {
        try {
            LocalDate.date(Year.MIN_YEAR, 1, 1).plusYears(-1);
        } catch (CalendricalException ex) {
            assertTrue(ex.getMessage().contains("exceeds the supported year range"));
            throw ex;
        }
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plusYears_int_invalidTooSmall_invalidInt() {
        try {
            LocalDate.date(Year.MIN_YEAR, 1, 1).plusYears(-10);
        } catch (CalendricalException ex) {
            assertTrue(ex.getMessage().contains("exceeds the supported year range"));
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    public void test_plusYears_int_DateResolver_normal() {
        LocalDate t = TEST_2007_07_15.plusYears(1, DateResolvers.nextValid());
        assertEquals(t, LocalDate.date(2008, 7, 15));
    }

    public void test_plusYears_int_DateResolver_noChange() {
        LocalDate t = TEST_2007_07_15.plusYears(0, DateResolvers.nextValid());
        assertSame(t, TEST_2007_07_15);
    }

    public void test_plusYears_int_DateResolver_negative() {
        LocalDate t = TEST_2007_07_15.plusYears(-1, DateResolvers.nextValid());
        assertEquals(t, LocalDate.date(2006, 7, 15));
    }

    public void test_plusYears_int_DateResolver_adjustDay() {
        LocalDate t = LocalDate.date(2008, 2, 29).plusYears(1, DateResolvers.nextValid());
        LocalDate expected = LocalDate.date(2009, 3, 1);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_plusYears_int_DateResolver_null_adjustDay() {
        TEST_2007_07_15.plusYears(1, new MockDateResolverReturnsNull());
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plusYears_int_DateResolver_invalidTooLarge() {
        try {
            LocalDate.date(Year.MAX_YEAR, 1, 1).plusYears(1, DateResolvers.nextValid());
            fail();
        } catch (CalendricalException ex) {
            assertTrue(ex.getMessage().contains("exceeds the supported year range"));
            throw ex;
        }
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plusYears_int_DateResolver_invalidTooSmall_validInt() {
        try {
            LocalDate.date(Year.MIN_YEAR, 1, 1).plusYears(-1, DateResolvers.nextValid());
        } catch (CalendricalException ex) {
            assertTrue(ex.getMessage().contains("exceeds the supported year range"));
            throw ex;
        }
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plusYears_int_DateResolver_invalidTooSmall_invalidInt() {
        try {
            LocalDate.date(Year.MIN_YEAR, 1, 1).plusYears(-10, DateResolvers.nextValid());
        } catch (CalendricalException ex) {
            assertTrue(ex.getMessage().contains("exceeds the supported year range"));
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    // plusMonths()
    //-----------------------------------------------------------------------
    public void test_plusMonths_int_normal() {
        LocalDate t = TEST_2007_07_15.plusMonths(1);
        assertEquals(t, LocalDate.date(2007, 8, 15));
    }

    public void test_plusMonths_int_noChange() {
        LocalDate t = TEST_2007_07_15.plusMonths(0);
        assertSame(t, TEST_2007_07_15);
    }

    public void test_plusMonths_int_overYears() {
        LocalDate t = TEST_2007_07_15.plusMonths(25);
        assertEquals(t, LocalDate.date(2009, 8, 15));
    }

    public void test_plusMonths_int_negative() {
        LocalDate t = TEST_2007_07_15.plusMonths(-1);
        assertEquals(t, LocalDate.date(2007, 6, 15));
    }

    public void test_plusMonths_int_negativeAcrossYear() {
        LocalDate t = TEST_2007_07_15.plusMonths(-7);
        assertEquals(t, LocalDate.date(2006, 12, 15));
    }

    public void test_plusMonths_int_negativeOverYears() {
        LocalDate t = TEST_2007_07_15.plusMonths(-31);
        assertEquals(t, LocalDate.date(2004, 12, 15));
    }

    public void test_plusMonths_int_adjustDayFromLeapYear() {
        LocalDate t = LocalDate.date(2008, 2, 29).plusMonths(12);
        LocalDate expected = LocalDate.date(2009, 2, 28);
        assertEquals(t, expected);
    }

    public void test_plusMonths_int_adjustDayFromMonthLength() {
        LocalDate t = LocalDate.date(2007, 3, 31).plusMonths(1);
        LocalDate expected = LocalDate.date(2007, 4, 30);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_plusMonths_int_invalidTooLarge() {
        LocalDate.date(Year.MAX_YEAR, 12, 1).plusMonths(1);
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_plusMonths_int_invalidTooSmall() {
        LocalDate.date(Year.MIN_YEAR, 1, 1).plusMonths(-1);
    }

    public void test_plusMonths_int_DateResolver_normal() {
        LocalDate t = TEST_2007_07_15.plusMonths(1, DateResolvers.nextValid());
        assertEquals(t, LocalDate.date(2007, 8, 15));
    }

    public void test_plusMonths_int_DateResolver_noChange() {
        LocalDate t = TEST_2007_07_15.plusMonths(0, DateResolvers.nextValid());
        assertSame(t, TEST_2007_07_15);
    }

    public void test_plusMonths_int_DateResolver_overYears() {
        LocalDate t = TEST_2007_07_15.plusMonths(25, DateResolvers.nextValid());
        assertEquals(t, LocalDate.date(2009, 8, 15));
    }

    public void test_plusMonths_int_DateResolver_negative() {
        LocalDate t = TEST_2007_07_15.plusMonths(-1, DateResolvers.nextValid());
        assertEquals(t, LocalDate.date(2007, 6, 15));
    }

    public void test_plusMonths_int_DateResolver_negativeAcrossYear() {
        LocalDate t = TEST_2007_07_15.plusMonths(-7, DateResolvers.nextValid());
        assertEquals(t, LocalDate.date(2006, 12, 15));
    }

    public void test_plusMonths_int_DateResolver_negativeOverYears() {
        LocalDate t = TEST_2007_07_15.plusMonths(-31, DateResolvers.nextValid());
        assertEquals(t, LocalDate.date(2004, 12, 15));
    }

    public void test_plusMonths_int_DateResolver_adjustDayFromLeapYear() {
        LocalDate t = LocalDate.date(2008, 2, 29).plusMonths(12, DateResolvers.nextValid());
        LocalDate expected = LocalDate.date(2009, 3, 1);
        assertEquals(t, expected);
    }

    public void test_plusMonths_int_DateResolver_adjustDayFromMonthLength() {
        LocalDate t = LocalDate.date(2007, 3, 31).plusMonths(1, DateResolvers.nextValid());
        LocalDate expected = LocalDate.date(2007, 5, 1);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_plusMonths_int_DateResolver_null_adjustDay() {
        TEST_2007_07_15.plusMonths(1, new MockDateResolverReturnsNull());
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_plusMonths_int_DateResolver_invalidTooLarge() {
        LocalDate.date(Year.MAX_YEAR, 12, 1).plusMonths(1, DateResolvers.nextValid());
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_plusMonths_int_DateResolver_invalidTooSmall() {
        LocalDate.date(Year.MIN_YEAR, 1, 1).plusMonths(-1, DateResolvers.nextValid());
    }

    //-----------------------------------------------------------------------
    // plusWeeks()
    //-----------------------------------------------------------------------
    @DataProvider(name="samplePlusWeeksSymmetry")
    Object[][] provider_samplePlusWeeksSymmetry() {
        return new Object[][] {
            {LocalDate.date(-1, 1, 1)},
            {LocalDate.date(-1, 2, 28)},
            {LocalDate.date(-1, 3, 1)},
            {LocalDate.date(-1, 12, 31)},
            {LocalDate.date(0, 1, 1)},
            {LocalDate.date(0, 2, 28)},
            {LocalDate.date(0, 2, 29)},
            {LocalDate.date(0, 3, 1)},
            {LocalDate.date(0, 12, 31)},
            {LocalDate.date(2007, 1, 1)},
            {LocalDate.date(2007, 2, 28)},
            {LocalDate.date(2007, 3, 1)},
            {LocalDate.date(2007, 12, 31)},
            {LocalDate.date(2008, 1, 1)},
            {LocalDate.date(2008, 2, 28)},
            {LocalDate.date(2008, 2, 29)},
            {LocalDate.date(2008, 3, 1)},
            {LocalDate.date(2008, 12, 31)},
            {LocalDate.date(2099, 1, 1)},
            {LocalDate.date(2099, 2, 28)},
            {LocalDate.date(2099, 3, 1)},
            {LocalDate.date(2099, 12, 31)},
            {LocalDate.date(2100, 1, 1)},
            {LocalDate.date(2100, 2, 28)},
            {LocalDate.date(2100, 3, 1)},
            {LocalDate.date(2100, 12, 31)},
        };
    }
    
    @Test(dataProvider="samplePlusWeeksSymmetry")
    public void test_plusWeeks_symmetry(LocalDate reference) {
        for (int weeks = 0; weeks < 365 * 8; weeks++) {
            LocalDate t = reference.plusWeeks(weeks).plusWeeks(-weeks);
            assertEquals(t, reference);

            t = reference.plusWeeks(-weeks).plusWeeks(weeks);
            assertEquals(t, reference);
        }
    }

    public void test_plusWeeks_normal() {
        LocalDate t = TEST_2007_07_15.plusWeeks(1);
        assertEquals(t, LocalDate.date(2007, 7, 22));
    }

    public void test_plusWeeks_noChange() {
        LocalDate t = TEST_2007_07_15.plusWeeks(0);
        assertSame(t, TEST_2007_07_15);
    }

    public void test_plusWeeks_overMonths() {
        LocalDate t = TEST_2007_07_15.plusWeeks(9);
        assertEquals(t, LocalDate.date(2007, 9, 16));
    }

    public void test_plusWeeks_overYears() {
        LocalDate t = LocalDate.date(2006, 7, 16).plusWeeks(52);
        assertEquals(t, TEST_2007_07_15);
    }

    public void test_plusWeeks_overLeapYears() {
        LocalDate t = TEST_2007_07_15.plusYears(-1).plusWeeks(104);
        assertEquals(t, LocalDate.date(2008, 7, 12));
    }

    public void test_plusWeeks_negative() {
        LocalDate t = TEST_2007_07_15.plusWeeks(-1);
        assertEquals(t, LocalDate.date(2007, 7, 8));
    }

    public void test_plusWeeks_negativeAcrossYear() {
        LocalDate t = TEST_2007_07_15.plusWeeks(-28);
        assertEquals(t, LocalDate.date(2006, 12, 31));
    }

    public void test_plusWeeks_negativeOverYears() {
        LocalDate t = TEST_2007_07_15.plusWeeks(-104);
        assertEquals(t, LocalDate.date(2005, 7, 17));
    }

    public void test_plusWeeks_maximum() {
        LocalDate t = LocalDate.date(Year.MAX_YEAR, 12, 24).plusWeeks(1);
        LocalDate expected = LocalDate.date(Year.MAX_YEAR, 12, 31);
        assertEquals(t, expected);
    }

    public void test_plusWeeks_minimum() {
        LocalDate t = LocalDate.date(Year.MIN_YEAR, 1, 8).plusWeeks(-1);
        LocalDate expected = LocalDate.date(Year.MIN_YEAR, 1, 1);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_plusWeeks_invalidTooLarge() {
        LocalDate.date(Year.MAX_YEAR, 12, 25).plusWeeks(1);
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_plusWeeks_invalidTooSmall() {
        LocalDate.date(Year.MIN_YEAR, 1, 7).plusWeeks(-1);
    }

    //-----------------------------------------------------------------------
    // plusDays()
    //-----------------------------------------------------------------------
    @DataProvider(name="samplePlusDaysSymmetry")
    Object[][] provider_samplePlusDaysSymmetry() {
        return new Object[][] {
            {LocalDate.date(-1, 1, 1)},
            {LocalDate.date(-1, 2, 28)},
            {LocalDate.date(-1, 3, 1)},
            {LocalDate.date(-1, 12, 31)},
            {LocalDate.date(0, 1, 1)},
            {LocalDate.date(0, 2, 28)},
            {LocalDate.date(0, 2, 29)},
            {LocalDate.date(0, 3, 1)},
            {LocalDate.date(0, 12, 31)},
            {LocalDate.date(2007, 1, 1)},
            {LocalDate.date(2007, 2, 28)},
            {LocalDate.date(2007, 3, 1)},
            {LocalDate.date(2007, 12, 31)},
            {LocalDate.date(2008, 1, 1)},
            {LocalDate.date(2008, 2, 28)},
            {LocalDate.date(2008, 2, 29)},
            {LocalDate.date(2008, 3, 1)},
            {LocalDate.date(2008, 12, 31)},
            {LocalDate.date(2099, 1, 1)},
            {LocalDate.date(2099, 2, 28)},
            {LocalDate.date(2099, 3, 1)},
            {LocalDate.date(2099, 12, 31)},
            {LocalDate.date(2100, 1, 1)},
            {LocalDate.date(2100, 2, 28)},
            {LocalDate.date(2100, 3, 1)},
            {LocalDate.date(2100, 12, 31)},
        };
    }
    
    @Test(dataProvider="samplePlusDaysSymmetry")
    public void test_plusDays_symmetry(LocalDate reference) {
        for (int days = 0; days < 365 * 8; days++) {
            LocalDate t = reference.plusDays(days).plusDays(-days);
            assertEquals(t, reference);

            t = reference.plusDays(-days).plusDays(days);
            assertEquals(t, reference);
        }
    }

    public void test_plusDays_normal() {
        LocalDate t = TEST_2007_07_15.plusDays(1);
        assertEquals(t, LocalDate.date(2007, 7, 16));
    }

    public void test_plusDays_noChange() {
        LocalDate t = TEST_2007_07_15.plusDays(0);
        assertSame(t, TEST_2007_07_15);
    }

    public void test_plusDays_overMonths() {
        LocalDate t = TEST_2007_07_15.plusDays(62);
        assertEquals(t, LocalDate.date(2007, 9, 15));
    }

    public void test_plusDays_overYears() {
        LocalDate t = LocalDate.date(2006, 7, 14).plusDays(366);
        assertEquals(t, TEST_2007_07_15);
    }

    public void test_plusDays_overLeapYears() {
        LocalDate t = TEST_2007_07_15.plusYears(-1).plusDays(365 + 366);
        assertEquals(t, LocalDate.date(2008, 7, 15));
    }

    public void test_plusDays_negative() {
        LocalDate t = TEST_2007_07_15.plusDays(-1);
        assertEquals(t, LocalDate.date(2007, 7, 14));
    }

    public void test_plusDays_negativeAcrossYear() {
        LocalDate t = TEST_2007_07_15.plusDays(-196);
        assertEquals(t, LocalDate.date(2006, 12, 31));
    }

    public void test_plusDays_negativeOverYears() {
        LocalDate t = TEST_2007_07_15.plusDays(-730);
        assertEquals(t, LocalDate.date(2005, 7, 15));
    }

    public void test_plusDays_maximum() {
        LocalDate t = LocalDate.date(Year.MAX_YEAR, 12, 30).plusDays(1);
        LocalDate expected = LocalDate.date(Year.MAX_YEAR, 12, 31);
        assertEquals(t, expected);
    }

    public void test_plusDays_minimum() {
        LocalDate t = LocalDate.date(Year.MIN_YEAR, 1, 2).plusDays(-1);
        LocalDate expected = LocalDate.date(Year.MIN_YEAR, 1, 1);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_plusDays_invalidTooLarge() {
        LocalDate.date(Year.MAX_YEAR, 12, 31).plusDays(1);
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_plusDays_invalidTooSmall() {
        LocalDate.date(Year.MIN_YEAR, 1, 1).plusDays(-1);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plusDays_overflowTooLarge() {
        LocalDate.date(Year.MAX_YEAR, 12, 31).plusDays(Long.MAX_VALUE);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_plusDays_overflowTooSmall() {
        LocalDate.date(Year.MIN_YEAR, 1, 1).plusDays(Long.MIN_VALUE);
    }

    //-----------------------------------------------------------------------
    // minus(PeriodProvider)
    //-----------------------------------------------------------------------
    public void test_minus_PeriodProvider() {
        PeriodProvider provider = Period.period(1, 2, 3, 4, 5, 6, 7);
        LocalDate t = TEST_2007_07_15.minus(provider);
        assertEquals(t, LocalDate.date(2006, 5, 12));
    }

    public void test_minus_PeriodProvider_timeIgnored() {
        PeriodProvider provider = Period.period(1, 2, 3, Integer.MAX_VALUE, 5, 6, 7);
        LocalDate t = TEST_2007_07_15.minus(provider);
        assertEquals(t, LocalDate.date(2006, 5, 12));
    }

    public void test_minus_PeriodProvider_zero() {
        LocalDate t = TEST_2007_07_15.minus(Period.ZERO);
        assertSame(t, TEST_2007_07_15);
    }

    public void test_minus_PeriodProvider_previousValidResolver_oneMonth() {
        PeriodProvider provider = Period.months(1);
        LocalDate t = LocalDate.date(2008, 3, 31).minus(provider);
        assertEquals(t, LocalDate.date(2008, 2, 29));
    }

//    public void test_minus_PeriodProvider_previousValidResolver_oneMonthOneDay() {
//        PeriodProvider provider = Period.yearsMonthsDays(0, 1, 1);
//        LocalDate t = LocalDate.date(2008, 3, 31).minus(provider);
//        assertEquals(t, LocalDate.date(2008, 2, 29));  // TODO: what is the correct result here
//    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_minus_PeriodProvider_null() {
        TEST_2007_07_15.minus((PeriodProvider) null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_minus_PeriodProvider_badProvider() {
        TEST_2007_07_15.minus(new MockPeriodProviderReturnsNull());
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minus_PeriodProvider_invalidTooLarge() {
        PeriodProvider provider = Period.years(-1);
        LocalDate.date(Year.MAX_YEAR, 1, 1).minus(provider);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minus_PeriodProvider_invalidTooSmall() {
        PeriodProvider provider = Period.years(1);
        LocalDate.date(Year.MIN_YEAR, 1, 1).minus(provider);
    }

    //-----------------------------------------------------------------------
    // minusYears()
    //-----------------------------------------------------------------------
    public void test_minusYears_int_normal() {
        LocalDate t = TEST_2007_07_15.minusYears(1);
        assertEquals(t, LocalDate.date(2006, 7, 15));
    }

    public void test_minusYears_int_noChange() {
        LocalDate t = TEST_2007_07_15.minusYears(0);
        assertSame(t, TEST_2007_07_15);
    }

    public void test_minusYears_int_negative() {
        LocalDate t = TEST_2007_07_15.minusYears(-1);
        assertEquals(t, LocalDate.date(2008, 7, 15));
    }

    public void test_minusYears_int_adjustDay() {
        LocalDate t = LocalDate.date(2008, 2, 29).minusYears(1);
        LocalDate expected = LocalDate.date(2007, 2, 28);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minusYears_int_invalidTooLarge() {
        try {
            LocalDate.date(Year.MAX_YEAR, 1, 1).minusYears(-1);
            fail();
        } catch (CalendricalException ex) {
            assertTrue(ex.getMessage().contains("exceeds the supported year range"));
            throw ex;
        }
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minusYears_int_invalidTooSmall_validInt() {
        try {
            LocalDate.date(Year.MIN_YEAR, 1, 1).minusYears(1);
        } catch (CalendricalException ex) {
            assertTrue(ex.getMessage().contains("exceeds the supported year range"));
            throw ex;
        }
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minusYears_int_invalidTooSmall_invalidInt() {
        try {
            LocalDate.date(Year.MIN_YEAR, 1, 1).minusYears(10);
        } catch (CalendricalException ex) {
            assertTrue(ex.getMessage().contains("exceeds the supported year range"));
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    public void test_minusYears_int_DateResolver_normal() {
        LocalDate t = TEST_2007_07_15.minusYears(1, DateResolvers.nextValid());
        assertEquals(t, LocalDate.date(2006, 7, 15));
    }

    public void test_minusYears_int_DateResolver_noChange() {
        LocalDate t = TEST_2007_07_15.minusYears(0, DateResolvers.nextValid());
        assertSame(t, TEST_2007_07_15);
    }

    public void test_minusYears_int_DateResolver_negative() {
        LocalDate t = TEST_2007_07_15.minusYears(-1, DateResolvers.nextValid());
        assertEquals(t, LocalDate.date(2008, 7, 15));
    }

    public void test_minusYears_int_DateResolver_adjustDay() {
        LocalDate t = LocalDate.date(2008, 2, 29).minusYears(1, DateResolvers.nextValid());
        LocalDate expected = LocalDate.date(2007, 3, 1);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_minusYears_int_DateResolver_null_adjustDay() {
        TEST_2007_07_15.minusYears(1, new MockDateResolverReturnsNull());
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minusYears_int_DateResolver_invalidTooLarge() {
        try {
            LocalDate.date(Year.MAX_YEAR, 1, 1).minusYears(-1, DateResolvers.nextValid());
            fail();
        } catch (CalendricalException ex) {
            assertTrue(ex.getMessage().contains("exceeds the supported year range"));
            throw ex;
        }
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minusYears_int_DateResolver_invalidTooSmall_validInt() {
        try {
            LocalDate.date(Year.MIN_YEAR, 1, 1).minusYears(1, DateResolvers.nextValid());
        } catch (CalendricalException ex) {
            assertTrue(ex.getMessage().contains("exceeds the supported year range"));
            throw ex;
        }
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minusYears_int_DateResolver_invalidTooSmall_invalidInt() {
        try {
            LocalDate.date(Year.MIN_YEAR, 1, 1).minusYears(10, DateResolvers.nextValid());
        } catch (CalendricalException ex) {
            assertTrue(ex.getMessage().contains("exceeds the supported year range"));
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    // minusMonths()
    //-----------------------------------------------------------------------
    public void test_minusMonths_int_normal() {
        LocalDate t = TEST_2007_07_15.minusMonths(1);
        assertEquals(t, LocalDate.date(2007, 6, 15));
    }

    public void test_minusMonths_int_noChange() {
        LocalDate t = TEST_2007_07_15.minusMonths(0);
        assertSame(t, TEST_2007_07_15);
    }

    public void test_minusMonths_int_overYears() {
        LocalDate t = TEST_2007_07_15.minusMonths(25);
        assertEquals(t, LocalDate.date(2005, 6, 15));
    }

    public void test_minusMonths_int_negative() {
        LocalDate t = TEST_2007_07_15.minusMonths(-1);
        assertEquals(t, LocalDate.date(2007, 8, 15));
    }

    public void test_minusMonths_int_negativeAcrossYear() {
        LocalDate t = TEST_2007_07_15.minusMonths(-7);
        assertEquals(t, LocalDate.date(2008, 2, 15));
    }

    public void test_minusMonths_int_negativeOverYears() {
        LocalDate t = TEST_2007_07_15.minusMonths(-31);
        assertEquals(t, LocalDate.date(2010, 2, 15));
    }

    public void test_minusMonths_int_adjustDayFromLeapYear() {
        LocalDate t = LocalDate.date(2008, 2, 29).minusMonths(12);
        LocalDate expected = LocalDate.date(2007, 2, 28);
        assertEquals(t, expected);
    }

    public void test_minusMonths_int_adjustDayFromMonthLength() {
        LocalDate t = LocalDate.date(2007, 3, 31).minusMonths(1);
        LocalDate expected = LocalDate.date(2007, 2, 28);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_minusMonths_int_invalidTooLarge() {
        LocalDate.date(Year.MAX_YEAR, 12, 1).minusMonths(-1);
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_minusMonths_int_invalidTooSmall() {
        LocalDate t = LocalDate.date(Year.MIN_YEAR, 1, 1).minusMonths(1);
    }

    public void test_minusMonths_int_DateResolver_normal() {
        LocalDate t = TEST_2007_07_15.minusMonths(1, DateResolvers.nextValid());
        assertEquals(t, LocalDate.date(2007, 6, 15));
    }

    public void test_minusMonths_int_DateResolver_noChange() {
        LocalDate t = TEST_2007_07_15.minusMonths(0, DateResolvers.nextValid());
        assertSame(t, TEST_2007_07_15);
    }

    public void test_minusMonths_int_DateResolver_overYears() {
        LocalDate t = TEST_2007_07_15.minusMonths(25, DateResolvers.nextValid());
        assertEquals(t, LocalDate.date(2005, 6, 15));
    }

    public void test_minusMonths_int_DateResolver_negative() {
        LocalDate t = TEST_2007_07_15.minusMonths(-1, DateResolvers.nextValid());
        assertEquals(t, LocalDate.date(2007, 8, 15));
    }

    public void test_minusMonths_int_DateResolver_negativeAcrossYear() {
        LocalDate t = TEST_2007_07_15.minusMonths(-7, DateResolvers.nextValid());
        assertEquals(t, LocalDate.date(2008, 2, 15));
    }

    public void test_minusMonths_int_DateResolver_negativeOverYears() {
        LocalDate t = TEST_2007_07_15.minusMonths(-31, DateResolvers.nextValid());
        assertEquals(t, LocalDate.date(2010, 2, 15));
    }

    public void test_minusMonths_int_DateResolver_adjustDayFromLeapYear() {
        LocalDate t = LocalDate.date(2008, 2, 29).minusMonths(12, DateResolvers.nextValid());
        LocalDate expected = LocalDate.date(2007, 3, 1);
        assertEquals(t, expected);
    }

    public void test_minusMonths_int_DateResolver_adjustDayFromMonthLength() {
        LocalDate t = LocalDate.date(2007, 3, 31).minusMonths(1, DateResolvers.nextValid());
        LocalDate expected = LocalDate.date(2007, 3, 1);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_minusMonths_int_DateResolver_null_adjustDay() {
        TEST_2007_07_15.minusMonths(1, new MockDateResolverReturnsNull());
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_minusMonths_int_DateResolver_invalidTooLarge() {
        LocalDate.date(Year.MAX_YEAR, 12, 1).minusMonths(-1, DateResolvers.nextValid());
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_minusMonths_int_DateResolver_invalidTooSmall() {
        LocalDate.date(Year.MIN_YEAR, 1, 1).minusMonths(1, DateResolvers.nextValid());
    }

    //-----------------------------------------------------------------------
    // minusWeeks()
    //-----------------------------------------------------------------------
    @DataProvider(name="sampleMinusWeeksSymmetry")
    Object[][] provider_sampleMinusWeeksSymmetry() {
        return new Object[][] {
            {LocalDate.date(-1, 1, 1)},
            {LocalDate.date(-1, 2, 28)},
            {LocalDate.date(-1, 3, 1)},
            {LocalDate.date(-1, 12, 31)},
            {LocalDate.date(0, 1, 1)},
            {LocalDate.date(0, 2, 28)},
            {LocalDate.date(0, 2, 29)},
            {LocalDate.date(0, 3, 1)},
            {LocalDate.date(0, 12, 31)},
            {LocalDate.date(2007, 1, 1)},
            {LocalDate.date(2007, 2, 28)},
            {LocalDate.date(2007, 3, 1)},
            {LocalDate.date(2007, 12, 31)},
            {LocalDate.date(2008, 1, 1)},
            {LocalDate.date(2008, 2, 28)},
            {LocalDate.date(2008, 2, 29)},
            {LocalDate.date(2008, 3, 1)},
            {LocalDate.date(2008, 12, 31)},
            {LocalDate.date(2099, 1, 1)},
            {LocalDate.date(2099, 2, 28)},
            {LocalDate.date(2099, 3, 1)},
            {LocalDate.date(2099, 12, 31)},
            {LocalDate.date(2100, 1, 1)},
            {LocalDate.date(2100, 2, 28)},
            {LocalDate.date(2100, 3, 1)},
            {LocalDate.date(2100, 12, 31)},
        };
    }
    
    @Test(dataProvider="sampleMinusWeeksSymmetry")
    public void test_minusWeeks_symmetry(LocalDate reference) {
        for (int weeks = 0; weeks < 365 * 8; weeks++) {
            LocalDate t = reference.minusWeeks(weeks).minusWeeks(-weeks);
            assertEquals(t, reference);

            t = reference.minusWeeks(-weeks).minusWeeks(weeks);
            assertEquals(t, reference);
        }
    }

    public void test_minusWeeks_normal() {
        LocalDate t = TEST_2007_07_15.minusWeeks(1);
        assertEquals(t, LocalDate.date(2007, 7, 8));
    }

    public void test_minusWeeks_noChange() {
        LocalDate t = TEST_2007_07_15.minusWeeks(0);
        assertSame(t, TEST_2007_07_15);
    }

    public void test_minusWeeks_overMonths() {
        LocalDate t = TEST_2007_07_15.minusWeeks(9);
        assertEquals(t, LocalDate.date(2007, 5, 13));
    }

    public void test_minusWeeks_overYears() {
        LocalDate t = LocalDate.date(2008, 7, 13).minusWeeks(52);
        assertEquals(t, TEST_2007_07_15);
    }

    public void test_minusWeeks_overLeapYears() {
        LocalDate t = TEST_2007_07_15.minusYears(-1).minusWeeks(104);
        assertEquals(t, LocalDate.date(2006, 7, 18));
    }

    public void test_minusWeeks_negative() {
        LocalDate t = TEST_2007_07_15.minusWeeks(-1);
        assertEquals(t, LocalDate.date(2007, 7, 22));
    }

    public void test_minusWeeks_negativeAcrossYear() {
        LocalDate t = TEST_2007_07_15.minusWeeks(-28);
        assertEquals(t, LocalDate.date(2008, 1, 27));
    }

    public void test_minusWeeks_negativeOverYears() {
        LocalDate t = TEST_2007_07_15.minusWeeks(-104);
        assertEquals(t, LocalDate.date(2009, 7, 12));
    }

    public void test_minusWeeks_maximum() {
        LocalDate t = LocalDate.date(Year.MAX_YEAR, 12, 24).minusWeeks(-1);
        LocalDate expected = LocalDate.date(Year.MAX_YEAR, 12, 31);
        assertEquals(t, expected);
    }

    public void test_minusWeeks_minimum() {
        LocalDate t = LocalDate.date(Year.MIN_YEAR, 1, 8).minusWeeks(1);
        LocalDate expected = LocalDate.date(Year.MIN_YEAR, 1, 1);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_minusWeeks_invalidTooLarge() {
        LocalDate.date(Year.MAX_YEAR, 12, 25).minusWeeks(-1);
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_minusWeeks_invalidTooSmall() {
        LocalDate.date(Year.MIN_YEAR, 1, 7).minusWeeks(1);
    }

    //-----------------------------------------------------------------------
    // minusDays()
    //-----------------------------------------------------------------------
    @DataProvider(name="sampleMinusDaysSymmetry")
    Object[][] provider_sampleMinusDaysSymmetry() {
        return new Object[][] {
            {LocalDate.date(-1, 1, 1)},
            {LocalDate.date(-1, 2, 28)},
            {LocalDate.date(-1, 3, 1)},
            {LocalDate.date(-1, 12, 31)},
            {LocalDate.date(0, 1, 1)},
            {LocalDate.date(0, 2, 28)},
            {LocalDate.date(0, 2, 29)},
            {LocalDate.date(0, 3, 1)},
            {LocalDate.date(0, 12, 31)},
            {LocalDate.date(2007, 1, 1)},
            {LocalDate.date(2007, 2, 28)},
            {LocalDate.date(2007, 3, 1)},
            {LocalDate.date(2007, 12, 31)},
            {LocalDate.date(2008, 1, 1)},
            {LocalDate.date(2008, 2, 28)},
            {LocalDate.date(2008, 2, 29)},
            {LocalDate.date(2008, 3, 1)},
            {LocalDate.date(2008, 12, 31)},
            {LocalDate.date(2099, 1, 1)},
            {LocalDate.date(2099, 2, 28)},
            {LocalDate.date(2099, 3, 1)},
            {LocalDate.date(2099, 12, 31)},
            {LocalDate.date(2100, 1, 1)},
            {LocalDate.date(2100, 2, 28)},
            {LocalDate.date(2100, 3, 1)},
            {LocalDate.date(2100, 12, 31)},
        };
    }
    
    @Test(dataProvider="sampleMinusDaysSymmetry")
    public void test_minusDays_symmetry(LocalDate reference) {
        for (int days = 0; days < 365 * 8; days++) {
            LocalDate t = reference.minusDays(days).minusDays(-days);
            assertEquals(t, reference);

            t = reference.minusDays(-days).minusDays(days);
            assertEquals(t, reference);
        }
    }

    public void test_minusDays_normal() {
        LocalDate t = TEST_2007_07_15.minusDays(1);
        assertEquals(t, LocalDate.date(2007, 7, 14));
    }

    public void test_minusDays_noChange() {
        LocalDate t = TEST_2007_07_15.minusDays(0);
        assertSame(t, TEST_2007_07_15);
    }

    public void test_minusDays_overMonths() {
        LocalDate t = TEST_2007_07_15.minusDays(62);
        assertEquals(t, LocalDate.date(2007, 5, 14));
    }

    public void test_minusDays_overYears() {
        LocalDate t = LocalDate.date(2008, 7, 16).minusDays(367);
        assertEquals(t, TEST_2007_07_15);
    }

    public void test_minusDays_overLeapYears() {
        LocalDate t = TEST_2007_07_15.plusYears(2).minusDays(365 + 366);
        assertEquals(t, TEST_2007_07_15);
    }

    public void test_minusDays_negative() {
        LocalDate t = TEST_2007_07_15.minusDays(-1);
        assertEquals(t, LocalDate.date(2007, 7, 16));
    }

    public void test_minusDays_negativeAcrossYear() {
        LocalDate t = TEST_2007_07_15.minusDays(-169);
        assertEquals(t, LocalDate.date(2007, 12, 31));
    }

    public void test_minusDays_negativeOverYears() {
        LocalDate t = TEST_2007_07_15.minusDays(-731);
        assertEquals(t, LocalDate.date(2009, 7, 15));
    }

    public void test_minusDays_maximum() {
        LocalDate t = LocalDate.date(Year.MAX_YEAR, 12, 30).minusDays(-1);
        LocalDate expected = LocalDate.date(Year.MAX_YEAR, 12, 31);
        assertEquals(t, expected);
    }

    public void test_minusDays_minimum() {
        LocalDate t = LocalDate.date(Year.MIN_YEAR, 1, 2).minusDays(1);
        LocalDate expected = LocalDate.date(Year.MIN_YEAR, 1, 1);
        assertEquals(t, expected);
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_minusDays_invalidTooLarge() {
        LocalDate.date(Year.MAX_YEAR, 12, 31).minusDays(-1);
    }

    @Test(expectedExceptions={CalendricalException.class})
    public void test_minusDays_invalidTooSmall() {
        LocalDate.date(Year.MIN_YEAR, 1, 1).minusDays(1);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minusDays_overflowTooLarge() {
        LocalDate.date(Year.MAX_YEAR, 12, 31).minusDays(Long.MIN_VALUE);
    }

    @Test(expectedExceptions=CalendricalException.class)
    public void test_minusDays_overflowTooSmall() {
        LocalDate.date(Year.MIN_YEAR, 1, 1).minusDays(Long.MAX_VALUE);
    }

    //-----------------------------------------------------------------------
    // matches()
    //-----------------------------------------------------------------------
    public void test_matches() {
        assertTrue(TEST_2007_07_15.matches(Year.isoYear(2007)));
        assertFalse(TEST_2007_07_15.matches(Year.isoYear(2006)));
        assertTrue(TEST_2007_07_15.matches(QuarterOfYear.Q3));
        assertFalse(TEST_2007_07_15.matches(QuarterOfYear.Q2));
        assertTrue(TEST_2007_07_15.matches(MonthOfYear.JULY));
        assertFalse(TEST_2007_07_15.matches(MonthOfYear.JUNE));
        assertTrue(TEST_2007_07_15.matches(DayOfMonth.dayOfMonth(15)));
        assertFalse(TEST_2007_07_15.matches(DayOfMonth.dayOfMonth(14)));
        assertTrue(TEST_2007_07_15.matches(DayOfWeek.SUNDAY));
        assertFalse(TEST_2007_07_15.matches(DayOfWeek.MONDAY));
    }

    //-----------------------------------------------------------------------
    // atTime()
    //-----------------------------------------------------------------------
    public void test_atTime() {
        LocalDate t = LocalDate.date(2008, 6, 30);
        assertEquals(t.atTime(LocalTime.time(11, 30)), LocalDateTime.dateTime(2008, 6, 30, 11, 30));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_atTime_nullLocalTime() {
        LocalDate t = LocalDate.date(2008, 6, 30);
        t.atTime((LocalTime) null);
    }

    //-----------------------------------------------------------------------
    // atMidnight()
    //-----------------------------------------------------------------------
    public void test_atMidnight() {
        LocalDate t = LocalDate.date(2008, 6, 30);
        assertEquals(t.atMidnight(), LocalDateTime.dateTime(2008, 6, 30, 0, 0));
    }

    //-----------------------------------------------------------------------
    // atOffset()
    //-----------------------------------------------------------------------
    public void test_atOffset() {
        LocalDate t = LocalDate.date(2008, 6, 30);
        assertEquals(t.atOffset(OFFSET_PTWO), OffsetDate.date(2008, 6, 30, OFFSET_PTWO));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_atOffset_nullZoneOffset() {
        LocalDate t = LocalDate.date(2008, 6, 30);
        t.atOffset((ZoneOffset) null);
    }

    //-----------------------------------------------------------------------
    // atStartOfDayInZone()
    //-----------------------------------------------------------------------
    public void test_atStartOfDayInZone() {
        LocalDate t = LocalDate.date(2008, 6, 30);
        assertEquals(t.atStartOfDayInZone(ZONE_PARIS),
                ZonedDateTime.dateTime(LocalDateTime.dateTime(2008, 6, 30, 0, 0), ZONE_PARIS));
    }

    public void test_atStartOfDayInZone_dstGap() {
        LocalDate t = LocalDate.date(2007, 4, 1);
        assertEquals(t.atStartOfDayInZone(ZONE_GAZA),
                ZonedDateTime.dateTime(LocalDateTime.dateTime(2007, 4, 1, 1, 0), ZONE_GAZA));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_atStartOfDayInZone_nullTimeZone() {
        LocalDate t = LocalDate.date(2008, 6, 30);
        t.atStartOfDayInZone((TimeZone) null);
    }

    //-----------------------------------------------------------------------
    // toLocalDate()
    //-----------------------------------------------------------------------
    @Test(dataProvider="sampleDates")
    public void test_toLocalDate(int year, int month, int day) {
        LocalDate t = LocalDate.date(year, month, day);
        assertSame(t.toLocalDate(), t);
    }

    //-----------------------------------------------------------------------
    // toCalendrical()
    //-----------------------------------------------------------------------
    @Test(dataProvider="sampleDates")
    public void test_toCalendrical(int year, int month, int day) {
        LocalDate t = LocalDate.date(year, month, day);
        assertEquals(t.toCalendrical(), new Calendrical(t, null, null, null));
    }

    //-----------------------------------------------------------------------
    // toEpochDays()
    //-----------------------------------------------------------------------
    public void test_toEpochDays() {
        long date_0000_01_01 = -678941 - 40587;
        
        LocalDate test = LocalDate.date(0, 1, 1);
        for (long i = date_0000_01_01; i < 700000; i++) {
            assertEquals(test.toEpochDays(), i);
            test = next(test);
        }
        test = LocalDate.date(0, 1, 1);
        for (long i = date_0000_01_01; i > -2000000; i--) {
            assertEquals(test.toEpochDays(), i);
            test = previous(test);
        }
        
        assertEquals(LocalDate.date(1858, 11, 17).toEpochDays(), -40587);
        assertEquals(LocalDate.date(1, 1, 1).toEpochDays(), -678575 - 40587);
        assertEquals(LocalDate.date(1995, 9, 27).toEpochDays(), 49987 - 40587);
        assertEquals(LocalDate.date(1970, 1, 1).toEpochDays(), 0);
        assertEquals(LocalDate.date(-1, 12, 31).toEpochDays(), -678942 - 40587);
    }

    public void test_toEpochDays_fromMJDays_symmetry() {
        long date_0000_01_01 = -678941 - 40587;
        
        LocalDate test = LocalDate.date(0, 1, 1);
        for (long i = date_0000_01_01; i < 700000; i++) {
            assertEquals(LocalDate.fromEpochDays(test.toEpochDays()), test);
            test = next(test);
        }
        test = LocalDate.date(0, 1, 1);
        for (long i = date_0000_01_01; i > -2000000; i--) {
            assertEquals(LocalDate.fromEpochDays(test.toEpochDays()), test);
            test = previous(test);
        }
    }

    //-----------------------------------------------------------------------
    // toModifiedJulianDays()
    //-----------------------------------------------------------------------
    public void test_toModifiedJulianDays() {
        LocalDate test = LocalDate.date(0, 1, 1);
        for (int i = -678941; i < 700000; i++) {
            assertEquals(test.toModifiedJulianDays(), i);
            test = next(test);
        }
        
        test = LocalDate.date(0, 1, 1);
        for (int i = -678941; i > -2000000; i--) {
            assertEquals(test.toModifiedJulianDays(), i);
            test = previous(test);
        }
        
        assertEquals(LocalDate.date(1858, 11, 17).toModifiedJulianDays(), 0);
        assertEquals(LocalDate.date(1, 1, 1).toModifiedJulianDays(), -678575);
        assertEquals(LocalDate.date(1995, 9, 27).toModifiedJulianDays(), 49987);
        assertEquals(LocalDate.date(1970, 1, 1).toModifiedJulianDays(), 40587);
        assertEquals(LocalDate.date(-1, 12, 31).toModifiedJulianDays(), -678942);
    }

    public void test_toModifiedJulianDays_fromMJDays_symmetry() {
        LocalDate test = LocalDate.date(0, 1, 1);
        for (int i = -678941; i < 700000; i++) {
            assertEquals(LocalDate.fromModifiedJulianDays(test.toModifiedJulianDays()), test);
            test = next(test);
        }

        test = LocalDate.date(0, 1, 1);
        for (int i = -678941; i > -2000000; i--) {
            assertEquals(LocalDate.fromModifiedJulianDays(test.toModifiedJulianDays()), test);
            test = previous(test);
        }
    }

    //-----------------------------------------------------------------------
    // compareTo()
    //-----------------------------------------------------------------------
    public void test_comparisons() {
        doTest_comparisons_LocalDate(
            LocalDate.date(Year.MIN_YEAR, 1, 1),
            LocalDate.date(Year.MIN_YEAR, 12, 31),
            LocalDate.date(-1, 1, 1),
            LocalDate.date(-1, 12, 31),
            LocalDate.date(0, 1, 1),
            LocalDate.date(0, 12, 31),
            LocalDate.date(1, 1, 1),
            LocalDate.date(1, 12, 31),
            LocalDate.date(2006, 1, 1),
            LocalDate.date(2006, 12, 31),
            LocalDate.date(2007, 1, 1),
            LocalDate.date(2007, 12, 31),
            LocalDate.date(2008, 1, 1),
            LocalDate.date(2008, 2, 29),
            LocalDate.date(2008, 12, 31),
            LocalDate.date(Year.MAX_YEAR, 1, 1),
            LocalDate.date(Year.MAX_YEAR, 12, 31)
        );
    }

    void doTest_comparisons_LocalDate(LocalDate... localDates) {
        for (int i = 0; i < localDates.length; i++) {
            LocalDate a = localDates[i];
            for (int j = 0; j < localDates.length; j++) {
                LocalDate b = localDates[j];
                if (i < j) {
                    assertTrue(a.compareTo(b) < 0, a + " <=> " + b);
                    assertEquals(a.isBefore(b), true, a + " <=> " + b);
                    assertEquals(a.isAfter(b), false, a + " <=> " + b);
                    assertEquals(a.equals(b), false, a + " <=> " + b);
                } else if (i > j) {
                    assertTrue(a.compareTo(b) > 0, a + " <=> " + b);
                    assertEquals(a.isBefore(b), false, a + " <=> " + b);
                    assertEquals(a.isAfter(b), true, a + " <=> " + b);
                    assertEquals(a.equals(b), false, a + " <=> " + b);
                } else {
                    assertEquals(a.compareTo(b), 0, a + " <=> " + b);
                    assertEquals(a.isBefore(b), false, a + " <=> " + b);
                    assertEquals(a.isAfter(b), false, a + " <=> " + b);
                    assertEquals(a.equals(b), true, a + " <=> " + b);
                }
            }
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_compareTo_ObjectNull() {
        TEST_2007_07_15.compareTo(null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_isBefore_ObjectNull() {
        TEST_2007_07_15.isBefore(null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_isAfter_ObjectNull() {
        TEST_2007_07_15.isAfter(null);
    }

    @Test(expectedExceptions=ClassCastException.class)
    @SuppressWarnings("unchecked")
    public void compareToNonLocalDate() {
       Comparable c = TEST_2007_07_15;
       c.compareTo(new Object());
    }

    //-----------------------------------------------------------------------
    // equals()
    //-----------------------------------------------------------------------
    @Test(dataProvider="sampleDates")
    public void test_equals_true(int y, int m, int d) {
        LocalDate a = LocalDate.date(y, m, d);
        LocalDate b = LocalDate.date(y, m, d);
        assertEquals(a.equals(b), true);
    }
    @Test(dataProvider="sampleDates")
    public void test_equals_false_year_differs(int y, int m, int d) {
        LocalDate a = LocalDate.date(y, m, d);
        LocalDate b = LocalDate.date(y + 1, m, d);
        assertEquals(a.equals(b), false);
    }
    @Test(dataProvider="sampleDates")
    public void test_equals_false_month_differs(int y, int m, int d) {
        LocalDate a = LocalDate.date(y, m, d);
        LocalDate b = LocalDate.date(y, m + 1, d);
        assertEquals(a.equals(b), false);
    }
    @Test(dataProvider="sampleDates")
    public void test_equals_false_day_differs(int y, int m, int d) {
        LocalDate a = LocalDate.date(y, m, d);
        LocalDate b = LocalDate.date(y, m, d + 1);
        assertEquals(a.equals(b), false);
    }

    public void test_equals_itself_true() {
        assertEquals(TEST_2007_07_15.equals(TEST_2007_07_15), true);
    }

    public void test_equals_string_false() {
        assertEquals(TEST_2007_07_15.equals("2007-07-15"), false);
    }

    public void test_equals_null_false() {
        assertEquals(TEST_2007_07_15.equals(null), false);
    }

    //-----------------------------------------------------------------------
    // hashCode()
    //-----------------------------------------------------------------------
    @Test(dataProvider="sampleDates")
    public void test_hashCode(int y, int m, int d) {
        LocalDate a = LocalDate.date(y, m, d);
        assertEquals(a.hashCode(), a.hashCode());
        LocalDate b = LocalDate.date(y, m, d);
        assertEquals(a.hashCode(), b.hashCode());
    }

    //-----------------------------------------------------------------------
    // toString()
    //-----------------------------------------------------------------------
    @DataProvider(name="sampleToString")
    Object[][] provider_sampleToString() {
        return new Object[][] {
            {2008, 7, 5, "2008-07-05"},
            {2007, 12, 31, "2007-12-31"},
            {999, 12, 31, "0999-12-31"},
            {-1, 1, 2, "-0001-01-02"},
            {9999, 12, 31, "9999-12-31"},
            {-9999, 12, 31, "-9999-12-31"},
            {10000, 1, 1, "+10000-01-01"},
            {-10000, 1, 1, "-10000-01-01"},
            {12345678, 1, 1, "+12345678-01-01"},
            {-12345678, 1, 1, "-12345678-01-01"},
        };
    }

    @Test(dataProvider="sampleToString")
    public void test_toString(int y, int m, int d, String expected) {
        LocalDate t = LocalDate.date(y, m, d);
        String str = t.toString();
        assertEquals(str, expected);
    }

    //-----------------------------------------------------------------------
    // matchesDate()
    //-----------------------------------------------------------------------
    @Test(dataProvider="sampleDates")
    public void test_matchesDate_true(int y, int m, int d) {
        LocalDate a = LocalDate.date(y, m, d);
        LocalDate b = LocalDate.date(y, m, d);
        assertEquals(a.matchesDate(b), true);
    }
    @Test(dataProvider="sampleDates")
    public void test_matchesDate_false_year_differs(int y, int m, int d) {
        LocalDate a = LocalDate.date(y, m, d);
        LocalDate b = LocalDate.date(y + 1, m, d);
        assertEquals(a.matchesDate(b), false);
    }
    @Test(dataProvider="sampleDates")
    public void test_matchesDate_false_month_differs(int y, int m, int d) {
        LocalDate a = LocalDate.date(y, m, d);
        LocalDate b = LocalDate.date(y, m + 1, d);
        assertEquals(a.matchesDate(b), false);
    }
    @Test(dataProvider="sampleDates")
    public void test_matchesDate_false_day_differs(int y, int m, int d) {
        LocalDate a = LocalDate.date(y, m, d);
        LocalDate b = LocalDate.date(y, m, d + 1);
        assertEquals(a.matchesDate(b), false);
    }

    public void test_matchesDate_itself_true() {
        assertEquals(TEST_2007_07_15.matchesDate(TEST_2007_07_15), true);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_matchesDate_null() {
        TEST_2007_07_15.matchesDate(null);
    }
    
    //-----------------------------------------------------------------------
    // adjustDate()
    //-----------------------------------------------------------------------
    @Test(dataProvider="sampleDates")
    public void test_adjustDate(int y, int m, int d) {
        LocalDate a = LocalDate.date(y, m, d);
        assertSame(a.adjustDate(TEST_2007_07_15), a);
        assertSame(TEST_2007_07_15.adjustDate(a), TEST_2007_07_15);
    }

    public void test_adjustDate_same() {
        assertSame(LocalDate.date(2007, 7, 15).adjustDate(TEST_2007_07_15), TEST_2007_07_15);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_adjustDate_null() {
        TEST_2007_07_15.adjustDate(null);
    }
}
