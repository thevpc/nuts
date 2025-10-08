package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class NNames {
    private static Set<String> loadNames(String... resources) {
        Set<String> all = new TreeSet<>();
        for (String resource : resources) {
            Enumeration<URL> found = null;
            try {
                found = NNames.class.getClassLoader().getResources(resource);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while (found.hasMoreElements()) {
                URL u = found.nextElement();
                try (InputStream is = u.openStream()) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            line = line.trim();
                            if (!line.isEmpty()) {
                                if (!line.startsWith("#")) {
                                    for (String s : line.split("[, ;]+")) {
                                        String ts = s.trim();
                                        if (!ts.isEmpty()) {
                                            all.add(ts.toLowerCase());
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    //
                }
            }
        }
        return all;
    }

    public static List<String> ADJECTIVES = Collections.unmodifiableList(new ArrayList<>(loadNames("net/thevpc/nuts/adjectives.txt")));
    public static List<String> NAMES = Collections.unmodifiableList(new ArrayList<>(loadNames("net/thevpc/nuts/names.txt")));

    private static Map<Integer, List<String[]>> CACHED_COLOR_NAMES_BY_EQ_SIZE = new HashMap<>();
    private static Map<Integer, List<String[]>> CACHED_COLOR_NAMES_BY_LTE_SIZE = new HashMap<>();


    /**
     * Picks a name using the given hash, with a number of words between minCount and maxCount (inclusive).
     * <p>
     * The generated name consists of a combination of adjectives, color names, and personal/fantasy names.
     * The total number of words in the name will be between {@code minCount} and {@code maxCount}, inclusive.
     * If {@code maxCount} is less than {@code minCount}, it will be automatically raised to {@code minCount}.
     * <p>
     * The selection is deterministic: the same hash will always produce the same name.
     * <p>
     * If {@code format} is {@code null}, {@link NNameFormat#TITLE_NAME} will be used as the default.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>
     * pickName(12345, 2, 3, NNameFormat.LOWER_CAMEL_CASE) -> "swiftNavyBlueTuring"
     * pickName(67890, 1, 2, NNameFormat.KEBAB_CASE)     -> "bright-tesla"
     * pickName(54321, 1, 3, null)                        -> "Curious Red Einstein" // uses TITLE_NAME
     * </pre>
     *
     * @param hash     an integer used as a deterministic seed for name selection
     * @param minCount minimum number of words in the resulting name (>= 1, inclusive)
     * @param maxCount maximum number of words in the resulting name (>= minCount, inclusive)
     * @param format   the {@link NNameFormat} used to format the resulting name; if {@code null}, defaults to {@link NNameFormat#TITLE_NAME}
     * @return a deterministic pseudo-random name composed of adjectives, color names, and names
     * @throws IllegalArgumentException if {@code minCount < 1} or other invalid input
     */
    public static String pickName(int hash, int minCount, int maxCount, NNameFormat format) {
        if (format == null) {
            format = NNameFormat.TITLE_NAME;
        }
        if (minCount <= 0) {
            minCount = 1;
        }
        if (maxCount <= minCount) {
            maxCount = minCount;
        }
        int range = Math.max(1, maxCount - minCount + 1);
        int wordsCount = ihash(hash, range) + minCount;
        NAssert.requireTrue(wordsCount > 0, () -> NMsg.ofC("size must be > 0"));
        if (wordsCount == 1) {
            int usecase = ihash(hash, 2);
            switch (usecase) {
                case 0: {
                    return format.format(lhash(hash, NAMES));
                }
                case 1: {
                    List<String[]> parts = colorNames(1);
                    String[] selected = parts.get(ihash(hash, parts.size()));
                    return format.format(selected);
                }
            }
            throw new IllegalArgumentException("should never happen");
        } else if (wordsCount == 2) {
            int usecase = ihash(hash, 3);
            switch (usecase) {
                case 0: {
                    String[] aa = new String[]{
                            lhash(hash, ADJECTIVES),
                            lhash(hash, NAMES),
                    };
                    return format.format(aa);
                }
                case 1: {
                    List<String[]> parts = colorNames(1);
                    String[] selected = parts.get(ihash(hash, parts.size()));
                    return format.format(new String[]{lhash(hash, ADJECTIVES), selected[0]});
                }
                case 2: {
                    List<String[]> parts = colorNames(2);
                    String[] selected = parts.get(ihash(hash, parts.size()));
                    return format.format(selected);
                }
            }
            throw new IllegalArgumentException("should never happen");
        } else if (wordsCount == 3) {
            int usecase = ihash(hash, 3);
            switch (usecase) {
                case 0: {
                    List<String> aa = new ArrayList<>();
                    aa.add(lhash(hash, ADJECTIVES));
                    List<String[]> parts = colorNames(1);
                    String[] selected = parts.get(ihash(hash, parts.size()));
                    aa.add(selected[0]);
                    aa.add(lhash(hash, NAMES));
                    return format.format(aa.toArray(new String[0]));
                }
                case 1: {
                    List<String> aa = new ArrayList<>();
                    aa.add(lhash(hash, ADJECTIVES));
                    List<String[]> parts = colorNames(2);
                    String[] selected = parts.get(ihash(hash, parts.size()));
                    aa.addAll(Arrays.asList(selected));
                    return format.format(aa.toArray(new String[0]));
                }
                case 2: {
                    List<String> aa = new ArrayList<>();
                    List<String[]> parts = colorNames(3);
                    String[] selected = parts.get(ihash(hash, parts.size()));
                    aa.addAll(Arrays.asList(selected));
                    return format.format(aa.toArray(new String[0]));
                }
            }
            throw new IllegalArgumentException("should never happen");
        } else {
            // a adjectives
            // b color
            // c name
            // a+b+c=n, n>3, c=0 or1
            List<int[]> triples = findTriples(wordsCount);

            int[] useCase = triples.get(ihash(hash, triples.size()));
            int adjectives = useCase[0];
            int colors = useCase[1];
            int names = useCase[2];

            List<String> aa = new ArrayList<>();
            {
                int h = hash;
                // Pick adjectives
                for (int i = 0; i < adjectives; i++) {
                    String word = lhash(h, ADJECTIVES);
                    h = Integer.rotateLeft(h, 5) ^ word.hashCode(); // mix hash
                    aa.add(word);
                }
            }
            {
                int h = hash;
                // Pick adjectives
                int remains = colors;
                while (remains > 0) {
                    List<String[]> parts = colorNamesLte(Math.min(3, remains));
                    String[] selected = parts.get(ihash(h, parts.size()));
                    aa.addAll(Arrays.asList(selected));
                    for (int i = 0; i < selected.length; i++) {
                        h = Integer.rotateLeft(h, 5) ^ selected[i].hashCode(); // mix hash
                    }
                    remains -= selected.length;
                }
            }
            {
                int h = hash;
                // Pick adjectives
                for (int i = 0; i < names; i++) {
                    String word = lhash(h, NAMES);
                    h = Integer.rotateLeft(h, 5) ^ word.hashCode(); // mix hash
                    aa.add(word);
                }
            }
            return format.format(aa.toArray(new String[0]));
        }
    }

    private static List<int[]> findTriples(int n) {
        if (n <= 3) throw new IllegalArgumentException("n must be > 3");
        List<int[]> result = new ArrayList<>();

        // Case c = 0
        for (int a = 0; a <= n; a++) {
            int b = n - a;
            result.add(new int[]{a, b, 0});
        }

        // Case c = 1
        for (int a = 0; a <= n - 1; a++) {
            int b = n - 1 - a;
            result.add(new int[]{a, b, 1});
        }

        return result;
    }

    private static int ihash(int hash, int count) {
        return Math.abs(hash) % count;
    }

    private static String lhash(int hash, List<String> list) {
        return list.get(ihash(hash, list.size()));
    }

    private static List<String[]> colorNames(int size) {
        return CACHED_COLOR_NAMES_BY_EQ_SIZE.computeIfAbsent(size, currentSize -> NColors.ALL_CANONICAL.stream().map(x -> {
            String[] a = NNameFormat.parse(x.getName());
            if (a.length == currentSize) {
                return a;
            } else {
                return null;
            }
        }).filter(x -> x != null).collect(Collectors.toList()));
    }

    private static List<String[]> colorNamesLte(int size) {
        return CACHED_COLOR_NAMES_BY_LTE_SIZE.computeIfAbsent(size, currentSize -> NColors.ALL_CANONICAL.stream().map(x -> {
            String[] a = NNameFormat.parse(x.getName());
            if (a.length <= currentSize) {
                return a;
            } else {
                return null;
            }
        }).filter(x -> x != null).collect(Collectors.toList()));
    }
}


