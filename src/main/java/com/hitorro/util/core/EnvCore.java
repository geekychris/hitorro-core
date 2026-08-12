/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * JDK-only companion to {@link Env}. Provides system-info methods that use only
 * {@code java.lang.System} and {@code java.net.InetAddress} — no dependency on
 * BaseFile/Properties/Log infrastructure. Any hitorro-core code that just needs
 * "what host am I on / where is my temp dir / how many CPUs" should use EnvCore
 * to avoid pulling in the full config/property machinery.
 */
public final class EnvCore {
    private EnvCore() {}

    // Common environment key constants (also defined on Env in streams).
    public static final String OS_BIN_DIR = "os_bin_dir";
    public static final String EXEC_EXTENSION = "exec_extension";
    public static final String SCRIPT_PLAT_DIR = "script_plat_dir";

    /** JDK-only Thread.sleep wrapper returning false if interrupted. */
    public static boolean sleepMillis(long millis) {
        try { Thread.sleep(millis); return true; }
        catch (InterruptedException e) { return false; }
    }

    public static boolean sleepNSeconds(int seconds) {
        return sleepMillis(seconds * 1000L);
    }

    public static boolean sleepNSeconds(int seconds, Object lock) {
        synchronized (lock) {
            try { lock.wait(seconds * 1000L); return true; }
            catch (InterruptedException e) { return false; }
        }
    }

    /**
     * Best-effort platform-script directory. Uses the {@code ht.script.dir}
     * system property if set, else falls back to the JDK temp directory.
     * (The full Env in streams resolves this from HTProperties.)
     */
    public static File getPlatformScriptDirectory() {
        String p = System.getProperty("ht.script.dir");
        if (p != null) return new File(p);
        return getTempDirectory();
    }

    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return System.getProperty("user.name", "unknown-host");
        }
    }

    public static String getHostIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    public static String getNodeId() {
        return System.getProperty("user.name", "node");
    }

    public static File getTempDirectory() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    public static int getCPUCores() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static String getPathSeparator() {
        return File.pathSeparator;
    }

    /** Alias for compatibility with the original Env spelling. */
    public static String getPathSeperator() {
        return File.pathSeparator;
    }

    public static String getOsName() {
        return System.getProperty("os.name");
    }

    public static String getOsVersion() {
        return System.getProperty("os.version");
    }

    public static Map<Object, Object> getJavaProps() {
        Properties props = System.getProperties();
        return props;
    }

    public static Date dumpTime() {
        return new Date();
    }

    /**
     * Free memory (bytes) in the JVM. Simple accessor without ManagementFactory pomp.
     */
    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public static String getUserHome() {
        return System.getProperty("user.home");
    }

    public static String getUserDir() {
        return System.getProperty("user.dir");
    }

    /**
     * HT_HOME directory (via env var or -Dht.home). The full Env in streams
     * resolves this through the property system; this fallback uses process env.
     */
    public static File getHome() {
        String p = System.getProperty("HT_HOME");
        if (p == null) p = System.getProperty("ht.home");
        if (p == null) p = System.getenv("HT_HOME");
        if (p == null) p = getUserDir();
        return new File(p);
    }

    /** HT_BIN directory. */
    public static File getBin() {
        String p = System.getProperty("HT_BIN");
        if (p == null) p = System.getProperty("ht.bin");
        if (p == null) p = System.getenv("HT_BIN");
        if (p == null) p = getUserDir();
        return new File(p);
    }

    /** Archive-log directory. Uses ht.archive.log.dir sysprop or ${HT_HOME}/logs/archive. */
    public static File getArchiveLogDir() {
        String p = System.getProperty("ht.archive.log.dir");
        return p != null ? new File(p) : new File(getHome(), "logs/archive");
    }

    /** HT_DATA directory. */
    public static File getData() {
        String p = System.getProperty("HT_DATA");
        if (p == null) p = System.getProperty("ht.data");
        if (p == null) p = System.getenv("HT_DATA");
        if (p == null) p = new File(getHome(), "data").getAbsolutePath();
        return new File(p);
    }

    /**
     * Best-effort system-args map. The full {@code Env} in streams populates this from
     * HTProperties/BaseFile; here we return the JVM system properties as strings.
     */
    /**
     * Best-effort machine key: {@code <ip>-<user>-<pid>}. The full {@code Env}
     * derives serverId/globalId from HTProperties; this fallback keeps the same
     * shape but uses JDK-only sources so it works without the property system.
     */
    public static String getMachineKey() {
        return getHostIP() + "-" + getNodeId() + "-" + ProcessHandle.current().pid();
    }

    public static java.util.Map<String, String> getSystemArgs() {
        java.util.TreeMap<String, String> map = new java.util.TreeMap<>();
        for (java.util.Map.Entry<Object, Object> e : System.getProperties().entrySet()) {
            map.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
        }
        return map;
    }
}
