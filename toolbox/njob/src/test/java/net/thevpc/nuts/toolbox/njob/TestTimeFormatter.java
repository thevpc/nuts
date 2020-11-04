package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.toolbox.njob.time.TimeFormatter;
import net.thevpc.nuts.toolbox.njob.time.TimespanPattern;
import net.thevpc.nuts.toolbox.njob.time.TimePeriod;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TestTimeFormatter {
    @Test
    public void test1(){
        System.out.println(new TimeFormatter().format(
                LocalDateTime.now().plusMinutes(-1631)
        ));
    }

    @Test
    public void test2(){
        System.out.println(
                new TimePeriod(165321.365254, ChronoUnit.SECONDS).toTimePeriods().toUnit(ChronoUnit.SECONDS, TimespanPattern.DEFAULT)
        );
    }
}
