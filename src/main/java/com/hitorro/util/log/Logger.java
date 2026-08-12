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
package com.hitorro.util.log;

import org.apache.log4j.Level;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Lightweight Logger wrapper for hitorro-core.
 * Wraps log4j Logger with formatted message support matching the hitorro-util Logger API.
 * The full-featured Logger in hitorro-util extends this class's contract.
 */
public class Logger {
    public static final String ExitEvent = "ExitEvent";
    public static File s_archiveDir = null;

    private final org.apache.log4j.Logger delegate;

    protected Logger(String name) {
        this.delegate = org.apache.log4j.Logger.getLogger(name);
    }

    public static Logger getLogger(String name) {
        return new Logger(name);
    }

    public static final String generateMessageWithCallstack(String msg, Throwable t) {
        StringWriter sw = new StringWriter();
        sw.append(msg);
        sw.append("\n");
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    // --- Fatal ---

    public void fatal(String message) {
        delegate.fatal(message);
    }

    public void fatal(String fmtString, Object... args) {
        if (delegate.isEnabledFor(Level.FATAL)) {
            delegate.fatal(formatMessage(fmtString, args));
        }
    }

    public void fatal(Throwable t, String str) {
        delegate.fatal(str, t);
    }

    public void fatal(Throwable t, String fmtString, Object... args) {
        if (delegate.isEnabledFor(Level.FATAL)) {
            delegate.fatal(formatMessage(fmtString, args), t);
        }
    }

    public void fatal(Throwable t) {
        delegate.fatal(t.getMessage(), t);
    }

    public void fatal(String message, Throwable t) {
        delegate.fatal(message, t);
    }

    // --- Error ---

    public void error(String message) {
        delegate.error(message);
    }

    public void error(String fmtString, Object... args) {
        if (delegate.isEnabledFor(Level.ERROR)) {
            delegate.error(formatMessage(fmtString, args));
        }
    }

    public void error(Throwable t, String fmtString, Object... args) {
        if (delegate.isEnabledFor(Level.ERROR)) {
            delegate.error(formatMessage(fmtString, args), t);
        }
    }

    public void error(Throwable t) {
        delegate.error(t.getMessage(), t);
    }

    public void error(String message, Throwable t) {
        delegate.error(message, t);
    }

    public void error(Throwable t, String message) {
        delegate.error(message, t);
    }

    // --- Warn ---

    public void warn(String message) {
        delegate.warn(message);
    }

    public void warn(String fmtString, Object... args) {
        if (delegate.isEnabledFor(Level.WARN)) {
            delegate.warn(formatMessage(fmtString, args));
        }
    }

    public void warn(Throwable t, String fmtString, Object... args) {
        if (delegate.isEnabledFor(Level.WARN)) {
            delegate.warn(formatMessage(fmtString, args), t);
        }
    }

    public void warn(Throwable t, String str) {
        delegate.warn(str, t);
    }

    public void warn(Throwable t) {
        delegate.warn(t.getMessage(), t);
    }

    public void warn(String message, Throwable t) {
        delegate.warn(message, t);
    }

    // --- Info ---

    public void info(String message) {
        delegate.info(message);
    }

    public void info(String fmtString, Object... args) {
        if (delegate.isEnabledFor(Level.INFO)) {
            delegate.info(formatMessage(fmtString, args));
        }
    }

    public void info(Throwable t, String fmtString, Object... args) {
        if (delegate.isEnabledFor(Level.INFO)) {
            delegate.info(formatMessage(fmtString, args), t);
        }
    }

    public void info(Throwable t, String str) {
        delegate.info(str, t);
    }

    public void info(Throwable t) {
        delegate.info(t.getMessage(), t);
    }

    public void info(String message, Throwable t) {
        delegate.info(message, t);
    }

    // --- Debug ---

    public void debug(String message) {
        delegate.debug(message);
    }

    public void debug(String fmtString, Object... args) {
        if (delegate.isDebugEnabled()) {
            delegate.debug(formatMessage(fmtString, args));
        }
    }

    public void debug(String message, Throwable t) {
        delegate.debug(message, t);
    }

    // --- Object-based overloads ---

    public void info(Object message) {
        delegate.info(message);
    }

    public void info(Object message, Throwable t) {
        delegate.info(message, t);
    }

    public void warn(Object message) {
        delegate.warn(message);
    }

    public void warn(Object message, Throwable t) {
        delegate.warn(message, t);
    }

    public void error(Object message) {
        delegate.error(message);
    }

    public void error(Object message, Throwable t) {
        delegate.error(message, t);
    }

    public void fatal(Object message) {
        delegate.fatal(message);
    }

    public void fatal(Object message, Throwable t) {
        delegate.fatal(message, t);
    }

    public void debug(Object message) {
        delegate.debug(message);
    }

    public void debug(Object message, Throwable t) {
        delegate.debug(message, t);
    }

    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return delegate.isEnabledFor(Level.INFO);
    }

    /**
     * Simple printf-style formatting. Uses %s for string substitution and %e for exception toString.
     */
    private static String formatMessage(String fmt, Object... args) {
        if (args == null || args.length == 0) {
            return fmt;
        }
        try {
            StringBuilder sb = new StringBuilder();
            int argIndex = 0;
            int i = 0;
            while (i < fmt.length()) {
                char c = fmt.charAt(i);
                if (c == '%' && i + 1 < fmt.length()) {
                    char next = fmt.charAt(i + 1);
                    if ((next == 's' || next == 'e') && argIndex < args.length) {
                        sb.append(args[argIndex++]);
                        i += 2;
                        continue;
                    }
                }
                sb.append(c);
                i++;
            }
            return sb.toString();
        } catch (Exception e) {
            return fmt;
        }
    }
}
