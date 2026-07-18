# Skyjo Card Counter with Gemini Flash

As I've been playing the [Skyjo](https://www.philibertnet.com/fr/magilano/80922-skyjo-4260470080018.html) playing card game with my daughter, I thought it would be fun to build an application to automatically count the points on the cards using AI.

This project utilizes Google's ultra-fast **Gemini Flash (`gemini-flash-latest`)** multimodal large language model to instantly detect and sum up the points on the playing cards from a simple photo.

The game consists of cards with numbers ranging from `-2` up to `12`. The winner is the person with the lowest number of points (i.e., the total of all the numbers on the revealed cards).

## Features

- **Instant Recognition:** Take a picture of your cards, and Gemini Flash instantly parses the face-up values while intelligently ignoring face-down cards or the "SKYJO" logo.
- **Beautiful Glassmorphism UI:** A sleek, mobile-friendly interface featuring fully responsive design, vibrant mesh gradients, smooth micro-animations, and a seamless auto-submit experience.
- **Modern Backend:** Built with **Java 25** and the **Micronaut 5.x** framework for blazing-fast startup and execution.
- **LangChain4j:** Integrates the official `langchain4j-google-genai` module to communicate with the Gemini API.
- **Containerized:** Fully Docker/Podman compatible and ready to be deployed to Google Cloud Run.

## Prerequisites

To run this application, you will need:
- Java 25 (if running natively via Gradle)
- Docker or Podman (if running via containers)
- A [Google Gemini API Key](https://aistudio.google.com/app/apikey)

Ensure you export your API key in your environment before running the application:
```bash
export GEMINI_API_KEY="your-api-key-here"
```

## Running it Locally

You can use the provided [just](https://github.com/casey/just) command runner, or use standard Gradle/Podman commands.

**Using Gradle directly:**
```bash
./gradlew run
```

**Using Podman (via Just):**
```bash
just podman-build
just podman-run
```
The application will be available at `http://localhost:8080`.

## Cloud Deployment

This repository is pre-configured for deployment to **Google Cloud Run** using Google Cloud Build. 

To deploy to Google Cloud, simply authenticate with the `gcloud` CLI and run:
```bash
just deploy
```
*Note: The deployment command automatically forwards your local `$GEMINI_API_KEY` directly to the Cloud Run service environment.*

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for more information.

## Disclaimer

This is not an official Google project.
