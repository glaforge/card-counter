# Counting playing card with Gemini Pro Vision

As I've been playing to the [Skyjo](https://www.philibertnet.com/fr/magilano/80922-skyjo-4260470080018.html)
playing card game with my daughter, I thought it would be fun to see if
[Gemini Pro Vision](https://blog.google/technology/ai/gemini-api-developers-cloud/)
multimodal large language model would be able to count the points on the cards.

The game consists of cards with numbers ranging from -2 up to 12.
The winner is the person with the lowest number of points (ie. the total of all the numbers on the cards).

## Trying it!



## Code

This repository contains the code of the demo application.
It's a [Micronaut](https://micronaut.io) application developed in Java.
Interactions with the Gemini model are handled with the [LangChain4j](https://github.com/langchain4j/) library.
