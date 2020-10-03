package application;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import neuralnetwork.BoardEvaluation;
import neuralnetwork.NN;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class TrainingView {
    Stage window;
    BorderPane layout;
    VBox vBox;

    HBox progressBox;
    ProgressBar progressBar;
    SimpleDoubleProperty progress;
    Label etaL;
    SimpleStringProperty eta;
    long dataLines;

    HBox modelBox;
    Label modelL;
    ComboBox<String> modelSelection;

    Label structureL;

    HBox trainingLabelValueBox;
    Label trainingLabelValueL;
    ComboBox<String> trainingLabelValue;

    Label evaluation;
    SimpleStringProperty eval;

    TextField learningRate;
    Label learningRateL;
    HBox learningRateBox;

    TextField minimumDepthOfData;
    Label minimumDepthOfDataL;
    HBox minimumDepthOfDataBox;

    TextField batchSize;
    Label batchSizeL;
    HBox batchSizeBox;

    CheckBox learnFromOwnData;
    Label learnFromOwnDataL;
    HBox learnFromOwnDataBox;

    HBox buttonsBox;
    Button startPause;
    SimpleStringProperty buttonText;
    Button save;

    NN model;
    RandomAccessFile data;
    int batchSizeNum = 10;

    TrainingProcess trainingProcess;

    public void display(){
        window = new Stage();
        // Progress Bar
        progressBar = new ProgressBar();
        progressBar.setProgress(0.0);
        progressBar.setPrefWidth(350);
        progress = new SimpleDoubleProperty(0.0);
        progressBar.progressProperty().bind(progress);

        etaL = new Label();
        eta = new SimpleStringProperty("Time Left: 0m");
        etaL.textProperty().bind(eta);

        progressBox = new HBox();
        App.SetUpHBoxCentered(progressBox);
        progressBox.getChildren().addAll(progressBar, etaL);

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
        eval = new SimpleStringProperty("Evaluation: --");
        evaluation.textProperty().bind(eval);

        // Learning Rate
        learningRateL = new Label("Learning Rate");

        learningRate = new TextField();
        setLearningRateListener();

        learningRateBox = new HBox();
        App.SetUpHBoxLeft(learningRateBox);
        learningRateBox.getChildren().addAll(learningRateL, learningRate);

        // Minimum Depth of Data
        minimumDepthOfDataL = new Label("Minimum Depth of Data");

        minimumDepthOfData = new TextField();
        setMinimumDepthOfDataListener();

        minimumDepthOfDataBox = new HBox();
        App.SetUpHBoxLeft(minimumDepthOfDataBox);
        minimumDepthOfDataBox.getChildren().addAll(minimumDepthOfDataL, minimumDepthOfData);

        // Batch Size
        batchSizeL = new Label("Batch Size");

        batchSize = new TextField("10");
        setBatchSizeListener();

        batchSizeBox = new HBox();
        App.SetUpHBoxLeft(batchSizeBox);
        batchSizeBox.getChildren().addAll(batchSizeL, batchSize);

        // Learn from own data
        learnFromOwnDataL = new Label("Learn From Own Data");

        learnFromOwnData = new CheckBox();
        setLearnFromOwnDataListener();

        learnFromOwnDataBox = new HBox();
        App.SetUpHBoxLeft(learnFromOwnDataBox);
        learnFromOwnDataBox.getChildren().addAll(learnFromOwnDataL, learnFromOwnData);

        // Buttons
        startPause = new Button("Start");
        startPause.setDisable(true);
        setStartPauseListener();
        buttonText = new SimpleStringProperty("Start");
        startPause.textProperty().bind(buttonText);

        save = new Button("Save Model");
        save.setDisable(true);
        save.setOnAction(e -> saveModel());

        Button refreshFile = new Button("Refresh File");
        refreshFile.setOnAction(e -> {
            if (!trainingProcess.isAlive()){
                loadDataSet();
            }
        });

        Button restartTraining = new Button("Reset Cursor");
        restartTraining.setOnAction(e -> {
            if (model != null && !trainingProcess.isAlive()){
                model.setTrainingCursor(0);
                loadDataSet();
                updateEvaluation();
            }
        });

        Button updateEvaluationButton = new Button("Update Evaluation");
        updateEvaluationButton.setOnAction(e -> updateEvaluation());

        buttonsBox = new HBox();
        App.SetUpHBoxCentered(buttonsBox);
        buttonsBox.getChildren().addAll(startPause, save, refreshFile, restartTraining, updateEvaluationButton);

        // VBox
        vBox = new VBox();
        vBox.getChildren().addAll(modelBox, structureL, trainingLabelValueBox, evaluation, learningRateBox, minimumDepthOfDataBox, batchSizeBox, learnFromOwnDataBox);
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

        // Load File
        loadDataSet();

        // Initialize Thread
        trainingProcess = new TrainingProcess(progress, eta, eval, buttonText);

        // Window
        Scene s = new Scene(layout);
        window.setTitle("Training");
        window.setWidth(500);
        window.setHeight(400);
        window.setResizable(false);
        window.setScene(s);
        window.centerOnScreen();
        window.showAndWait();

    }

    public void loadDataSet(){
        try {
            String path = getClass().getResource("../resources/").getPath()+"shuffled.csv";
            data = new RandomAccessFile(path, "r");
            data.seek(App.CSVHeader.length());
            dataLines = (data.length() - App.CSVHeader.length()) / App.CSVLineLength;

            if(model != null){
                progress.setValue((double)(model.getTrainingCursor())/(dataLines));
            }
        } catch (FileNotFoundException e) {
            System.err.println("Could Not Load Data set: resources/"+App.DataSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                minimumDepthOfData.setText(Integer.toString(model.getMiniumDepthOfData()));
            }
        });
    }

    private void setBatchSizeListener(){
        batchSize.textProperty().addListener(e -> {
            String val = minimumDepthOfData.getText();
            if (val.matches("\\d+")){
                batchSizeNum = Integer.parseInt(val);
            } else {
                batchSize.setText(Integer.toString(batchSizeNum));
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
        window.setTitle(modelSelection.getValue());
        model = newModel;

        structureL.setText(model.getStructure());
        trainingLabelValue.setValue(model.getLabelingMethod() == NN.LabelingMethod.GameOutcome ? "Game Outcome" : "Minimax Score");
        learningRate.setText(Double.toString(model.getLearningRate()));
        minimumDepthOfData.setText(Integer.toString(model.getMiniumDepthOfData()));
        learnFromOwnData.setSelected(model.isLearnFromOwnData());

        progress.setValue((double)(model.getTrainingCursor())/(dataLines));
        startPause.setDisable(false);
        updateEvaluation();
    }

    private void updateEvaluation(){
        eval.setValue(String.format("Mean Error: %.2f, Cursor: %d/%d", trainingProcess.eval, model.getTrainingCursor(), dataLines));
    }

    private void setStartPauseListener(){
       startPause.setOnAction(e -> {
           System.out.println("*");
           if(startPause.getText().equals("Start")){
               if(trainingProcess.isAlive()) {
                   System.out.println("Wait until the other process is done");
               } else {
                   buttonText.setValue("Pause");
                   trainingProcess = new TrainingProcess(progress, eta, eval, buttonText);
                   trainingProcess.start();
               }
           } else {
               buttonText.setValue("Start");
           }
       });

    }

    class TrainingProcess extends Thread {
        SimpleDoubleProperty pg;
        SimpleStringProperty et;
        SimpleStringProperty ev;
        SimpleStringProperty bt;

        double eval = -1;

        public TrainingProcess(SimpleDoubleProperty pg, SimpleStringProperty et, SimpleStringProperty ev, SimpleStringProperty bt) {
            this.pg = pg;
            this.et = et;
            this.ev = ev;
            this.bt = bt;
        }

        private void updateProgressBar(){
            pg.setValue((double)(model.getTrainingCursor())/(dataLines));
        }

        @Override
        public void run() {
            while (startPause.getText().equals("Pause") && model.getTrainingCursor() < dataLines) {
                try {
                    eval = model.trainOnBatch(data, batchSizeNum);
                    saveModel();
                    updateProgressBar();
                    //updateEvaluation(eval);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            if(model.getTrainingCursor() >= dataLines) {
                System.out.println("Done Training");
            }
        }
    }

}
