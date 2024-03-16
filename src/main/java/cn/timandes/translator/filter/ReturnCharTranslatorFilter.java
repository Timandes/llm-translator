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

import org.springframework.util.StringUtils;

import java.io.IOException;

public class ReturnCharTranslatorFilter implements TranslatorFilter {
    @Override
    public String preTranslate(String text) {
        return text;
    }

    @Override
    public String postTranslate(String original, String translation) throws IOException {
        String retval = addPrefixReturnChar(original, translation);
        retval = addSuffixReturnChar(original, retval);
        return retval;
    }

    private String addSuffixReturnChar(String original, String translated) {
        if (isLastCharReturn(original) && !isLastCharReturn(translated)) {
            return translated + "\n";
        } else if (!isLastCharReturn(original) && isLastCharReturn(translated)) {
            return StringUtils.trimTrailingWhitespace(translated);
        } else {
            return translated;
        }
    }

    private String addPrefixReturnChar(String original, String translated) {
        if (isFirstCharReturn(original) && !isFirstCharReturn(translated)) {
            return "\n" + translated;
        } else if (!isFirstCharReturn(original) && isFirstCharReturn(translated)) {
            return StringUtils.trimLeadingWhitespace(translated);
        } else {
            return translated;
        }
    }
    private boolean isFirstCharReturn(String s) {
        return '\n' == s.charAt(0);
    }

    private boolean isLastCharReturn(String s) {
        return '\n' == s.charAt(s.length() - 1);
    }

}
