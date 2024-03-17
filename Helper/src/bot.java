import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class bot {

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        // Convert CSV to ARFF if ARFF doesn't exist
        File arffFile = new File("Data/trainingSet.arff");
        if (!arffFile.exists()) {
            convertCSVtoARFF("Data/trainingSet.csv", "Data/trainingSet.arff");
        }

        // Load the training dataset
        Instances trainingData = loadTrainingData("Data/trainingSet.arff");

        // Train a decision tree classifier
        Classifier classifier = new J48();
        classifier.buildClassifier(trainingData);

        // Chatbot interaction loop
        while (true) {
            System.out.println("What symptom do you have? (Type 'exit' to quit)");
            String symptom = scanner.nextLine();
            if (symptom.equalsIgnoreCase("exit")) {
                break;
            }
            
            // Create an instance for prediction
            Instance instance = new DenseInstance(1.0, new double[trainingData.numAttributes()]);
            instance.setDataset(trainingData);
            instance.setValue(trainingData.attribute("Symptom"), symptom);
            
            // Predict disease based on user's response
            double prediction = classifier.classifyInstance(instance);
            String predictedDisease = trainingData.classAttribute().value((int) prediction);
            System.out.println("Predicted disease: " + predictedDisease);
        }
    }

    private static Instances loadTrainingData(String filename) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        Instances data = new Instances(reader);
        reader.close();
        // Set the class attribute
        data.setClassIndex(data.numAttributes() - 1);
        return data;
    }

    private static void convertCSVtoARFF(String csvFile, String arffFile) throws IOException {
        // Load CSV file
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(csvFile));
        Instances data = loader.getDataSet();

        // Save as ARFF
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File(arffFile));
        saver.writeBatch();
    }
}
