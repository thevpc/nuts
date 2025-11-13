package net.thevpc.nuts.core.test;

import net.thevpc.nuts.concurrent.NTaskResult;
import net.thevpc.nuts.concurrent.NTaskSet;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.util.NExceptions;
import net.thevpc.nuts.util.NOptional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TaskSetTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace("--progress");
    }


    @Test
    public void test1() throws Exception {
        NTaskSet nTaskSet = NTaskSet.of();
        NChronometer ch = NChronometer.startNow();
        NOptional<NTaskResult<Boolean>> firstError = nTaskSet
                .call("verif1",() -> verify1())
                .call("verif2",() -> verify2())
                .call("verif3",() -> verify3())
                .<Boolean>firstMatch(x -> {
                    if (x.isSuccess()) {
                        boolean b = (Boolean) x.getResult();
                        if (!b) {
                            // return with first check that fails!
                            return true;
                        }
                    } else {
                        // this task could not be called
                        // we will throw the exception
                        throw NExceptions.ofUncheckedException(x.getError());
                    }
                    return false;
                }, true);
        // return true if none of the checks fail
        boolean allOk=!firstError.isPresent();
        if(allOk){
            NOut.println("All ok "+ch);
        }else{
            NOut.println("some rejections id="+firstError.get().getTaskId()+" : "+firstError.get().getError()+" :: " +ch);
        }
        ch.start();
    }

    private boolean verify1() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
    private boolean verify2() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("unexpected error in verify2");
        //return false;
    }
    private boolean verify3() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
