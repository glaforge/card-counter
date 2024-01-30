@Grab("org.apache.commons:commons-text:1.11.0")
import org.apache.commons.text.similarity.LevenshteinDistance

@Grab("dev.langchain4j:langchain4j-vertex-ai-gemini:0.26.0-SNAPSHOT")
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.data.message.TextContent
import dev.langchain4j.data.message.ImageContent
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.output.Response

import java.io.File

import groovy.json.JsonSlurper

// helper record classes

record Sampleclear(File image, String base64, List<Integer> cardList) {
    @Override
    String toString() {
        return "Sample{image='$image', base64='$base64', cardList=$cardList"
    }
}

record Prompt(File file, String prompt) {
    @Override
    String toString() {
        return "Prompt{file='$file', prompt='$prompt'}"
    }
}

record Result(String response, List<Integer> cardList, Integer distance) {
    @Override
    String toString() {
        return "Result{response=$response}, cardList=$cardList, distance=$distance"
    }
}

// preparing the model

def model = VertexAiGeminiChatModel.builder()
.project("genai-java-demos")
.location("us-central1")
.modelName("gemini-pro-vision")
.maxRetries(3)
.build()

// loading the samples

def sampleFolder = new File("../samples")
def allSamples = sampleFolder.listFiles().collect { File img ->
    def allNumbers = img.name.findAll(/(-?\d+)/).collect { it.toInteger() }.sort()

    // def sum = allNumbers.sum()
    // def totalCards = allNumbers.size()
    // def cardGroups = allNumbers.groupBy { it }.collectEntries { [it.key, it.value.size()] }

    return new Sample(img, img.bytes.encodeBase64().toString(), allNumbers.sort())
}

// loading the templates

def allPrompts = new File("../prompts").listFiles().collect { File promptFile ->
    return new Prompt(promptFile, promptFile.text)
}

// testing the various samples with the different prompts

Map<Sample, Map<Prompt, Result>> results = [:].withDefault{ k -> [:] }

allSamples.each { sample ->
    println "========================================================"
    allPrompts.each { prompt ->
        Thread.start {
            def userMessage = UserMessage.from(
                ImageContent.from(sample.base64, "image/jpeg"),
                TextContent.from(prompt.prompt)
            )

            def response = stripMarkup(model.generate(userMessage).content().text())
            def cardList = new JsonSlurper().parseText(stripMarkup(response)).sort()

            results[sample][prompt] = new Result(
                response, cardList,
                LevenshteinDistance.defaultInstance.apply(toLettersString(cardList), toLettersString(sample.cardList))
            )

            println "Testing $sample.image.name with prompt $prompt.file.name"
            println "> Real cards: ${sample.cardList}"
            println ">   Response: ${cardList}"
            println "==> Distance: [ ${results[sample][prompt].distance} ]"
            println "--------------------------------------------------------"
        }

    }
}

static String stripMarkup(String text) {
    return text.replaceAll(/```/, '')
    .replaceAll(/json/, '')
    .trim()
}

static String toLettersString(List<Integer> l) {
    l.collect{ ('C' as char + it) as char }.join()
}

