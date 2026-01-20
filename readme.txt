AI Ticket Classifier – Spring Boot + Spring AI

This project implements a support ticket classifier using Spring Boot and Spring AI ChatClient.
It classifies a ticket into one of the following categories using an LLM:
1. BILLING
2. TECHNICAL
3. GENERAL

The service supports OpenAI API.
OpenAI (cloud)

Prerequisites
-Java 17
-Maven 3.9+
-An OpenAI API key


Project Structure:
src/main/java/com/vupico/ticket
 ├── api        → REST controllers 
 ├── service    → LLM classification logic
 ├── domain     → Request / response models
 ├── exceptions → Global exception handler

Running the application
-Unzip the file
-navigate to the root folder 
-replace ${OPENAI_API_KEY} with your API key in project_root/docker-compose.yml
if you don't have docker, replace ${OPENAI_API_KEY} in application.yml

if you have docker [easiest and production grade from local machine, I have removed the dependency of doing mvn install and target folder in repo, when we run docker compose command it will build the project nad run it.]:
-ensure docker is running in your system
-run command : docker compose up -d

If you don't have docker [Easiest common way is from local machine]:
-open the project into any javaee IDE
-find the TicketCLassifierApplication class and run/debug.

If you want to run by command line [mvn needs to be setup in you machine]:
-run command: mvn install
-run command: mvn run


run below curl command or just import in postman (Change the 'text' field as per your choice):

curl -X POST http://localhost:8080/classify \
  -H "Content-Type: application/json" \
  -d '{"text":"Customer charged twice on credit card"}'

