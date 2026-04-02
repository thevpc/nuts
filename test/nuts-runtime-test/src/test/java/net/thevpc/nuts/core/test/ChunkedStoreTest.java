package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.util.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ChunkedStoreTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace("--progress");
    }

    @Test
    public void test2() {
        if(!_TestConfig.ENABLE_PERF){
            return;
        }
        NChronometer chronometer = NChronometer.of();
        try (NChunkedStore<String> fq = NChunkedStoreBuilder.ofLines(NPath.ofUserHome().resolve("test-file-queue"))
                .setAppend(false)
                .setBufferSize(1000)
                .setChunkSize(1000000)
                .setNumberLayout(10)
                .build()
        ) {
            for (int i = 0; i < 5000000; i++) {
                fq.add(String.valueOf(i));
            }
        }
        System.out.println("write in "+chronometer.stop());


        chronometer = NChronometer.of();
        try (NChunkedStore<String> fq = NChunkedStoreBuilder.ofLines(NPath.ofUserHome().resolve("test-file-queue"))
                .setMetadataBufferSize(100)
                .setNumberLayout(3)
                .build()
        ) {
            try (NStream<String> st = fq.stream()) {
                st.forEach(d->{
                    System.out.println("READ "+d);
                });
            }
        }
        System.out.println("read in "+chronometer.stop());
    }

    @Test
    public void test1() {
        try (NChunkedStore<String> fq = NChunkedStoreBuilder.ofLines(NPath.ofUserHome().resolve("test-file-queue"))
                .setAppend(false)
                .setBufferSize(2)
                .setChunkSize(5)
                .setNumberLayout(3)
                .build()
        ) {
            for (int i = 0; i < 5; i++) {
                fq.add(String.valueOf(i));
            }
        }
        try (NChunkedStore<String> fq = NChunkedStoreBuilder.ofLines(NPath.ofUserHome().resolve("test-file-queue"))
                .setAppend(true)
                .setBufferSize(2)
                .setChunkSize(5)
                .setNumberLayout(3)
                .build()
        ) {
            for (int i = 0; i < 5; i++) {
                fq.add(String.valueOf(i+5));
            }
        }


        try (NChunkedStore<String> fq = NChunkedStoreBuilder.ofLines(NPath.ofUserHome().resolve("test-file-queue"))
                .setMetadataBufferSize(1)
                .setNumberLayout(3)
                .build()
        ) {
            try (NStream<String> st = fq.stream().limit(10)) {
                st.forEach(d->{
                    System.out.println("READ "+d);
                });
            }
        }
        System.out.println("----");
        try (NChunkedStore<String> fq = NChunkedStoreBuilder.ofLines(NPath.ofUserHome().resolve("test-file-queue"))
                .setMetadataBufferSize(10)
                .setNumberLayout(3)
                .build()
        ) {
            try (NStream<String> st = fq.stream().limit(10)) {
                st.forEach(d->{
                    System.out.println("READ "+d);
                });
            }
        }

    }

}
