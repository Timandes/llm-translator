package cn.timandes.chat;

import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatMessage;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import java.util.List;
import java.util.stream.Collectors;

public class OpenAiChatClient extends AbstractChatClient {
    public OpenAiChatClient(HttpMethod httpMethod, String url, HttpHeaders httpHeaders, RestOperations restOperations) {
        super(httpMethod, url, httpHeaders, restOperations);
    }

    @Override
    public ChatResponse chat(GenericChatRequest request) {
        HttpEntity<GenericChatRequest> requestHttpEntity = new HttpEntity<>(request, httpHeaders);
        ResponseEntity<ChatCompletions> responseEntity =
            restOperations.exchange(url, httpMethod,
                requestHttpEntity, ChatCompletions.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new GenericChatException();
        }

        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setChoices(buildChoices(responseEntity.getBody().getChoices()));
        return chatResponse;
    }

    static List<ChatResponse.Choice> buildChoices(List<ChatChoice> chatChoiceList) {
        return chatChoiceList.stream()
            .map(OpenAiChatClient::buildChoice)
            .collect(Collectors.toList());
    }

    static ChatResponse.Choice buildChoice(ChatChoice chatChoice) {
        ChatResponse.Choice retval = new ChatResponse.Choice();
        retval.setMessage(buildMessage(chatChoice.getMessage()));
        return retval;
    }

    static ChatResponse.Choice.Message buildMessage(ChatMessage message) {
        ChatResponse.Choice.Message retval = new ChatResponse.Choice.Message();
        retval.setRole(message.getRole().toString().toLowerCase());
        retval.setContent(message.getContent());
        return retval;
    }
}
