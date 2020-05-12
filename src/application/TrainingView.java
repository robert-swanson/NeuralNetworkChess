package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import neuralnetwork.NN;

public class TrainingView {
    Stage window;
    BorderPane layout;
    VBox vBox;

    HBox progressBox;
    ProgressBar progressBar;
    Label timeLeftL;

    HBox modelBox;
    Label modelL;
    ComboBox<String> modelSelection;

    Label structureL;

    HBox trainingLabelValueBox;
    Label trainingLabelValueL;
    ComboBox<String> trainingLabelValue;

    Label evaluation;

    TextField learningRate;
    Label learningRateL;
    HBox learningRateBox;

    TextField minimumDepthOfData;
    Label minimumDepthOfDataL;
    HBox minimumDepthOfDataBox;

    CheckBox learnFromOwnData;
    Label learnFromOwnDataL;
    HBox learnFromOwnDataBox;

    HBox buttonsBox;
    Button startPause;
    Button save;

    NN model;

    public void display(){
        // Progress Bar
        progressBar = new ProgressBar();
        progressBar.setProgress(0.0);
        progressBar.setPrefWidth(350);

        timeLeftL = new Label("Time Left: 0m");

        progressBox = new HBox();
        App.SetUpHBoxCentered(progressBox);
        progressBox.getChildren().addAll(progressBar, timeLeftL);

        // Model Selection
        modelL = new Label("Training Label:");

        modelSelection = new ComboBox<String>(NN.getModelList());
        setModelSelectionListener();

        modelBox = new HBox();
        App.SetUpHBoxLeft(modelBox);
        modelBox.getChildren().addAll(modelL, modelSelection);

        // Structure
        structureL = new Label("Structure: --");

        // Training Label
        trainingLabelValueL = new Label("Training Label:");

        ObservableList<String> options = FXCollections.observableArrayList("Game Outcome", "Minimax Score");
        trainingLabelValue = new ComboBox<>(options);
        setTrainingLabelValueListener();

        trainingLabelValueBox = new HBox();
        App.SetUpHBoxLeft(trainingLabelValueBox);
        trainingLabelValueBox.getChildren().addAll(trainingLabelValueL, trainingLabelValue);

        // Evaluation
        evaluation = new Label("Evaluation: --");

        // Learning Rate
        learningRateL = new Label("Learning Rate:");

        learningRate = new TextField();
        setLearningRateListener();

        learningRateBox = new HBox();
        App.SetUpHBoxLeft(learningRateBox);
        learningRateBox.getChildren().addAll(learningRateL, learningRate);

        // Minimum Depth of Data
        minimumDepthOfDataL = new Label("Minimum Depth of Data:");

        minimumDepthOfData = new TextField();
        setMinimumDepthOfDataListener();

        minimumDepthOfDataBox = new HBox();
        App.SetUpHBoxLeft(minimumDepthOfDataBox);
        minimumDepthOfDataBox.getChildren().addAll(minimumDepthOfDataL, minimumDepthOfData);

        // Learn from own data
        learnFromOwnDataL = new Label("Learn From Own Data: ");

        learnFromOwnData = new CheckBox();
        setLearnFromOwnDataListener();

        learnFromOwnDataBox = new HBox();
        App.SetUpHBoxLeft(learnFromOwnDataBox);
        learnFromOwnDataBox.getChildren().addAll(learnFromOwnDataL, learnFromOwnData);

        // Buttons
        startPause = new Button("Start");

        save = new Button("Save Model");
        save.setDisable(true);
        save.setOnAction(e -> saveModel());

        buttonsBox = new HBox();
        App.SetUpHBoxCentered(buttonsBox);
        buttonsBox.getChildren().addAll(startPause, save);

        // VBox
        vBox = new VBox();
        vBox.getChildren().addAll(modelBox, structureL, trainingLabelValueBox, evaluation, learningRateBox, minimumDepthOfDataBox, learnFromOwnDataBox);
        vBox.getChildren().forEach(e -> VBox.setMargin(e, new Insets(0, 10, 0, 10)));
        App.SetUpVBoxLeft(vBox);

        // Stack Pane
        layout = new BorderPane();
        layout.setTop(progressBox);
        layout.setCenter(vBox);
        layout.setBottom(buttonsBox);
        BorderPane.setAlignment(progressBox, Pos.CENTER);
        BorderPane.setAlignment(buttonsBox, Pos.CENTER);
        BorderPane.setMargin(progressBox, new Insets(10, 10, 10, 10));
        BorderPane.setMargin(buttonsBox, new Insets(10, 10, 10, 10));

        // Window
        Scene s = new Scene(layout);
        window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Training");
        window.setWidth(500);
        window.setHeight(400);
        window.setResizable(false);
        window.setScene(s);
        window.centerOnScreen();
        window.showAndWait();
    }

    private void setModelSelectionListener(){
        modelSelection.valueProperty().addListener(e -> {
            String filename = modelSelection.getValue().toString();
            filename += ".txt";
            try {
                NN newModel = new NN(filename);
                loadModel(newModel);
            } catch (Exception error) {
                System.err.println("Failed to load model: " + error.getMessage());
                modelSelection.setValue("");
            }
        });
    }

    private void setTrainingLabelValueListener(){
        trainingLabelValue.valueProperty().addListener(e -> {
            model.setLabelingMethod(trainingLabelValue.getValue().equals("Game Outcome") ? NN.LabelingMethod.GameOutcome : NN.LabelingMethod.StandardScore);
            save.setDisable(false);
        });
    }

    private void setLearningRateListener(){
        learningRate.textProperty().addListener(e -> {
            String val = learningRate.getText();
            if (val.matches("\\d*\\.?\\d+")){
                model.setLearningRate(Double.parseDouble(val));
                save.setDisable(false);
            } else {
                learningRate.setText(Double.toString(model.getLearningRate()));
            }
        });
    }

    private void setMinimumDepthOfDataListener(){
        minimumDepthOfData.textProperty().addListener(e -> {
            String val = minimumDepthOfData.getText();
            if (val.matches("\\d+")){
                model.setMiniumDepthOfData(Integer.parseInt(val));
                save.setDisable(false);
            } else {
                learningRate.setText(Double.toString(model.getMiniumDepthOfData()));
            }
        });
    }

    private void setLearnFromOwnDataListener(){
        learnFromOwnData.selectedProperty().addListener(e -> {
            model.setLearnFromOwnData(learnFromOwnData.isSelected());
            save.setDisable(false);
        });
    }

    private void saveModel(){
        model.save(modelSelection.getValue()+".txt");
        save.setDisable(true);
    }

    private void loadModel(NN newModel) {
       model = newModel;

       structureL.setText(model.getStructure());
       trainingLabelValue.setValue(model.getLabelingMethod() == NN.LabelingMethod.GameOutcome ? "Game Outcome" : "Minimax Score");
       learningRate.setText(Double.toString(model.getLearningRate()));
       minimumDepthOfData.setText(Integer.toString(model.getMiniumDepthOfData()));
       learnFromOwnData.setSelected(model.isLearnFromOwnData());
    }



}
