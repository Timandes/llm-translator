/*
   Copyright 2024 Timandes White (https://timandes.cn)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package cn.timandes;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TranslationCacheManager {
    private Map<String, String> cache = new HashMap<>(1024);

    private File file;
    private String path;

    public TranslationCacheManager(String path) {
        this.path = path;
        this.file = new File(path);
    }

    public void add(String original, String translated) {
        cache.put(original, translated);
    }

    public boolean contains(String original) {
        return cache.containsKey(original);
    }

    public String get(String original) {
        return cache.get(original);
    }

    public void save() throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        Writer writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        String json = JSON.toJSONString(cache);
        writer.write(json);
        writer.close();
    }

    public void tryToLoad() throws IOException {
        if (!file.exists()) {
            return;
        }

        byte[] buffer = Files.readAllBytes(Paths.get(path));
        String json = new String(buffer, StandardCharsets.UTF_8);
        Map<String, String> o = JSON.parseObject(json, Map.class);
        if (o != null) {
            cache = o;
        }
    }
}
