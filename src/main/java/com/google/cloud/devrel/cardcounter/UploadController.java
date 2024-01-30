package com.google.cloud.devrel.cardcounter;

import java.io.File;
import java.io.IOException;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.server.cors.CrossOrigin;

import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;

import java.util.Base64;
import java.time.Instant;
import java.time.Duration;

import static io.micronaut.http.MediaType.MULTIPART_FORM_DATA;
import static io.micronaut.http.MediaType.TEXT_PLAIN;
import static io.micronaut.http.MediaType.APPLICATION_JSON;

@Controller("/upload")
public class UploadController {

    @Value("${gcp.project}")
    private String project;

    @Value("${gcp.location}")
    private String location;

    @Value("${gcp.llm.model}")
    private String modelName;

    @Post(consumes = MULTIPART_FORM_DATA, produces = APPLICATION_JSON)
    public String upload(CompletedFileUpload picture) throws IOException {
        System.out.println("Received file: " + picture.getFilename());

        byte[] pictureBytes = picture.getBytes();

        ChatLanguageModel model = VertexAiGeminiChatModel.builder()
                .project(project)
                .location(location)
                .modelName(modelName)
                .maxRetries(3)
                .build();

        String base64Data = Base64.getEncoder().encodeToString(pictureBytes);
        UserMessage userMessage = UserMessage.from(
                ImageContent.from(base64Data, picture.getContentType().get().toString()),
                TextContent.from("""
                    Return a JSON array with all the big numbers found on the playing cards on the picture.

                    JSON:
                """)
        );

        // Generate a response from the model
        System.out.println("Generating response...");
        Instant start = Instant.now();

        Response<AiMessage> response = model.generate(userMessage);

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Response in " + timeElapsed + "ms \n" + response);

        // Return the response
        return stripMarkup(response.content().text());
    }

    private static String stripMarkup(String text) {
        return text.replaceAll("```", "")
        .replaceAll("json", "")
        .trim();
    }
}