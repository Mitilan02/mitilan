package com.hcbot.HCbot;

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

import javax.swing.JFrame;

import weka.gui.graphvisualizer.GraphVisualizer;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;

public class Hcbot extends JFrame {
    private JButton loadDatasetButton;
    private JButton trainDataButton;
    private JButton showDecisionTreeButton;
    private JTextArea outputTextArea;
    private J48 treeModel;

    private Instances trainingSet;
    private Instances testingSet;

    public Hcbot() {
        setTitle("Decision Tree Classifier");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        loadDatasetButton = new JButton("Load Dataset");
        trainDataButton = new JButton("Train Data");
        showDecisionTreeButton = new JButton("Decision Tree");
        outputTextArea = new JTextArea();

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loadDatasetButton);
        buttonPanel.add(trainDataButton);
        buttonPanel.add(showDecisionTreeButton);

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

        showDecisionTreeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (treeModel != null) {
                    showDecisionTree();
                } else {
                    outputTextArea.append("\nNo decision tree available. Train a model first.");
                }
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

                // Create training and testing sets
                createTrainTestSets(data);
            } catch (Exception ex) {
                outputTextArea.setText("Error loading dataset: " + ex.getMessage());
            }
        }
    }

    private void createTrainTestSets(Instances data) {
        try {
            // Set the class attribute
            data.setClassIndex(data.numAttributes() - 1);

            // Create a training set (80% of the data) and a testing set (20% of the data)
            int trainSize = (int) Math.round(data.numInstances() * 0.8);
            int testSize = data.numInstances() - trainSize;

            trainingSet = new Instances(data, 0, trainSize);
            testingSet = new Instances(data, trainSize, testSize);

            // Save training and testing sets to CSV files
            saveToCSV(trainingSet, "Data/trainingSet.csv");
            saveToCSV(testingSet, "Data/testingSet.csv");
        } catch (Exception e) {
            outputTextArea.append("\nError creating training and testing sets: " + e.getMessage());
        }
    }

    private void saveToCSV(Instances instances, String filename) {
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(instances.toString());
            writer.close();
        } catch (Exception e) {
            outputTextArea.append("\nError saving " + filename + " to CSV: " + e.getMessage());
        }
    }

    private void trainData() {
        try {
            if (trainingSet == null || testingSet == null) {
                outputTextArea.append("\nTraining or testing set is missing. Load dataset first.");
                return;
            }

            // Build a decision tree
            treeModel = new J48();
            treeModel.setMinNumObj(10); // Adjust the value as needed
            treeModel.buildClassifier(trainingSet);

            // Evaluate the decision tree
            Evaluation eval = new Evaluation(trainingSet);
            eval.evaluateModel(treeModel, testingSet);

            // Print evaluation results
            String evaluationResult = eval.toSummaryString("\nResults\n======\n", false);

            // Display evaluation results
            outputTextArea.setText(evaluationResult);
        } catch (Exception e) {
            outputTextArea.append("\nError training data: " + e.getMessage());
        }
    }

   

    private void showDecisionTree() {
        try {
            // Generate DOT string from the decision tree
            String dotStr = treeModel.graph();

            // Prompt user for download location
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Decision Tree PDF");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int userSelection = fileChooser.showSaveDialog(this);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File saveLocation = fileChooser.getSelectedFile();

                // Write DOT string to a temporary file
                File tempDotFile = new File("temp.dot");
                FileWriter writer = new FileWriter(tempDotFile);
                writer.write(dotStr);
                writer.close();

                // Convert DOT file to PDF using GraphViz
                ProcessBuilder pb = new ProcessBuilder("dot", "-Tpdf", "-o", saveLocation.getAbsolutePath() + "/decision_tree.pdf", tempDotFile.getAbsolutePath());
                pb.redirectErrorStream(true);
                Process process = pb.start();
                process.waitFor();

                // Delete temporary DOT file
                tempDotFile.delete();

                outputTextArea.append("\nDecision tree PDF saved successfully.");
            }
        } catch (Exception e) {
            outputTextArea.append("\nError generating and saving decision tree PDF: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Hcbot().setVisible(true);
            }
        });
    }
}

