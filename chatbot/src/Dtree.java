import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.converters.CSVLoader;
import weka.core.Instances;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;

public class Dtree extends JFrame {
    private JButton loadDatasetButton;
    private JButton trainDataButton;
    private JTextArea outputTextArea;

    public Dtree() {
        setTitle("Decision Tree Classifier");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        loadDatasetButton = new JButton("Load Dataset");
        trainDataButton = new JButton("Train Data");
        outputTextArea = new JTextArea();

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loadDatasetButton);
        buttonPanel.add(trainDataButton);

        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

        add(mainPanel);

        loadDatasetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadDataset();
            }
        });

        trainDataButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                trainData();
            }
        });
    }

    private void loadDataset() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                CSVLoader loader = new CSVLoader();
                loader.setSource(selectedFile);
                Instances data = loader.getDataSet();
                outputTextArea.setText("Dataset loaded successfully:\n" + data.toSummaryString());
            } catch (Exception ex) {
                outputTextArea.setText("Error loading dataset: " + ex.getMessage());
            }
        }
    }

    private void trainData() {
        try {
            // Replace this path with the path of your loaded dataset
            File datasetFile = new File("Data/trainingset.csv");
            if (!datasetFile.exists()) {
                outputTextArea.setText("Please load the dataset first.");
                return;
            }

            CSVLoader loader = new CSVLoader();
            loader.setSource(datasetFile);
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
            tree.setMinNumObj(10); // Adjust the value as needed
            tree.buildClassifier(data);

            // Evaluate the decision tree
            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(tree, data, 10, new Random(1));

            // Print evaluation results
            String evaluationResult = eval.toSummaryString("\nResults\n======\n", false);

            // Save the tree to a .dot file
            FileWriter writer = new FileWriter("D:/Project/java project/decisionTree.dot");
            writer.write(tree.graph());
            writer.close();

            // Display evaluation results
            outputTextArea.setText(evaluationResult);
        } catch (Exception e) {
            outputTextArea.setText("Error training data: " + e.getMessage());
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Dtree().setVisible(true);
            }
        });
    }
}
