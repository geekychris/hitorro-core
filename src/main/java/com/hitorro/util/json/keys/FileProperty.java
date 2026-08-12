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
package com.hitorro.util.json.keys;


import com.hitorro.util.core.EnvCore;
import com.hitorro.util.core.params.GlobalProperties;
import com.hitorro.util.core.string.StringUtilCore;
import com.hitorro.util.json.keys.mappers.JsonNodeToFile;
import com.hitorro.util.json.keys.propaccess.Propaccess;

import java.io.File;


public class FileProperty extends BaseMappingProperty<File> {
    public FileProperty(String path, String description, String defaultValue) {
        super(new Propaccess(path), description,
                getDefaultFile(defaultValue),
                JsonNodeToFile.instance);
        this.rawDefault = defaultValue;
    }

    public FileProperty(String path, String description) {
        super(new Propaccess(path), description, null, JsonNodeToFile.instance);
    }

    public FileProperty(String path, String description, File defaultValue) {
        super(new Propaccess(path), description, defaultValue, JsonNodeToFile.instance);
    }

    public FileProperty(Propaccess path, String description, File defaultValue) {
        super(path, description, defaultValue, JsonNodeToFile.instance);
    }

    public FileProperty(Propaccess path, String description) {
        super(path, description, null, JsonNodeToFile.instance);
    }

    private String rawDefault;

    private static File getDefaultFile(String defaultValue) {
        String resolved = GlobalProperties.resolveJsonVariable(defaultValue);
        if (StringUtilCore.nullOrEmptyString(resolved)) {
            return null;
        }
        return new File(resolved);
    }

    @Override
    public File apply() {
        File result = super.apply();
        // If the result is the default and the raw default had variables, re-resolve lazily.
        // This handles the case where GlobalProperties wasn't populated at class-load time
        // (e.g., Spring Boot where JVSProperties is set but GlobalProperties is empty).
        if (result != null && result.equals(defaultValue) && rawDefault != null
                && rawDefault.contains("${")) {
            String resolved = resolveWithEnv(rawDefault);
            if (!StringUtilCore.nullOrEmptyString(resolved) && !resolved.contains("${")) {
                return new File(resolved);
            }
        }
        return result;
    }

    /**
     * Resolve Hitorro path variables using Env directly.
     * GlobalProperties.resolveJsonVariable silently drops unresolvable variables,
     * so we use Env as the canonical source for path variables.
     */
    private String resolveWithEnv(String value) {
        String bin = EnvCore.getBin().getAbsolutePath();
        String home = EnvCore.getHome().getAbsolutePath();
        String data = EnvCore.getData().getAbsolutePath();
        return value.replace("${HT_BIN}", bin)
                    .replace("${HT_HOME}", home)
                    .replace("${HT_DATA}", data)
                    .replace("${ht_bin}", bin)
                    .replace("${ht_home}", home)
                    .replace("${ht_data}", data);
    }
}
