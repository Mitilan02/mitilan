package com.hcb.HCB;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import java.util.*;

public class DiseasePredictor {
    private final MongoCollection<Document> diseaseCollection;

    public DiseasePredictor(MongoCollection<Document> diseaseCollection) {
        this.diseaseCollection = diseaseCollection;
    }

    public void predictDiseases(List<String> symptoms) {
        Map<String, Integer> diseaseCounts = new HashMap<>();

        // Count occurrences of each disease based on symptoms
        for (String symptom : symptoms) {
            FindIterable<Document> cursor = diseaseCollection.find(Filters.eq("symptoms", symptom))
                    .projection(Projections.include("disease"));

            for (Document document : cursor) {
                String diseaseName = document.getString("disease");
                diseaseCounts.put(diseaseName, diseaseCounts.getOrDefault(diseaseName, 0) + 1);
            }
        }

        // Calculate possibility values
        List<Map.Entry<String, Integer>> sortedDiseases = new ArrayList<>(diseaseCounts.entrySet());
        sortedDiseases.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        int totalSymptoms = symptoms.size();
        for (Map.Entry<String, Integer> entry : sortedDiseases) {
            String disease = entry.getKey();
            int count = entry.getValue();
            double possibility = (double) count / totalSymptoms * 100;
            System.out.printf("You may be affected by %s. Possibility value is %.2f%%\n", disease, possibility);
        }
    }

    public static void main(String[] args) {
        // Connect to MongoDB
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("disease");
        MongoCollection<Document> diseaseCollection = database.getCollection("Dataset");

        // Initialize DiseasePredictor
        DiseasePredictor diseasePredictor = new DiseasePredictor(diseaseCollection);

        // Simulate user input symptoms
        List<String> inputSymptoms = Arrays.asList();

        // Predict diseases based on symptoms
        diseasePredictor.predictDiseases(inputSymptoms);

        // Close MongoDB connection
        mongoClient.close();
    }
}

