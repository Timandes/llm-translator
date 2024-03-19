package cn.timandes.chat;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestOperations;

import java.util.function.Consumer;

abstract public class AbstractChatClient implements ChatClient {
    protected HttpMethod httpMethod;
    protected String url;
    protected HttpHeaders httpHeaders;
    protected RestOperations restOperations;

    public AbstractChatClient(HttpMethod httpMethod, String url, HttpHeaders httpHeaders, RestOperations restOperations) {
        this.httpMethod = httpMethod;
        this.url = url;
        this.httpHeaders = httpHeaders;
        this.restOperations = restOperations;
    }

    @Override
    public void chat(GenericChatRequest request, Consumer<ChatResponse> responseConsumer) {
        throw new UnsupportedOperationException();
    }

}
