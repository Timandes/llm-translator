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

package cn.timandes.text;

import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.PoParser;

import java.io.FileReader;
import java.io.IOException;

public class PoMessageTextReader implements TextReader<Message> {
    private String filePath;

    public PoMessageTextReader(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void read(TextConsumer<Message> textConsumer) throws IOException {
        PoParser parser = new PoParser();
        Catalog potCatalog = parser.parseCatalog(new FileReader(filePath), true);

        for (Message message : potCatalog) {
            textConsumer.accept(message);
        }
    }
}
