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
import org.fedorahosted.tennera.jgettext.PoWriter;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;

public class PoMessageTextWriter implements TextWriter<Message>, Closeable {
    private String filePath;
    private Catalog poCatalog = new Catalog();

    public PoMessageTextWriter(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void write(Message message) throws IOException {
        // Remove fuzzy
        message.setFuzzy(false);

        poCatalog.addMessage(message);
    }

    @Override
    public void close() throws IOException {
        FileWriter writer = new FileWriter(filePath);
        PoWriter poWriter = new PoWriter();
        poWriter.write(poCatalog, writer);
    }
}
