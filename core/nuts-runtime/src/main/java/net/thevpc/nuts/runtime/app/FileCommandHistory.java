//package net.thevpc.nuts.runtime.app;
//
//import net.thevpc.nuts.core.app.CommandHistory;
//import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
//
//import java.io.*;
//import java.util.LinkedHashSet;
//import java.util.Map;
//
//public class FileCommandHistory implements CommandHistory {
//
//    private int maxEntries = 100;
//    private LinkedHashSet<String> historySet = new LinkedHashSet<String>() {
//        protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
//            return size() > maxEntries;
//        }
//    };
//    private File file;
//
//    public FileCommandHistory(String file) {
//        if (file.startsWith("~/") || file.startsWith("~\\")) {
//            file = System.getProperty("user.home") + File.separatorChar + file.substring(2);
//        }
//        this.file = new File(file);
//
//    }
//
//    public void load() throws IOException {
//        if (file.exists()) {
//            BufferedReader r = new BufferedReader(new FileReader(file));
//            String line = null;
//            while ((line = r.readLine()) != null) {
//                historySet.add(line);
//            }
//            r.close();
//        }
//    }
//
//    public void save() throws IOException {
//        file.getParentFile().mkdirs();
//        PrintStream r = new PrintStream(file);
//        for (String o : historySet) {
//            r.println(o);
//        }
//        r.close();
//    }
//
//    public void append(String command) throws IOException {
//        //force put it at the end!
//        historySet.remove(command);
//        historySet.add(command);
//        file.getParentFile().mkdirs();
//        PrintStream r = CoreIOUtils.toPrintStream(new FileOutputStream(file, true));
//        r.println(command);
//        r.close();
//    }
//
//    public String[] list() {
//        return historySet.toArray(new String[0]);
//    }
//
//    public String[] tail(int n) {
//        String[] all = list();
//        if (n <= 0 || n >= all.length) {
//            n = all.length;
//        }
//        String[] c = new String[n];
//        System.arraycopy(all, all.length - n, c, 0, n);
//        return c;
//    }
//
//    public String[] head(int n) {
//        String[] all = list();
//        if (n <= 0 || n >= all.length) {
//            n = all.length;
//        }
//        String[] c = new String[n];
//        System.arraycopy(all, 0, c, 0, n);
//        return c;
//    }
//}
