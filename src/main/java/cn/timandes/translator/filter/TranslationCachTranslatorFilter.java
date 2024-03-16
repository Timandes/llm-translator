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

package cn.timandes.translator.filter;

import cn.timandes.TranslationCacheManager;

import java.io.IOException;

public class TranslationCachTranslatorFilter implements TranslatorFilter {
    private TranslationCacheManager translationCacheManager;

    public TranslationCachTranslatorFilter(TranslationCacheManager translationCacheManager) {
        this.translationCacheManager = translationCacheManager;
    }

    @Override
    public String preTranslate(String s) {
        if (translationCacheManager.contains(s)) {
            return translationCacheManager.get(s);
        } else {
            return s;
        }
    }

    @Override
    public String postTranslate(String original, String translation) throws IOException {
        translationCacheManager.add(original, translation);
        translationCacheManager.save();
        return translation;
    }
}
