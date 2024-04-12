package com.hcb.HCB;

import static spark.Spark.*;

import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class App {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    public App() {
        // Connect to MongoDB
        this.mongoClient = new MongoClient("localhost", 27017);
        this.database = mongoClient.getDatabase("disease");
        this.collection = database.getCollection("Dataset");

        // Set up SparkJava HTTP service
        port(8080); // Set desired port

        // Define HTML content for the frontend interface
        String htmlContent = "<!DOCTYPE html>\r\n"
        		+ "<html>\r\n"
        		+ "<head>\r\n"
        		+ "    <title>Healthcare Chatbot</title>\r\n"
        		+ "    <style>\r\n"
        		+ "        /* Chat box styling */\r\n"
        		+ "        #chat-container {\r\n"
        		+ "            width: 400px;\r\n"
        		+ "            height: 400px;\r\n"
        		+ "            border: 1px solid #ccc;\r\n"
        		+ "            overflow-y: scroll;\r\n"
        		+ "            padding: 10px;\r\n"
        		+ "        }\r\n"
        		+ "        .user-message {\r\n"
        		+ "            text-align: right;\r\n"
        		+ "            color: blue;\r\n"
        		+ "        }\r\n"
        		+ "        .bot-message {\r\n"
        		+ "            text-align: left;\r\n"
        		+ "            color: green;\r\n"
        		+ "        }\r\n"
        		+ "    </style>\r\n"
        		+ "</head>\r\n"
        		+ "<body>\r\n"
        		+ "    <h1>Healthcare Chatbot</h1>\r\n"
        		+ "    <div id=\"chat-container\"></div>\r\n"
        		+ "    <form id=\"chatbotForm\">\r\n"
        		+ "        <input type=\"text\" id=\"symptoms\" name=\"symptoms\" placeholder=\"Enter Symptoms (comma-separated)\" required>\r\n"
        		+ "        <button type=\"submit\">Send</button>\r\n"
        		+ "    </form>\r\n"
        		+ "\r\n"
        		+ "    <script>\r\n"
        		+ "        document.getElementById('chatbotForm').addEventListener('submit', function(event) {\r\n"
        		+ "            event.preventDefault();\r\n"
        		+ "            var symptoms = document.getElementById('symptoms').value;\r\n"
        		+ "            appendMessage('user', symptoms); // Show user's input\r\n"
        		+ "            predictDisease(symptoms); // Get response from server\r\n"
        		+ "            document.getElementById('symptoms').value = ''; // Clear input field\r\n"
        		+ "        });\r\n"
        		+ "\r\n"
        		+ "        function predictDisease(symptoms) {\r\n"
        		+ "            fetch('/predictDisease?symptoms=' + encodeURIComponent(symptoms))\r\n"
        		+ "            .then(response => response.text())\r\n"
        		+ "            .then(data => {\r\n"
        		+ "                appendMessage('bot', \"Predicted Disease: \" + data);\r\n"
        		+ "            })\r\n"
        		+ "            .catch(error => {\r\n"
        		+ "                console.error('Error:', error);\r\n"
        		+ "                appendMessage('bot', \"Error occurred while predicting disease.\");\r\n"
        		+ "            });\r\n"
        		+ "        }\r\n"
        		+ "\r\n"
        		+ "        function appendMessage(sender, message) {\r\n"
        		+ "            var chatContainer = document.getElementById('chat-container');\r\n"
        		+ "            var messageElem = document.createElement('div');\r\n"
        		+ "            messageElem.className = (sender === 'user') ? 'user-message' : 'bot-message';\r\n"
        		+ "            messageElem.textContent = message;\r\n"
        		+ "            chatContainer.appendChild(messageElem);\r\n"
        		+ "            // Scroll to bottom of chat container\r\n"
        		+ "            chatContainer.scrollTop = chatContainer.scrollHeight;\r\n"
        		+ "        }\r\n"
        		+ "    </script>\r\n"
        		+ "</body>\r\n"
        		+ "</html>\r\n";
        		

        // Serve the HTML content as the response to root URL
        get("/", (req, res) -> {
            return htmlContent;
        });

        // Handle disease prediction request
        get("/predictDisease", (req, res) -> {
            String symptomsParam = req.queryParams("symptoms");
            List<String> symptoms = Arrays.asList(symptomsParam.split(","));
            String predictedDisease = predictDisease(symptoms);
            return predictedDisease;
        });
    }

    private String predictDisease(List<String> symptoms) {
        // Query MongoDB for disease prediction
        Document diseaseDoc = findDiseaseBySymptoms(symptoms);
        if (diseaseDoc != null) {
            return diseaseDoc.getString("disease");
        } else {
            return "Disease not found";
        }
    }

    private Document findDiseaseBySymptoms(List<String> symptoms) {
        // MongoDB query to find matching disease
        Document query = new Document("symptoms", new Document("$all", symptoms));
        return collection.find(query).first();
    }

    public void stopService() {
        // Close MongoDB connection
        this.mongoClient.close();
        // Stop SparkJava service
        stop();
    }

    public static void main(String[] args) {
        App chatbotService = new App();

        // Keep the service running
        awaitInitialization();
    }
}
