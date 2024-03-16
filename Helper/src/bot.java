import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class bot {

    public static void main(String[] args) {
        // Replace this array with your actual symptoms list
        String[] symptoms = {
                "itching", "skin_rash", "nodal_skin_eruptions", "continuous_sneezing", "shivering",
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
                "swollen_extremities", "excessive_hunger", "extra_marital_contacts", "drying_and_tingling_lips", "slurred_speech",
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
                "yellow_crust_ooze"
        };

        // Replace "tree" with the actual decision tree object you created
        J48 tree = createDecisionTree();

        if (tree != null) {
            // Load training data
            Instances data = loadData();

            // Make a prediction
            String predictedDisease = predictDisease(tree, symptoms, data);

            System.out.println("Predicted Disease: " + predictedDisease);
        } else {
            System.out.println("Failed to create decision tree.");
        }
    }

    private static J48 createDecisionTree() {
        try {
            // Load training data
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File("Data/Dataset.csv"));
            Instances data = loader.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);

            // Build a decision tree
            J48 tree = new J48();
            tree.buildClassifier(data);
            
            String graph = tree.graph();
            try (PrintWriter out = new PrintWriter("tree.dot")) {
                out.println(graph);
            }

            // Return the decision tree
            return tree;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Instances loadData() {
        try {
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File("Data/Dataset.csv"));
            Instances data = loader.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String predictDisease(J48 tree, String[] symptoms, Instances data) {
        try {
            // Manually traverse the decision tree based on user input
            Scanner scanner = new Scanner(System.in);

            int node = 0;
            while (true) {
                System.out.println("Current Node: " + node);
                System.out.println("Question: " + getQuestion(tree, node));
                System.out.print("Enter 1 for Yes, 0 for No: ");

                int answer = scanner.nextInt();
                // Extract the next node based on user input
                node = getNextNode(tree, node, answer, data);

                // If it's a leaf node, break the loop
                if (node == -1) {
                    break;
                }
            }

            // Get the predicted disease label from the leaf node
            int predictedClassIndex = (int) tree.classifyInstance(data.instance(0));
            String predictedDisease = data.classAttribute().value(predictedClassIndex);

            return predictedDisease;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error predicting disease.";
        }
    }

    private static String getQuestion(J48 tree, int node) {
        try {
            // Extract the question from the decision tree
            if (tree == null || tree.graph() == null) {
                return "Unknown Question";
            }

            String[] lines = tree.graph().split("\n");
            for (String line : lines) {
                if (line.startsWith("N" + node)) {
                    int startIndex = line.indexOf("[") + 1;
                    int endIndex = line.indexOf("]");
                    return line.substring(startIndex, endIndex).trim();
                }
            }
            return "Unknown Question";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error getting question.";
        }
    }

    private static int getNextNode(J48 tree, int currentNode, int answer, Instances data) {
        try {
            if (tree == null || tree.graph() == null) {
                return -1;
            }

            String[] lines = tree.graph().split("\n");
            for (String line : lines) {
                if (line.startsWith("N" + currentNode)) {
                    String[] parts = line.split("->");

                    if (parts.length > 1) {
                        String successor = parts[1].trim().split(" ")[0];

                        if (successor.startsWith("N")) {
                            currentNode = Integer.parseInt(successor.substring(1));
                            return currentNode;
                        } else if (successor.startsWith("leaf")) {
                            return -1; // Indicates leaf node
                        }
                    }
                }
            }

            // If the current node has no successor (e.g., due to previous wrong answers), move to the root
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}


