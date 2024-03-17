package Training;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;

import java.io.File;

public class traindata {

    public static void main(String[] args) {
        try {
            // Load the dataset
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File("Data/Dataset.csv")); // Replace with the actual path to your dataset
            Instances dataset = loader.getDataSet();
            dataset.setClassIndex(dataset.numAttributes() - 1); // Set the class attribute

            // Print information about the dataset
            System.out.println("Dataset summary:\n" + dataset.toSummaryString());

            // Create a training set (you can adjust the percentage of instances for training)
            Instances trainingSet = new Instances(dataset);
            trainingSet.randomize(new java.util.Random(1)); // Set a seed for reproducibility
            int trainSize = (int) Math.round(trainingSet.numInstances() * 0.8); // 80% for training
            int testSize = trainingSet.numInstances() - trainSize; // Remaining for testing

            Instances train = new Instances(trainingSet, 0, trainSize);
            Instances test = new Instances(trainingSet, trainSize, testSize);

            // Optionally, you can save the training and testing sets to CSV files
            CSVSaver csvSaver = new CSVSaver();
            csvSaver.setInstances(train);
            csvSaver.setFile(new File("Data/trainingSet.csv")); // Replace with the desired output path
            csvSaver.writeBatch();

            csvSaver.setInstances(test);
            csvSaver.setFile(new File("Data/testingSet.csv")); // Replace with the desired output path
            csvSaver.writeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
