package net.thevpc.nuts.core.test.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.core.NBootOptionsBuilder;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.time.NDuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LockTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void testNamedMemoryLock() {
        NLockFactory factory = NLockFactory.of();
        NLock lock = factory.of("test-mem-lock");

        Assertions.assertFalse(lock.isHeldByCurrentThread());
        lock.lock();
        try {
            Assertions.assertTrue(lock.isHeldByCurrentThread());
            Assertions.assertTrue(lock.isLocked());

            // Re-entrant lock
            lock.lock();
            try {
                Assertions.assertTrue(lock.isHeldByCurrentThread());
            } finally {
                lock.unlock();
            }

            Assertions.assertTrue(lock.isHeldByCurrentThread());
        } finally {
            lock.unlock();
        }
        Assertions.assertFalse(lock.isHeldByCurrentThread());
    }

    @Test
    public void testRunWithAndCallWith() {
        NLock lock = NLock.of("run-call-lock");
        AtomicBoolean ran = new AtomicBoolean(false);

        lock.runWith(() -> ran.set(true));
        Assertions.assertTrue(ran.get());

        String res = lock.callWith(() -> "Success");
        Assertions.assertEquals("Success", res);
    }

    @Test
    public void testDirectFileLock() throws IOException {
        Path tempDir = Files.createTempDirectory("nuts-lock-test");
        Path lockFilePath = tempDir.resolve("direct.lock");

        NLock fileLock = NLock.ofFile(lockFilePath);
        Assertions.assertFalse(Files.exists(lockFilePath));

        fileLock.runWith(() -> {
            Assertions.assertTrue(Files.exists(lockFilePath));
            Assertions.assertTrue(fileLock.isHeldByCurrentThread());
        });

        Assertions.assertFalse(Files.exists(lockFilePath));
    }

    @Test
    public void testCompanionFileLock() throws IOException {
        Path tempDir = Files.createTempDirectory("nuts-companion-test");
        Path dataFile = tempDir.resolve("data.json");
        Files.write(dataFile, "{}".getBytes());

        NLock companionLock = NLock.ofCompanion(dataFile);
        Path expectedCompanionLockFile = tempDir.resolve("data.json.lock");

        companionLock.runWith(() -> {
            Assertions.assertTrue(Files.exists(expectedCompanionLockFile));
            Assertions.assertTrue(Files.exists(dataFile));
        });

        Assertions.assertFalse(Files.exists(expectedCompanionLockFile));
        Assertions.assertTrue(Files.exists(dataFile));
    }

    @Test
    public void testCompanionDirectoryLock() throws IOException {
        Path tempDir = Files.createTempDirectory("nuts-dir-companion-test");
        NLock dirLock = NLock.ofCompanion(tempDir);
        Path expectedDirLockFile = tempDir.resolve(".nuts-lock");

        dirLock.runWith(() -> {
            Assertions.assertTrue(Files.exists(expectedDirLockFile));
        });

        Assertions.assertFalse(Files.exists(expectedDirLockFile));
    }

    @Test
    public void testStoreBackedLock() {
        NLockStore store = new net.thevpc.nuts.runtime.standalone.concurrent.NLockStoreMemory();
        NLockFactory factory = NLockFactory.of(store);
        NLock lock = factory.ofBuilder("store-lock-1")
                .leaseDuration(NDuration.ofMinutes(1))
                .build();

        AtomicInteger counter = new AtomicInteger(0);
        lock.runWith(() -> {
            counter.incrementAndGet();
            Assertions.assertTrue(lock.isHeldByCurrentThread());
            Assertions.assertTrue(lock.isLocked());
            NLockModel model = store.load("store-lock-1");
            Assertions.assertNotNull(model);
            Assertions.assertFalse(model.isExpired());
        });

        Assertions.assertEquals(1, counter.get());
        Assertions.assertFalse(lock.isHeldByCurrentThread());
    }
}
