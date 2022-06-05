/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.util.NutsDuration;
import net.thevpc.nuts.util.NutsDurationFormatMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

/**
 * @author thevpc
 */
public class Test36_NutsDuration {

    @Test
    public void test01() {
        long[] time=new long[ChronoUnit.values().length];
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.NANOS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.NANOS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.DEFAULT);
        System.out.println(s);
        Assertions.assertEquals("0ns",s);
    }

    @Test
    public void test02() {
        long[] time=new long[ChronoUnit.values().length];
        NutsDuration t = NutsDuration.of(time).withSmallestUnit(ChronoUnit.SECONDS);
        Assertions.assertEquals(ChronoUnit.SECONDS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.SECONDS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.DEFAULT);
        System.out.println(s);
        Assertions.assertEquals("0s",s);
    }

    @Test
    public void test03() {
        long[] time=new long[ChronoUnit.values().length];
        time[ChronoUnit.NANOS.ordinal()]=16;
        time[ChronoUnit.MICROS.ordinal()]=17;
        NutsDuration t = NutsDuration.of(time).withSmallestUnit(ChronoUnit.SECONDS);
        Assertions.assertEquals(ChronoUnit.SECONDS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.SECONDS,t.getLargestUnit());
        String s=t.toString();
        System.out.println(s);
        Assertions.assertEquals("0s",s);
    }

    @Test
    public void test04() {
        long[] time=new long[ChronoUnit.values().length];
        time[ChronoUnit.NANOS.ordinal()]=16;
        time[ChronoUnit.MILLIS.ordinal()]=17;
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.NANOS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.MILLIS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.DEFAULT);
        System.out.println(s);
        Assertions.assertEquals("17ms 16ns",s);
    }

    @Test
    public void test05() {
        long[] time=new long[ChronoUnit.values().length];
        time[ChronoUnit.NANOS.ordinal()]=999;
        time[ChronoUnit.MILLIS.ordinal()]=999;
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.NANOS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.MILLIS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.DEFAULT);
        System.out.println(s);
        Assertions.assertEquals("999ms 999ns",s);

    }

    @Test
    public void test06() {
        long[] time=new long[ChronoUnit.values().length];
        time[ChronoUnit.NANOS.ordinal()]=1;
        time[ChronoUnit.MICROS.ordinal()]=2;
        time[ChronoUnit.MILLIS.ordinal()]=3;
        time[ChronoUnit.SECONDS.ordinal()]=4;
        time[ChronoUnit.MINUTES.ordinal()]=5;
        time[ChronoUnit.HOURS.ordinal()]=6;
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.NANOS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.HOURS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.DEFAULT);
        System.out.println(s);
        Assertions.assertEquals("6h 5mn 4s 3ms 2us 1ns",s);

    }


    @Test
    public void test07() {
        long[] time=new long[ChronoUnit.values().length];
        time[ChronoUnit.NANOS.ordinal()]=1;
        time[ChronoUnit.MICROS.ordinal()]=2;
        time[ChronoUnit.MILLIS.ordinal()]=3;
        time[ChronoUnit.SECONDS.ordinal()]=4;
        time[ChronoUnit.MINUTES.ordinal()]=5;
        time[ChronoUnit.HOURS.ordinal()]=6;
        time[ChronoUnit.DAYS.ordinal()]=7;
        time[ChronoUnit.WEEKS.ordinal()]=8;
        time[ChronoUnit.MONTHS.ordinal()]=9;
        time[ChronoUnit.YEARS.ordinal()]=10;
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.NANOS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.YEARS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.DEFAULT);
        System.out.println(s);
        Assertions.assertEquals("10y 9m 8w 7d 6h 5mn 4s 3ms 2us 1ns",s);
    }

    @Test
    public void test08() {
        long[] time=new long[ChronoUnit.values().length];
//        time[ChronoUnit.NANOS.ordinal()]=1;
        time[ChronoUnit.MICROS.ordinal()]=2;
        time[ChronoUnit.MILLIS.ordinal()]=3;
//        time[ChronoUnit.SECONDS.ordinal()]=4;
        time[ChronoUnit.MINUTES.ordinal()]=5;
        time[ChronoUnit.HOURS.ordinal()]=6;
//        time[ChronoUnit.DAYS.ordinal()]=7;
        time[ChronoUnit.WEEKS.ordinal()]=8;
        time[ChronoUnit.MONTHS.ordinal()]=9;
        time[ChronoUnit.YEARS.ordinal()]=10;
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.MICROS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.YEARS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.DEFAULT);
        System.out.println(s);
        Assertions.assertEquals("10y 9m 8w 6h 5mn 3ms 2us",s);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test101() {
        long[] time=new long[ChronoUnit.values().length];
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.NANOS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.NANOS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.FIXED);
        System.out.println(s);
        Assertions.assertEquals("  0ns",s);
    }

    @Test
    public void test102() {
        long[] time=new long[ChronoUnit.values().length];
        NutsDuration t = NutsDuration.of(time).withSmallestUnit(ChronoUnit.SECONDS);
        Assertions.assertEquals(ChronoUnit.SECONDS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.SECONDS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.FIXED);
        System.out.println(s);
        Assertions.assertEquals(" 0s",s);
    }

    @Test
    public void test103() {
        long[] time=new long[ChronoUnit.values().length];
        time[ChronoUnit.NANOS.ordinal()]=16;
        time[ChronoUnit.MICROS.ordinal()]=17;
        NutsDuration t = NutsDuration.of(time).withSmallestUnit(ChronoUnit.SECONDS);
        Assertions.assertEquals(ChronoUnit.SECONDS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.SECONDS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.FIXED);
        System.out.println(s);
        Assertions.assertEquals(" 0s",s);
    }

    @Test
    public void test104() {
        long[] time=new long[ChronoUnit.values().length];
        time[ChronoUnit.NANOS.ordinal()]=16;
        time[ChronoUnit.MILLIS.ordinal()]=17;
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.NANOS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.MILLIS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.FIXED);
        System.out.println(s);
        Assertions.assertEquals(" 17ms   0us  16ns",s);
    }

    @Test
    public void test105() {
        long[] time=new long[ChronoUnit.values().length];
        time[ChronoUnit.NANOS.ordinal()]=999;
        time[ChronoUnit.MILLIS.ordinal()]=999;
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.NANOS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.MILLIS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.FIXED);
        System.out.println(s);
        Assertions.assertEquals("999ms   0us 999ns",s);

    }

    @Test
    public void test106() {
        long[] time=new long[ChronoUnit.values().length];
        time[ChronoUnit.NANOS.ordinal()]=1;
        time[ChronoUnit.MICROS.ordinal()]=2;
        time[ChronoUnit.MILLIS.ordinal()]=3;
        time[ChronoUnit.SECONDS.ordinal()]=4;
        time[ChronoUnit.MINUTES.ordinal()]=5;
        time[ChronoUnit.HOURS.ordinal()]=6;
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.NANOS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.HOURS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.FIXED);
        System.out.println(s);
        Assertions.assertEquals(" 6h  5mn  4s   3ms   2us   1ns",s);

    }


    @Test
    public void test107() {
        long[] time=new long[ChronoUnit.values().length];
        time[ChronoUnit.NANOS.ordinal()]=1;
        time[ChronoUnit.MICROS.ordinal()]=2;
        time[ChronoUnit.MILLIS.ordinal()]=3;
        time[ChronoUnit.SECONDS.ordinal()]=4;
        time[ChronoUnit.MINUTES.ordinal()]=5;
        time[ChronoUnit.HOURS.ordinal()]=6;
        time[ChronoUnit.DAYS.ordinal()]=7;
        time[ChronoUnit.WEEKS.ordinal()]=8;
        time[ChronoUnit.MONTHS.ordinal()]=9;
        time[ChronoUnit.YEARS.ordinal()]=10;
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.NANOS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.YEARS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.FIXED);
        System.out.println(s);
        Assertions.assertEquals("10y  9m  8w  7d  6h  5mn  4s   3ms   2us   1ns",s);
    }

    @Test
    public void test108() {
        long[] time=new long[ChronoUnit.values().length];
//        time[ChronoUnit.NANOS.ordinal()]=1;
        time[ChronoUnit.MICROS.ordinal()]=2;
        time[ChronoUnit.MILLIS.ordinal()]=3;
//        time[ChronoUnit.SECONDS.ordinal()]=4;
        time[ChronoUnit.MINUTES.ordinal()]=5;
        time[ChronoUnit.HOURS.ordinal()]=6;
//        time[ChronoUnit.DAYS.ordinal()]=7;
        time[ChronoUnit.WEEKS.ordinal()]=8;
        time[ChronoUnit.MONTHS.ordinal()]=9;
        time[ChronoUnit.YEARS.ordinal()]=10;
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.MICROS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.YEARS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.FIXED);
        System.out.println(s);
        Assertions.assertEquals("10y  9m  8w  0d  6h  5mn  0s   3ms   2us",s);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test201() {
        long[] time=new long[ChronoUnit.values().length];
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.NANOS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.NANOS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.CLOCK);
        System.out.println(s);
        Assertions.assertEquals("000s",s);
    }

    @Test
    public void test202() {
        long[] time=new long[ChronoUnit.values().length];
        NutsDuration t = NutsDuration.of(time).withSmallestUnit(ChronoUnit.SECONDS);
        Assertions.assertEquals(ChronoUnit.SECONDS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.SECONDS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.CLOCK);
        System.out.println(s);
        Assertions.assertEquals("00s",s);
    }

    @Test
    public void test203() {
        long[] time=new long[ChronoUnit.values().length];
        time[ChronoUnit.NANOS.ordinal()]=16;
        time[ChronoUnit.MICROS.ordinal()]=17;
        NutsDuration t = NutsDuration.of(time).withSmallestUnit(ChronoUnit.SECONDS);
        Assertions.assertEquals(ChronoUnit.SECONDS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.SECONDS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.CLOCK);
        System.out.println(s);
        Assertions.assertEquals("00s",s);
    }

    @Test
    public void test204() {
        long[] time=new long[ChronoUnit.values().length];
        time[ChronoUnit.NANOS.ordinal()]=16;
        time[ChronoUnit.MILLIS.ordinal()]=17;
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.NANOS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.MILLIS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.CLOCK);
        System.out.println(s);
        Assertions.assertEquals("00.017000016s",s);
    }

    @Test
    public void test205() {
        long[] time=new long[ChronoUnit.values().length];
        time[ChronoUnit.NANOS.ordinal()]=999;
        time[ChronoUnit.MILLIS.ordinal()]=999;
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.NANOS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.MILLIS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.CLOCK);
        System.out.println(s);
        Assertions.assertEquals("00.999000999s",s);

    }

    @Test
    public void test206() {
        long[] time=new long[ChronoUnit.values().length];
        time[ChronoUnit.NANOS.ordinal()]=1;
        time[ChronoUnit.MICROS.ordinal()]=2;
        time[ChronoUnit.MILLIS.ordinal()]=3;
        time[ChronoUnit.SECONDS.ordinal()]=4;
        time[ChronoUnit.MINUTES.ordinal()]=5;
        time[ChronoUnit.HOURS.ordinal()]=6;
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.NANOS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.HOURS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.CLOCK);
        System.out.println(s);
        Assertions.assertEquals("06:05:04.003002001",s);

    }


    @Test
    public void test207() {
        long[] time=new long[ChronoUnit.values().length];
        time[ChronoUnit.NANOS.ordinal()]=1;
        time[ChronoUnit.MICROS.ordinal()]=2;
        time[ChronoUnit.MILLIS.ordinal()]=3;
        time[ChronoUnit.SECONDS.ordinal()]=4;
        time[ChronoUnit.MINUTES.ordinal()]=5;
        time[ChronoUnit.HOURS.ordinal()]=6;
        time[ChronoUnit.DAYS.ordinal()]=7;
        time[ChronoUnit.WEEKS.ordinal()]=8;
        time[ChronoUnit.MONTHS.ordinal()]=9;
        time[ChronoUnit.YEARS.ordinal()]=10;
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.NANOS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.YEARS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.CLOCK);
        System.out.println(s);
        Assertions.assertEquals("10y  9m  8w  7d 06:05:04.003002001",s);
    }

    @Test
    public void test208() {
        long[] time=new long[ChronoUnit.values().length];
//        time[ChronoUnit.NANOS.ordinal()]=1;
        time[ChronoUnit.MICROS.ordinal()]=2;
        time[ChronoUnit.MILLIS.ordinal()]=3;
//        time[ChronoUnit.SECONDS.ordinal()]=4;
        time[ChronoUnit.MINUTES.ordinal()]=5;
        time[ChronoUnit.HOURS.ordinal()]=6;
//        time[ChronoUnit.DAYS.ordinal()]=7;
        time[ChronoUnit.WEEKS.ordinal()]=8;
        time[ChronoUnit.MONTHS.ordinal()]=9;
        time[ChronoUnit.YEARS.ordinal()]=10;
        NutsDuration t = NutsDuration.of(time);
        Assertions.assertEquals(ChronoUnit.MICROS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.YEARS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.CLOCK);
        System.out.println(s);
        Assertions.assertEquals("10y  9m  8w  0d 06:05:00.003002",s);
    }

    @Test
    public void test209() {
        long[] time=new long[ChronoUnit.values().length];
//        time[ChronoUnit.NANOS.ordinal()]=1;
        time[ChronoUnit.MICROS.ordinal()]=2;
        time[ChronoUnit.MILLIS.ordinal()]=3;
//        time[ChronoUnit.SECONDS.ordinal()]=4;
        time[ChronoUnit.MINUTES.ordinal()]=5;
        time[ChronoUnit.HOURS.ordinal()]=6;
//        time[ChronoUnit.DAYS.ordinal()]=7;
        time[ChronoUnit.WEEKS.ordinal()]=3;
        time[ChronoUnit.MONTHS.ordinal()]=9;
        time[ChronoUnit.YEARS.ordinal()]=10;
        NutsDuration t = NutsDuration.of(time).withSmallestUnit(ChronoUnit.NANOS);
        Assertions.assertEquals(ChronoUnit.NANOS,t.getSmallestUnit());
        Assertions.assertEquals(ChronoUnit.YEARS,t.getLargestUnit());
        String s=t.toString(NutsDurationFormatMode.CLOCK);
        System.out.println(s);
        Assertions.assertEquals("10y  9m  3w  0d 06:05:00.003002000",s);
    }


}
