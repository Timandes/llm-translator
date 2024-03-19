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

package cn.timandes;

import cn.timandes.chat.ChatClient;
import cn.timandes.chat.OllamaChatClient;
import cn.timandes.text.PoMessageTextReader;
import cn.timandes.text.PoMessageTextWriter;
import cn.timandes.text.TextReader;
import cn.timandes.text.TextWriter;
import cn.timandes.translator.OllamaTranslator;
import cn.timandes.translator.filter.RemoveBackQuotesTranslatorFilter;
import cn.timandes.translator.filter.ReturnCharTranslatorFilter;
import cn.timandes.translator.filter.TranslationCachTranslatorFilter;
import cn.timandes.translator.filter.TranslatorFilter;
import com.alibaba.fastjson.JSON;
import org.apache.commons.cli.*;
import org.fedorahosted.tennera.jgettext.Message;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LlmTranslatorApplication {
    private static final String DEFAULT_USER_PROMPT_FORMAT = "问题：把 '''%s''' 翻译成中文，你的回答：";

    private static final String DEFAULT_MODEL = "qwen:14b";
    private static final String DEFAULT_LLM_SERVICE_ENDPOINT = "http://localhost:11434/api/chat";
    private static final String LLM_SERVICE_URL_OPT_NAME = "llm-service-url";
    private static final String TEXT_OPT_NAME = "text";
    private static final String PROMPT_FILE_OPT_NAME = "prompt-file";
    private static final String PO_FILE_OPT_NAME = "po-file";
    private static final String MODEL_OPT_NAME = "model";
    private static final String LLM_SERVICE_HTTP_HEADER_OPT_NAME = "llm-service-http-header";

    public static void main(String[] args) throws IOException {
        LlmTranslatorApplication application = new LlmTranslatorApplication();
        application.run(args);
    }

    public void run(String[] args) throws IOException {
        Options options = new Options();

        options.addOption(null, PROMPT_FILE_OPT_NAME, true, "Provide prompt from file");
        options.addOption(null, TEXT_OPT_NAME, true, "Provide text needed to be translated");
        options.addOption("f", PO_FILE_OPT_NAME, true, "Source PO file");
        options.addOption("m", MODEL_OPT_NAME, true, "Provide model name");
        options.addOption(null, LLM_SERVICE_URL_OPT_NAME, true, "Provide LLM Service endpoint");
        options.addOption(null, LLM_SERVICE_HTTP_HEADER_OPT_NAME, true, "Provide HTTP header for LLM Service");

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine;
        try {
            commandLine = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printUsage(options);

            System.exit(1);
            return;
        }

        // 基本对象
        String promptFile = commandLine.getOptionValue(PROMPT_FILE_OPT_NAME);
        SystemPrompt systemPrompt = commandLine.hasOption(PROMPT_FILE_OPT_NAME)
                ?SystemPrompt.loadFromFile(promptFile)
                :new SystemPrompt();
        System.out.println("System prompt: " + systemPrompt.getPromptString());

        HttpHeaders httpHeaders = new HttpHeaders();
        if (commandLine.hasOption(LLM_SERVICE_HTTP_HEADER_OPT_NAME)) {
            String[] httpHeaderLines = commandLine.getOptionValues(LLM_SERVICE_HTTP_HEADER_OPT_NAME);
            for (String line : httpHeaderLines) {
                System.out.println("Http header: " + line);
                String[] a = line.split(":");
                httpHeaders.add(a[0], a[1]);
            }
        }

        String llmServiceEndpoint = commandLine.getOptionValue(LLM_SERVICE_URL_OPT_NAME, DEFAULT_LLM_SERVICE_ENDPOINT);
        ChatClient chatClient = buildChatClient(HttpMethod.POST, llmServiceEndpoint, httpHeaders, new RestTemplate());
        System.out.println("LLM service endpoint: " + llmServiceEndpoint);

        String model = commandLine.getOptionValue(MODEL_OPT_NAME, DEFAULT_MODEL);
        System.out.println("Model: " + model);

        String userPromptFormat = DEFAULT_USER_PROMPT_FORMAT;
        OllamaTranslator translator = new OllamaTranslator(chatClient, model, systemPrompt.getPromptString(), userPromptFormat);

        // 不同用法
        if (commandLine.hasOption(TEXT_OPT_NAME)) {
            translateTextNow(commandLine, translator);
        } else if (commandLine.hasOption(PO_FILE_OPT_NAME)) {
            translatePoFile(commandLine, translator);
        } else {
            printUsage(options);
        }
    }

    protected ChatClient buildChatClient(HttpMethod httpMethod, String url, HttpHeaders httpHeaders, RestOperations restOperations) {
        return new OllamaChatClient(httpMethod, url, httpHeaders, restOperations);
    }

    private static void translateTextNow(CommandLine commandLine, OllamaTranslator translator) {
        String text = commandLine.getOptionValue(TEXT_OPT_NAME);
        System.out.println("Text: " + text);

        String tranlated = translator.translate(text);
        System.out.println("Translated: " + tranlated);
    }

    private static void translatePoFile(CommandLine commandLine, OllamaTranslator translator) throws IOException {

        String srcPoFilePath = commandLine.getOptionValue(PO_FILE_OPT_NAME);
        String destPoFilePath = StringUtils.trimTrailingCharacter(srcPoFilePath, 't');
        String cacheFilePath = srcPoFilePath + ".cache";

        TextReader<Message> textReader = new PoMessageTextReader(srcPoFilePath);
        TextWriter<Message> textWriter = new PoMessageTextWriter(destPoFilePath);
        TranslatePipeline pipeline = new TranslatePipeline(cacheFilePath, translator, textReader, textWriter);
        pipeline.go();
    }

    private static void printUsage(Options options) {
        String[] usageList = new String[] {
                "java -jar llm-translator.jar --text <text> [options]",
                "java -jar llm-translator.jar -f <path> [options]",
        };
        HelpFormatter helpFormatter = new HelpFormatter();
        String delimiter = "\n    ";
        helpFormatter.printHelp(delimiter + Arrays.stream(usageList).collect(Collectors.joining(delimiter)) + "\n\noptions:", options);
    }

    static class SystemPrompt {
        private static final String DEFAULT_SYSTEM_PROMPT = "你是一个翻译官";

        private String promptString = null;

        public SystemPrompt() {
            this(DEFAULT_SYSTEM_PROMPT);
        }

        public SystemPrompt(String promptString) {
            this.promptString = promptString;
        }

        public String getPromptString() {
            return Optional.ofNullable(promptString)
                    .orElse(DEFAULT_SYSTEM_PROMPT);
        }

        public static SystemPrompt loadFromFile(String path) throws IOException {
            byte[] buffer = Files.readAllBytes(Paths.get(path));
            String prompt = new String(buffer, StandardCharsets.UTF_8);
            return new SystemPrompt(prompt);
        }
    }

    static class TranslatePipeline {
        private TextReader<Message> textReader;
        private TextWriter<Message> textWriter;

        private List<TranslatorFilter> filterList = new ArrayList<>(4);

        private cn.timandes.Translator translator;

        TranslatePipeline(String cacheFilePath, cn.timandes.Translator translator, TextReader<Message> textReader, TextWriter<Message> textWriter) {
            this.translator = translator;
            this.textReader = textReader;
            this.textWriter = textWriter;

            filterList.add(new TranslationCachTranslatorFilter(new TranslationCacheManager(cacheFilePath)));
            filterList.add(new RemoveBackQuotesTranslatorFilter());
            filterList.add(new ReturnCharTranslatorFilter());
        }

        public void go() throws IOException{
            textReader.read(this::translateText);
            if (textWriter instanceof Closeable) {
                ((Closeable) textWriter).close();
            }
        }

        private void translateText(Message message) throws IOException {
            Message translatedMessage = cloneMessage(message);
            if (!translatedMessage.isHeader()) {
                String s = message.getMsgstr();
                for (TranslatorFilter filter: filterList) {
                    s = filter.preTranslate(s);
                }

                // 没变化，就直接开始翻译
                if (message.getMsgstr().equals(s)) {
                    s = translator.translate(s);
                }

                for (TranslatorFilter filter: filterList) {
                    s = filter.postTranslate(message.getMsgstr(), s);
                }
                translatedMessage.setMsgstr(s);
            }

            textWriter.write(translatedMessage);
        }

        private Message cloneMessage(Message message) {
            String json = JSON.toJSONString(message);
            return JSON.parseObject(json, Message.class);
        }
    }
}
