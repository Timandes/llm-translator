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

import cn.timandes.Translator;
import cn.timandes.chat.ChatClient;
import cn.timandes.chat.ChatResponse;
import cn.timandes.chat.GenericChatRequest;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatRole;

import java.util.ArrayList;
import java.util.List;

public class OllamaTranslator implements Translator {
    private ChatClient chatClient;
    private String model;
    private String systemPrompt;
    private String userPromptFormat;

    public OllamaTranslator(ChatClient chatClient, String model, String systemPrompt, String userPromptFormat) {
        this.chatClient = chatClient;
        this.model = model;
        this.systemPrompt = systemPrompt;
        this.userPromptFormat = userPromptFormat;
    }

    @Override
    public String translate(String text) {
        List<ChatMessage> chatMessageList = new ArrayList<>();
        chatMessageList.add(new ChatMessage(ChatRole.SYSTEM, systemPrompt));
        chatMessageList.add(new ChatMessage(ChatRole.USER, buildUserPrompt(text)));

        GenericChatRequest request = new GenericChatRequest();
        request.setMessages(chatMessageList);
        request.setModel(model);
        request.setStream(false);

        ChatResponse response = chatClient.chat(request);
        return response.getChoices().get(0).getMessage().getContent();
    }

    private String buildUserPrompt(String s) {
        return String.format(userPromptFormat, s);
    }
}
