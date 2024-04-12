package com.hcb.HCB;

import static spark.Spark.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class App {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private Gson gson;

    public App() {
        // Connect to MongoDB
        this.mongoClient = new MongoClient("localhost", 27017);
        this.database = mongoClient.getDatabase("disease");
        this.collection = database.getCollection("Dataset");
        this.gson = new Gson();

        // Set up SparkJava HTTP service
        port(8080); // Set desired port

        // Define HTML content for the frontend interface
        String htmlContent = "<!DOCTYPE html>\n" +
                             "<html>\n" +
                             "<head>\n" +
                             "    <title>Healthcare Chatbot</title>\n" +
                             "    <style>\n" +
                             "        /* Chat box styling */\n" +
                             "        #chat-container {\n" +
                             "            width: 400px;\n" +
                             "            height: 400px;\n" +
                             "            border: 1px solid #ccc;\n" +
                             "            overflow-y: scroll;\n" +
                             "            padding: 10px;\n" +
                             "        }\n" +
                             "        .user-message {\n" +
                             "            text-align: right;\n" +
                             "            color: blue;\n" +
                             "        }\n" +
                             "        .bot-message {\n" +
                             "            text-align: left;\n" +
                             "            color: green;\n" +
                             "        }\n" +
                             "        .suggestion {\n" +
                             "            cursor: pointer;\n" +
                             "            padding: 5px;\n" +
                             "        }\n" +
                             "        .suggestion:hover {\n" +
                             "            background-color: #f0f0f0;\n" +
                             "        }\n" +
                             "    </style>\n" +
                             "</head>\n" +
                             "<body>\n" +
                             "    <h1>Healthcare Chatbot</h1>\n" +
                             "    <div id=\"chat-container\"></div>\n" +
                             "    <form id=\"chatbotForm\">\n" +
                             "        <input type=\"text\" id=\"Symptoms\" name=\"Symptoms\" placeholder=\"Enter Symptoms\" required autocomplete=\"off\">\n" +
                             "        <div id=\"suggestions\"></div>\n" + // Suggestions container
                             "        <button type=\"submit\">Send</button>\n" +
                             "    </form>\n" +
                             "\n" +
                             "    <script>\n" +
                             "        const SymptomsInput = document.getElementById('Symptoms');\n" +
                             "        const suggestionsContainer = document.getElementById('suggestions');\n" +
                             "\n" +
                             "       SymptomsInput.addEventListener('input', function() {\r\n"
                             + "    const inputText = this.value.trim();\r\n"
                             + "    const Symptoms = inputText.split(', ').map(symptom => symptom.trim()); // Split by \", \" and trim each part\r\n"
                             + "\r\n"
                             + "    if (Symptoms.length > 0) {\r\n"
                             + "        fetch(`/suggestSymptoms?query=${encodeURIComponent(Symptoms[Symptoms.length - 1])}`) // Fetch suggestions for the last entered symptom\r\n"
                             + "        .then(response => response.json())\r\n"
                             + "        .then(data => {\r\n"
                             + "            showSuggestions(data);\r\n"
                             + "        })\r\n"
                             + "        .catch(error => {\r\n"
                             + "            console.error('Error fetching suggestions:', error);\r\n"
                             + "        });\r\n"
                             + "    } else {\r\n"
                             + "        suggestionsContainer.innerHTML = ''; // Clear suggestions\r\n"
                             + "    }\r\n"
                             + "});\r\n"
                             + 
                             "function showSuggestions(suggestions) {\r\n"
                             + "    suggestionsContainer.innerHTML = ''; // Clear previous suggestions\r\n"
                             + "    suggestions.forEach(symptom => {\r\n"
                             + "        const suggestionElem = document.createElement('div');\r\n"
                             + "        suggestionElem.textContent = symptom;\r\n"
                             + "        suggestionElem.classList.add('suggestion');\r\n"
                             + "        suggestionElem.addEventListener('click', function() {\r\n"
                             + "            let currentText = SymptomsInput.value.trim();\r\n"
                             + "\r\n"
                             + "            // Find the index of the last occurrence of \", \"\r\n"
                             + "            const lastIndex = currentText.lastIndexOf(', ');\r\n"
                             + "\r\n"
                             + "            // Replace text after the last \", \" with the selected suggestion\r\n"
                             + "            const newText = lastIndex !== -1 ? currentText.substring(0, lastIndex + 2) + symptom : symptom;\r\n"
                             + "\r\n"
                             + "            SymptomsInput.value = newText; // Update input with the new text\r\n"
                             + "            suggestionsContainer.innerHTML = ''; // Clear suggestions\r\n"
                             + "        });\r\n"
                             + "        suggestionsContainer.appendChild(suggestionElem);\r\n"
                             + "    });\r\n"
                             + "}\r\n"
                             + 
                             "        document.getElementById('chatbotForm').addEventListener('submit', function(event) {\n" +
                             "            event.preventDefault();\n" +
                             "            var Symptoms = document.getElementById('Symptoms').value;\n" +
                             "            appendMessage('user', Symptoms); // Show user's input\n" +
                             "            predictDisease(Symptoms); // Get response from server\n" +
                             "            document.getElementById('Symptoms').value = ''; // Clear input field\n" +
                             "        });\n" +
                             "\n" +
                             "        function predictDisease(Symptoms) {\n" +
                             "            fetch('/predictDisease?Symptoms=' + encodeURIComponent(Symptoms))\n" +
                             "            .then(response => response.text())\n" +
                             "            .then(data => {\n" +
                             "                appendMessage('bot', \"Predicted Disease: \" + data);\n" +
                             "            })\n" +
                             "            .catch(error => {\n" +
                             "                console.error('Error:', error);\n" +
                             "                appendMessage('bot', \"Error occurred while predicting disease.\");\n" +
                             "            });\n" +
                             "        }\n" +
                             "\n" +
                             "        function appendMessage(sender, message) {\n" +
                             "            var chatContainer = document.getElementById('chat-container');\n" +
                             "            var messageElem = document.createElement('div');\n" +
                             "            messageElem.className = (sender === 'user') ? 'user-message' : 'bot-message';\n" +
                             "            messageElem.textContent = message;\n" +
                             "            chatContainer.appendChild(messageElem);\n" +
                             "            // Scroll to bottom of chat container\n" +
                             "            chatContainer.scrollTop = chatContainer.scrollHeight;\n" +
                             "        }\n" +
                             "    </script>\n" +
                             "</body>\n" +
                             "</html>";

        // Serve the HTML content as the response to root URL
        get("/", (req, res) -> htmlContent);

        // Handle symptom suggestion requests
        get("/suggestSymptoms", (req, res) -> {
            String query = req.queryParams("query");
            List<String> suggestedSymptoms = suggestSymptoms(query);
            return gson.toJson(suggestedSymptoms); // Return JSON response
        });

        // Handle disease prediction request
        get("/predictDisease", (req, res) -> {
            String SymptomsParam = req.queryParams("Symptoms");
            List<String> Symptoms = Arrays.asList(SymptomsParam.split(", "));
            String predictedDiseases = predictDisease(Symptoms);
            res.type("application/json");
            return predictedDiseases;
        });
    }

    private List<String> suggestSymptoms(String query) {
        // Filter Symptoms based on query
        List<String> suggestedSymptoms = getAllSymptoms().stream()
                .filter(symptom -> symptom.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        return suggestedSymptoms;
    }


    private List<String> getAllSymptoms() {
        // Simulate example: return a list of all Symptoms
        return Arrays.asList("itching", "skin_rash", "nodal_skin_eruptions", "continuous_sneezing", "shivering",
                "chills", "joint_pain", "stomach_pain", "acidity", "ulcers_on_tongue", "muscle_wasting",
                "vomiting", "burning_micturition", "spotting_urination", "fatigue", "weight_gain", "anxiety",
                "cold_hands_and_feets", "mood_swings", "weight_loss", "restlessness", "lethargy", "patches_in_throat",
                "irregular_sugar_level", "cough", "high_fever", "sunken_eyes", "breathlessness", "sweating", "dehydration",
                "indigestion", "headache", "yellowish_skin", "dark_urine", "nausea", "loss_of_appetite", "pain_behind_the_eyes",
                "back_pain", "constipation", "abdominal_pain", "diarrhoea", "mild_fever", "yellow_urine", "yellowing_of_eyes",
                "acute_liver_failure", "fluid_overload", "swelling_of_stomach", "swelled_lymph_nodes", "malaise",
                "blurred_and_distorted_vision", "phlegm", "throat_irritation", "redness_of_eyes", "sinus_pressure",
                "runny_nose", "congestion", "chest_pain", "weakness_in_limbs", "fast_heart_rate", "pain_during_bowel_movements",
                "pain_in_anal_region", "bloody_stool", "irritation_in_anus", "neck_pain", "dizziness", "cramps", "bruising",
                "obesity", "swollen_legs", "swollen_blood_vessels", "puffy_face_and_eyes", "enlarged_thyroid", "brittle_nails",
                "swollen_extremeties", "excessive_hunger", "extra_marital_contacts", "drying_and_tingling_lips", "slurred_speech",
                "knee_pain", "hip_joint_pain", "muscle_weakness", "stiff_neck", "swelling_joints", "movement_stiffness",
                "spinning_movements", "loss_of_balance", "unsteadiness", "weakness_of_one_body_side", "loss_of_smell",
                "bladder_discomfort", "foul_smell_of_urine", "continuous_feel_of_urine", "passage_of_gases",
                "internal_itching", "toxic_look_(typhos)", "depression", "irritability", "muscle_pain", "altered_sensorium",
                "red_spots_over_body", "belly_pain", "abnormal_menstruation", "dischromic_patches", "watering_from_eyes",
                "increased_appetite", "polyuria", "family_history", "mucoid_sputum", "rusty_sputum", "lack_of_concentration",
                "visual_disturbances", "receiving_blood_transfusion", "receiving_unsterile_injections", "coma", "stomach_bleeding",
                "distention_of_abdomen", "history_of_alcohol_consumption", "blood_in_sputum", "prominent_veins_on_calf",
                "palpitations", "painful_walking", "pus_filled_pimples", "blackheads", "scurring", "skin_peeling",
                "silver_like_dusting", "small_dents_in_nails", "inflammatory_nails", "blister", "red_sore_around_nose",
                "yellow_crust_ooze");
    }

    private String predictDisease(List<String> Symptoms) {
        // Query MongoDB for diseases matching provided Symptoms
        List<Document> matchingDiseases = findDiseasesBySymptoms(Symptoms);

        if (matchingDiseases.isEmpty()) {
            return "Disease not found";
        }

        // Calculate total number of matched Symptoms
        int totalMatchedSymptoms = Symptoms.size();

        // Map to store cumulative match percentage for each disease
        Map<String, Double> diseaseProbabilities = new HashMap<>();

        // Calculate cumulative probability for each disease
        for (Document diseaseDoc : matchingDiseases) {
            String diseaseName = diseaseDoc.getString("Disease");
            List<String> matchedSymptoms = (List<String>) diseaseDoc.get("Symptoms");
            double matchPercentage = (double) matchedSymptoms.size() / totalMatchedSymptoms * 100;

            if (diseaseProbabilities.containsKey(diseaseName)) {
                // Add to existing cumulative probability
                double currentProbability = diseaseProbabilities.get(diseaseName);
                diseaseProbabilities.put(diseaseName, currentProbability + matchPercentage);
            } else {
                // Initialize cumulative probability for new disease
                diseaseProbabilities.put(diseaseName, matchPercentage);
            }
        }

        // Normalize probabilities to ensure the total sums up to 100%
        double totalProbability = diseaseProbabilities.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalProbability > 0) {
            for (Map.Entry<String, Double> entry : diseaseProbabilities.entrySet()) {
                double normalizedPercentage = (entry.getValue() / totalProbability) * 100;
                entry.setValue(normalizedPercentage);
            }
        }

        // Prepare output string with unique diseases and normalized probabilities
        StringBuilder resultBuilder = new StringBuilder();
        Iterator<Map.Entry<String, Double>> iterator = diseaseProbabilities.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Double> entry = iterator.next();
            String diseaseName = entry.getKey();
            double normalizedPercentage = entry.getValue();

            resultBuilder.append(" You may be affected by ").append(diseaseName)
                         .append(". Possibility value is ").append(String.format("%.2f", normalizedPercentage))
                         .append("% .");

            if (iterator.hasNext()) {
                resultBuilder.append("\n");
            }
        }

        return resultBuilder.toString();
    }



    private List<Document> findDiseasesBySymptoms(List<String> Symptoms) {
        // MongoDB query to find diseases matching provided Symptoms
        Document query = new Document("Symptoms", new Document("$all", Symptoms));
        return collection.find(query).into(new ArrayList<>());
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
