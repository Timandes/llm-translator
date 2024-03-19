package cn.timandes.chat;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import java.util.ArrayList;

public class OllamaChatClient extends AbstractChatClient {
    public OllamaChatClient(HttpMethod httpMethod, String url, HttpHeaders httpHeaders, RestOperations restOperations) {
        super(httpMethod, url, httpHeaders, restOperations);
    }

    @Override
    public ChatResponse chat(GenericChatRequest request) {
        HttpEntity<GenericChatRequest> requestHttpEntity = new HttpEntity<>(request, httpHeaders);
        ResponseEntity<OllamaChatResponse> responseEntity =
            restOperations.exchange(url, httpMethod,
                requestHttpEntity, OllamaChatResponse.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new GenericChatException();
        }

        ChatResponse.Choice choice = new ChatResponse.Choice();
        choice.setMessage(responseEntity.getBody().getMessage());
        ArrayList<ChatResponse.Choice> choices = new ArrayList<>();
        choices.add(choice);
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setChoices(choices);
        return chatResponse;
    }

    /**
     * @see <a href="https://github.com/ollama/ollama/blob/main/docs/api.md#generate-a-chat-completion">Ollama API</a>
     */
    static class OllamaChatResponse {
        private ChatResponse.Choice.Message message;

        public ChatResponse.Choice.Message getMessage() {
            return message;
        }

        public void setMessage(ChatResponse.Choice.Message message) {
            this.message = message;
        }
    }
}
