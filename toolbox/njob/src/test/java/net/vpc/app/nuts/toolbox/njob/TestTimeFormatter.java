package net.vpc.app.nuts.toolbox.njob;

import net.vpc.app.nuts.toolbox.njob.time.TimeFormatter;
import net.vpc.app.nuts.toolbox.njob.time.TimePeriod;
import net.vpc.app.nuts.toolbox.njob.time.TimespanPattern;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

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
                new TimePeriod(165321.365254, TimeUnit.SECONDS).toTimePeriods().toUnit(TimeUnit.SECONDS, TimespanPattern.DEFAULT)
        );
    }
}
