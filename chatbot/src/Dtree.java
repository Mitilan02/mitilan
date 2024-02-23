import weka.core.Instances;
import weka.classifiers.trees.J48;
import weka.classifiers.Evaluation;

import weka.core.converters.CSVLoader;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;

import Training.*;

public class Dtree {

    public static void main(String[] args) {
    	traindata td = new traindata();
    	td.main(args);
    	
        try {
            // Load training data
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File("Data/trainingset.csv"));
            Instances data = loader.getDataSet();

            // Convert string attributes to nominal
            for (int i = 0; i < data.numAttributes(); i++) {
                if (data.attribute(i).isString()) {
                    data = convertStringToNominal(data, i);
                }
            }

            data.setClassIndex(data.numAttributes() - 1);

            // Build a decision tree
            
            J48 tree = new J48();
            tree.setMinNumObj(10);  // Adjust the value as needed
            tree.buildClassifier(data);

            
            // Evaluate the decision tree
            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(tree, data, 10, new Random(1));

            // Print evaluation results
            System.out.println(eval.toSummaryString("\nResults\n======\n", false));

            // Save the tree to a .dot file
            FileWriter writer = new FileWriter("D:/Project/java project/decisionTree.dot");
            writer.write(tree.graph());
            writer.close();

            // Now you can use external tools like Graphviz to convert the .dot file to an image or PDF.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Instances convertStringToNominal(Instances data, int attributeIndex) throws Exception {
        // Use StringToNominal filter to convert string attribute to nominal
        weka.filters.unsupervised.attribute.StringToNominal filter = new weka.filters.unsupervised.attribute.StringToNominal();
        filter.setAttributeRange(Integer.toString(attributeIndex + 1));
        filter.setInputFormat(data);
        Instances newData = weka.filters.Filter.useFilter(data, filter);
        return newData;
    }
}
