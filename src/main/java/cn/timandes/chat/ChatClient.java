package cn.timandes.chat;

import java.util.function.Consumer;

public interface ChatClient {
    ChatResponse chat(GenericChatRequest request);

    void chat(GenericChatRequest request, Consumer<ChatResponse> responseConsumer);
}
