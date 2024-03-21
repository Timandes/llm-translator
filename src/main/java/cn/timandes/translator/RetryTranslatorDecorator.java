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

package cn.timandes.translator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryTranslatorDecorator implements Translator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryTranslatorDecorator.class);

    private Translator delegate;

    private int maxTries;

    public RetryTranslatorDecorator(Translator delegate, int maxTries) {
        this.delegate = delegate;
        this.maxTries = maxTries;
    }

    @Override
    public String translate(String text) {
        for (int i=0; i<maxTries; ++i) {
            try {
                return delegate.translate(text);
            } catch (TranslationFailedException e) {
                LOGGER.warn("Translation failed, retrying ...", e);
            }
        }

        throw new TranslationFailedException();
    }
}
