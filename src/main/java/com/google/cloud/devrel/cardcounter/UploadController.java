/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.devrel.cardcounter;

import java.io.File;
import java.io.IOException;

import dev.langchain4j.data.message.*;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.multipart.CompletedFileUpload;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.request.ResponseFormat;

import java.util.Base64;
import java.time.Instant;
import java.time.Duration;
import java.util.List;

import static io.micronaut.http.MediaType.MULTIPART_FORM_DATA;
import static io.micronaut.http.MediaType.APPLICATION_JSON;

@Controller("/upload")
public class UploadController {

    @Value("${gcp.llm.apiKey:}")
    private String apiKey;

    @Value("${gcp.llm.model}")
    private String modelName;

    @Post(consumes = MULTIPART_FORM_DATA, produces = APPLICATION_JSON)
    public String upload(CompletedFileUpload picture) throws IOException {
        System.out.println("Received file: " + picture.getFilename());

        byte[] pictureBytes = picture.getBytes();

        ChatModel model = GoogleGenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName(modelName)
            .maxRetries(3)
            .responseFormat(ResponseFormat.JSON)
            .build();

        SystemMessage systemMessage = SystemMessage.from("""
            Detect playing cards with numbers, with no more than 12 cards.
            Output a JSON list of integers, where each value is the big number displayed in the center of each face-up card.
            Ignore the small numbers in the corners of the cards.
            CRITICAL: Ignore face-down cards completely. Face-down cards have the colorful word "SKYJO" or "SKYJO ACTION" written on them. Do NOT mistake the letter "O" in the word "SKYJO" for the number 0. If a card has the word "SKYJO" printed across the back, skip it and do not output any number for it.
            Only count cards that clearly show a valid Skyjo number (-1 to 12).
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

        ChatResponse response = model.chat(
            List.of(systemMessage, userMessage)
        );

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Response in " + timeElapsed + "ms \n" + response.aiMessage().text());

        // Return the response
        return response.aiMessage().text();
    }
}