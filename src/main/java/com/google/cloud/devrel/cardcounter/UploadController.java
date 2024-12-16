package com.google.cloud.devrel.cardcounter;

import java.io.File;
import java.io.IOException;

import dev.langchain4j.data.message.*;
import dev.langchain4j.model.vertexai.SchemaHelper;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.multipart.CompletedFileUpload;

import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;

import java.util.Base64;
import java.time.Instant;
import java.time.Duration;
import java.util.List;

import static io.micronaut.http.MediaType.MULTIPART_FORM_DATA;
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
            .responseSchema(SchemaHelper.fromClass(int[].class))
            .build();

        SystemMessage systemMessage = SystemMessage.from("""
            Detect playing cards with numbers, with no more than 12 cards.
            Output a JSON list of integers, where each value is the big number displayed in the center of each card.
            Ignore the small numbers in the corners of the cards.
            Ignore cards with text written on them. Don't include it in the response.
            If you see the text "SKYJO" on the card, use 0 as the value instead.
            Be careful when reading the numbers, as sometimes some cards are tilted, cut, or upside down.
            """);

        String base64Data = Base64.getEncoder().encodeToString(pictureBytes);
        UserMessage userMessage = UserMessage.from(
            ImageContent.from(base64Data, picture.getContentType().get().toString()),
            TextContent.from("""
                Return a JSON array with all the big numbers found on the playing cards on the picture.
                """)
        );

        // Generate a response from the model
        System.out.println("Generating response...");
        Instant start = Instant.now();

        Response<AiMessage> response = model.generate(
            List.of(systemMessage, userMessage)
        );

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Response in " + timeElapsed + "ms \n" + response.content().text());

        // Return the response
        return response.content().text();
    }
}