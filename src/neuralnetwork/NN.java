package neuralnetwork;

import application.App;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Defines a neural network that supports custom layer structure, classification through forward propagation, and training through back propagation
 * @author Robert Swanson: https://github.com/robert-swanson
 * @version 1.1
 */
public class NN {
	// Chess Options
	public enum LabelingMethod {GameOutcome, StandardScore};
	private LabelingMethod labelingMethod;


	private double learningRate;
	private int miniumDepthOfData;


	private boolean learnFromOwnData;

	// Network Attributes
	private double[][][] weights;
	private double[][] biases;
	private int[] structure = new int[]{};

	// Network State
	private double[] input;
	private double[][] activations;

	// Derivatives
	private double[][][] dcdw;
	private double[][] dcdb;
	private double[][] dcda;


	// Training
	private int trainingCursor;
	private double bestAccuracy;

	// Constants
	public static final int BOARD_LAYER_SIZE = 768;
	public static final int OUTPUT_LAYER_SIZE = 1;

	//  ================================================= Constructors =============================================

	/**
	 * Creates a random new neural network based off of the given structure
	 * @param structure in integer array with size at least two (for input and output layers) defining the number of layers in the network, and each element defining the size of that layer
	 */
	public NN(int[] structure, LabelingMethod labelingMethod, double learningRate, int miniumDepthOfData, boolean learnFromOwnData) {
		this.structure = structure;
		if(structure.length <= 1) {
			System.err.println("NN Structure must be at least 2 layers");
		}
		weights = new double[structure.length-1][][];
		biases = new double[structure.length-1][];
		activations = new double[structure.length-1][];
		for(int layer = 0; layer < structure.length-1; layer++) {
			if(structure[layer] <= 0 || structure[layer+1] <= 0) {
				System.err.println("NN Structure cannot include any numbers less than 1");
				return;
			}
			weights[layer] = randArr(structure[layer+1], structure[layer]);
			biases[layer] = randArr(structure[layer+1]);
			activations[layer] = new double[structure[layer+1]];
		}
		trainingCursor = 0;

		this.labelingMethod = labelingMethod;
		this.learningRate = learningRate;
		this.miniumDepthOfData = miniumDepthOfData;
		this.learnFromOwnData = learnFromOwnData;
	}

	/**
	 * Constructs a neural network by loading the data from "Current.txt"
	 */
	public NN() { //Test Init
		load("Current.txt");
	}

	/**
	 * Constructs a neural network by loading the data from the file specified
	 */
	public NN(String filename) { //Test Init
		load(filename);
	}

	//  ================================================= Classifying ==============================================

	/**
	 * Tests the neural network on the provided boards
	 * @param boards a list of boards to test on
	 * @return the mean error of the tested boards
	 */
	public double testWithBoards(LinkedList<BoardEvaluation> boards) {
		int t = boards.size();
		double errorSum = 0;
		int totalTested = 0;
		double label;
		for(BoardEvaluation board: boards) {
			if (board.depth >= miniumDepthOfData) {
				totalTested++;
				errorSum += Math.abs(classify(board)-board.getLabel(labelingMethod));
			}
		}
		return errorSum/totalTested;
	}

	/**
	 * Classifies the given board through forward propagation
	 * @param board the board to classify
	 * @return the classification of the board
	 */
	public double classify(BoardEvaluation board) {
		updateInput(board.getExpandedBoardInputLayer());
		propagateForward();
		return getOutput()[0];
	}

	// Propagates forward through the neural network
	private void propagateForward() {
		activations[0] = sigmoid(sumProd(input, weights[0], biases[0]));
		for(int layer = 1; layer < structure.length - 1; layer++) {
			activations[layer] = sigmoid(sumProd(activations[layer-1], weights[layer], biases[layer]));
		}
	}

	/**
	 * Set the values for the input layer
 	 * @param input an array the same length as the input layer
	 */
	private void updateInput(double[] input) {
		if(input.length != structure[0]) {
			System.err.println("GET ACTIVATION ERROR: Input wrong shape");
			return;
		}
		this.input = input;
	}

	/**
	 * Retrieves the result of the forward propagation
	 * @return an array the same length as the output layer containing the output layer
	 */
	public double[] getOutput() {
		if(input == null) {
			System.err.println("GET ACTIVATION ERROR: No input");
			return new double[0];
		}
		return activations[activations.length-1];
	}

	/**
	 * Retrieves the result of the classification
	 * @return the integer value of the classification
	 */
	private int getGuess() {
		double biggest = 0;
		int index = 0;
		for(int i = 0; i < structure[structure.length-1]; i++) {
			double val = activations[activations.length-1][i];
			if(val > biggest) {
				biggest = val;
				index = i;
			}
		}
		return index;
	}

	// Calculates the cost of the last calculation
	private double getCost(double[] label) {
		double cost = 0;
		for(int i = 0; i < label.length; i++) {
			cost += Math.pow(label[i]-getOutput()[i], 2);
		}
		return cost;
	}

	//  ================================================= Training =================================================

	/**
	 * Trains the network on one batch of data
	 * @param file The file containing the training data, the cursor should be pointing at the line specified by trainingCursor
	 * @param batchSize The number of data to be included in the batch
	 * @return the mean error, -1 if no training occurred
	 * @throws IOException If error reading the file
	 */
	public double trainOnBatch(RandomAccessFile file, int batchSize) throws IOException {
		System.out.printf("Training Entry %d\n", trainingCursor);
		double errorSum = 0;
		int count = 0;
		double[][][] weightVector = null;
		double[][] biasVector = null;
		BoardEvaluation board;

		for(int dataIndex = 0; dataIndex < batchSize &&  file.getFilePointer() < file.length(); dataIndex++) { //Iterates through each data set in the batch
			board = new BoardEvaluation(file.readLine());
			trainingCursor++;

			if (board.depth < miniumDepthOfData){
				dataIndex--;
				continue;
			}

			classify(board);
			backPropagate(board.getExpandedBoardInputLayer(), new double[] {board.getLabel(labelingMethod)});

			if (weightVector == null) {
				weightVector = dcdw;
				biasVector = dcdb;
			} else {
				weightVector = sumMatrix(weightVector, dcdw);
				biasVector = sumMatrix(biasVector, dcdb);
			}
			errorSum += Math.abs(getOutput()[0]-board.getLabel(labelingMethod));
			count++;
		}
		if(count > 0) {
			divideMatrix(weightVector, count);
			divideMatrix(biasVector, count);
			//Make changes from batch
			multiplyMatrix(weightVector, learningRate*-1);
			multiplyMatrix(biases, learningRate*-1);
			weights = sumMatrix(weights, weightVector);
			biases = sumMatrix(biases, biasVector);

			System.out.printf("Mean Error %f\n", errorSum/count);
			return errorSum/count;
		}
		return -1;
	}

	// Sets the input, propagates forward, then propagates backward
	private void backPropagate(double[] data, double[] label) {
		updateInput(data);
		propagateForward();
		dcdw = emptyWArr(structure);
		dcdb = emptyBArr(structure);
		dcda = emptyBArr(structure);
		for(int layer = structure.length-2; layer >= 0; layer--) {
			for(int j = 0; j < structure[layer+1]; j++) {
				dcdb[layer][j] = getDcDb(layer, j, label);
				for(int k = 0; k < structure[layer]; k++) {
					dcdw[layer][j][k] = getDcDw(layer, j, k, label);
				}
			}
		}
	}

	// Calls `backPropigate` and returns the execution time
	public int getBackPropTime(BoardEvaluation board) {
		long start = System.currentTimeMillis();
		backPropagate(board.getExpandedBoardInputLayer(), new double[]{board.getLabel(labelingMethod)});
		return (int)(System.currentTimeMillis()-start);
	}

	// Estimates the execution time for the remaining batches
	public String getETA(double seconds, int batchesLeft) {
		SimpleDateFormat format = new SimpleDateFormat("hh:mm aa, EEEE");
		Calendar calender = GregorianCalendar.getInstance();
		int secondsLeft =  (int)(batchesLeft * seconds);
		calender.add(Calendar.SECOND, secondsLeft);
		return format.format(calender.getTime());
	}


	//  ================================================= File Management ==========================================

	/**
	 * Saves the network parameters to file
	 * @param file the filename to save to
	 */
	public void save(String file) {
		PrintWriter print;
		try {
			URL modelsURL = getClass().getResource("../resources/models");
			File modelDirectory = new File(modelsURL.getPath());
			File newFile = new File(modelDirectory.getPath()+"/"+file);
			print = new PrintWriter(newFile);
			print.printf("%23s %d\n", "Training Cursor:", trainingCursor);

			print.printf("%23s ", "Structure:", trainingCursor);
			for(int i:structure) print.print(i+ " ");
			print.println();


			print.printf("%23s %s\n", "Labeling Method:", labelingMethod.toString());
			print.printf("%23s %f\n", "Learning Rate:", learningRate);
			print.printf("%23s %d\n", "Minimum Depth of Data:", miniumDepthOfData);
			print.printf("%23s %s\n", "Learn From Own Data:", learnFromOwnData ? "yes" : "no");

			for(int x = 0; x < weights.length; x++)
				for(int y = 0; y < weights[x].length; y++) {
					for(int z = 0; z < weights[x][y].length; z++)
						print.print(weights[x][y][z]+" ");
					print.println();
				}
			for(int x = 0; x < biases.length; x++) {
				for(int y = 0; y < biases[x].length; y++)
					print.print(biases[x][y]+" ");
				print.println();
			}
			print.flush();
		} catch (FileNotFoundException e) {
			System.err.println("Could not save: "+e.getMessage());
		}
	}

	// Loads a file and sets the network parameters accordingly
	private void load(String file) {
		try {

			URL path = getClass().getResource("../resources/models/"+file);
			Scanner in = new Scanner(new File(path.getPath()));

			trainingCursor = Integer.parseInt(in.nextLine().substring(24));

			structure = sti(in.nextLine().substring(24).split(" "));
			weights = new double[structure.length-1][][];
			activations = new double[structure.length-1][];

			labelingMethod = LabelingMethod.valueOf(in.nextLine().substring(24));
			learningRate = Double.parseDouble(in.nextLine().substring(24));
			miniumDepthOfData = Integer.parseInt(in.nextLine().substring(24));
			learnFromOwnData = in.nextLine().substring(24).equalsIgnoreCase("yes");

			for(int l = 0; l < structure.length-1; l++) {
				weights[l] = new double[structure[l+1]][];
				activations[l] = new double[structure[l+1]];
				for(int j = 0; j < structure[l+1]; j++) {
					weights[l][j] = std(in.nextLine().split(" "));
				}
			}
			biases = new double[structure.length-1][];
			for(int l = 0; l < structure.length-1; l++)
				biases[l] = std(in.nextLine().split(" "));
			in.close();
		} catch (FileNotFoundException e) {
			System.err.printf("Saved File Not Found: %s\n", e.getMessage());
		}

	}

	// string to int[]
	private static int[] sti(String[] args) {
		int[] ret = new int[args.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = Integer.parseInt(args[i]);
		return ret;
	}

	// string to double[]
	private static double[] std(String[] args) {
		double[] ret = new double[args.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = Double.parseDouble(args[i]);
		return ret;
	}

	//  ================================================= Printing =================================================

// Graphs out the neural network
	public void printGraph() {
		System.out.print("In\t");
		for(int i = 1; i < structure.length; i++) {
			System.out.print("L"+i+"\t");
		}
		System.out.println();
		boolean letter = true;
		char c = 'A';
		int maxNodes = 0;
		for(int l = 0; l < structure.length; l++)
			maxNodes = Math.max(maxNodes, structure[l]);
		char[][] graph = new char[maxNodes][structure.length];
		for(int l = 0; l < structure.length; l++) {
			int start = (maxNodes-structure[l])/2;
			for(int i = 0; i < structure[l]; i++) {
				graph[i+start][l] = c++;
			}
			if(letter) c = '1';
			else c = 'A';
			letter = !letter;
		}
		for(int y = 0; y < graph.length; y++) {
			for(int x = 0; x < graph[y].length; x++) {
				System.out.print(graph[y][x]+"\t");
			}
			System.out.println();
		}
	}

	// Prints out the weights of the network
	public void printWeights() {
		for(int l = 0; l < structure.length-1; l++) {
			System.out.println("Layer "+l);
			for(int j = 0; j < structure[l+1]; j++) {
				for(int k = 0; k < structure[l]; k++) {
					char jc = (char)('A' + j);
					char kc = (char)('0' + k);
					if(l%2==0) {
						jc = (char)('0'+k);
						kc = (char)('A'+j);
					}
					System.out.printf("%c%c:\t%5.2f\n",kc,jc,weights[l][j][k]);
				}
				System.out.println();
			}
		}
	}

	// Prints out the biases of the network
	public void printBiases() {
		for(int l = 0; l < structure.length-1; l++) {
			System.out.println("Layer "+l);
			for(int k = 0; k < structure[l+1]; k++) {
				char c = (char)('0' + k);
				if(l%2==0) {
					c = (char)('A'+k);
				}
				System.out.printf("%c:\t%5.2f\n",c,biases[l][k]);
			}
			System.out.println();
		}
	}

	// Prints out the activations of the network
	public void printActivations() {
		for(int l = 0; l < structure.length-1; l++) {
			System.out.println("Layer "+l);
			for(int k = 0; k < structure[l+1]; k++) {
				char c = (char)('0' + k);
				if(l%2==0) {
					c = (char)('A'+k);
				}
				System.out.printf("%c:\t%5.2f\n",c,activations[l][k]);
			}
			System.out.println();
		}
	}

	// Prints a 3D array
	public static void print(double[][][] a) {
		for(double[][] daa : a) {
			print(daa);
		}
	}

	// Prints a 2D array
	public static void print(double[][] a) {
		System.out.print("[");
		for (int i = 0; i < a.length; i++) {
			if (i != 0)
				System.out.print("\n [");
			else
				System.out.print("[");
			for (int j = 0; j < a[i].length - 1; j++) {
				System.out.printf("%3.4f, ",a[i][j]);
			}
			System.out.printf("%3.4f]",a[i][a[i].length - 1]);
		}
		System.out.println("]");
	}

	//  ================================================= Math =====================================================

	// Calculates the sum of the bias and the product of the weight and activation values for forward propagation
	private double[] sumProd(double[] prevActivtions, double[][] weights, double[] biases) {
		double[] rv = new double[weights.length];
		if(prevActivtions.length != weights[0].length) {
			System.err.println("SUMPROD ERROR: Matracies shapes don't match");
			return rv;
		}
		for(int j = 0; j < weights.length; j++) {
			rv[j] = biases[j];
			for(int k = 0; k < weights[0].length; k++) {
				rv[j] += prevActivtions[k]*weights[j][k];
			}
		}
		return rv;
	}

	// Performs a deep addition on the 2D arrays
	private double[][] sumMatrix(double[][] a, double[][] b){
		if(a.length != b.length) {
			System.err.println("SUM MATRIX ERROR: Matracies not same shape");
			return null;
		}
		for(int y = 0; y < a.length; y++) {
			if(a[y].length != b[y].length) {
				System.err.println("SUM MATRIX ERROR: Matracies not same shape");
				return null;
			}
			for(int x = 0; x < a[y].length; x++) {
				a[y][x] += b[y][x];
			}
		}
		return a;
	}

	// Performs a deep addition on the 3D arrays
	private double[][][] sumMatrix(double[][][] a, double[][][] b){
		if(a.length != b.length) {
			System.err.println("SUM MATRIX ERROR: Matracies not same shape");
			return null;
		}
		for(int i = 0; i < a.length; i++) {
			a[i] = sumMatrix(a[i], b[i]);
		}
		return a;
	}

	// Performs a scalar division on the 3D matrix
	private void divideMatrix(double[][][] matrix, double c){
		for(int x = 0; x < matrix.length; x++)
			for(int y = 0; y < matrix[x].length; y++)
				for(int z = 0; z < matrix[x][y].length; z++)
					matrix[x][y][z] /= c;
	}

	// Performs a scalar division on the 2D matrix
	private void divideMatrix(double[][] matrix, double c){
		for(int x = 0; x < matrix.length; x++)
			for(int y = 0; y < matrix[x].length; y++)
				matrix[x][y] /= c;
	}

	// Performs scalar multiplication on the 2D matrix
	private void multiplyMatrix(double[][] a, double c){
		for(int j = 0; j < a.length; j++) {
			for(int i = 0; i < a[j].length; i++)
				a[j][i] *= c;
		}
	}

	// Performs scalar multiplication on the 3D matrix
	private void multiplyMatrix(double[][][] a, double c) {
		for(double[][] daa: a)
			multiplyMatrix(daa, c);
	}

	// Calculates the derivative of the cost with respect to the specified weight
	private double getDcDw(int l, int j, int k, double[] label) {
		double dzdw = getActivation(l-1, k);
		double a = getActivation(l, j);
		double z = sigmoidInv(a);
		double dadz = sigmoidP(z);
		double dcda = getDcDa(l, j, label);
		return dzdw * dadz * dcda;
	}

	// Calculates the derivative of the cost with respect to the specified bias
	private double getDcDb(int l, int j, double[] label) {
		double a = getActivation(l, j);
		double z = sigmoidInv(a);
		double dadz = sigmoidP(z);
		double dcda = getDcDa(l, j, label);
		return dadz * dcda;
	}

	// Calculates the derivative of the cost with respect to the specified activation
	private double getDcDa(int l, int k, double[] label) {
		double dcdak = 0.0;
		if(l == structure.length - 2) {
			dcdak = 2 * (getOutput()[k]-label[k]);
		}else if(l == structure.length - 1) {
			return 1;
		}else {
			for(int j = 0; j < structure[l+2]; j++) {
				double dzdak = weights[l+1][j][k]; //<-- Error point
				double a = getActivation(l+1, j);
				double z = sigmoidInv(a);
				double dadz = sigmoidP(z);
				//				double dcda = getDcDa(l+1, j, label);
				double dcda = this.dcda[l+1][j];
				dcdak += dzdak * dadz * dcda;
			}
		}
		dcda[l][k] = dcdak;
		return dcdak;
	}

	// Returns the activation at the specified location
	private double getActivation(int l, int j) {
		if(l >= structure.length || l < -1) {
			System.err.println("GET ACTIVATION ERROR: Layer index out of bounds");
			return 0.0;
		}
		if(j >= structure[l+1] || j < 0) {
			System.err.println("GET ACTIVATION ERROR: Node index out of bounds");
			return 0.0;
		}
		if(input == null) {
			System.err.println("GET ACTIVATION ERROR: No input");
			return 0.0;
		}
		if(input.length != structure[0]) {
			System.err.println("GET ACTIVATION ERROR: Input wrong shape");
			return 0.0;
		}
		if(l == -1)
			return input[j];
		return activations[l][j];
	}

	// Calculates the sigmoid of the value
	public static double sigmoid(double x) {
		return 1/(1+Math.exp(-x));
	}

	// Calculates the sigmoid of each value in the array
	private double[] sigmoid(double[] x) {
		double[] rv = new double[x.length];
		for(int i = 0; i < rv.length; i++) {
			rv[i] = sigmoid(x[i]);
		}
		return rv;
	}

	// Calculates the derivative of the sigmoid function at x
	private double sigmoidP(double x) {
		double sig = sigmoid(x);
		return sig*(1-sig);
	}

	// Calculates 1 / the sigmoid of x
	private double sigmoidInv(double x) {
		return Math.log(x/(1-x));
	}

	// Creates a random double array with the given dimensions
	private double[][] randArr(int h, int w){
		double[][] rv = new double[h][w];
		for(int i = 0; i < h; i++)
			rv[i] = randArr(rv[i].length);
		return rv;
	}

	// Creates a random double array with the given length
	private double[] randArr(int len) {
		double[] rv = new double[len];
		for(int i = 0; i < rv.length; i++)
			rv[i] = Math.random()*2-1;
		return rv;
	}

	// Creates an empty 3D weight array with the given structure
	private double[][][] emptyWArr(int[] struc){
		double[][][] rv = new double[struc.length-1][][];
		for(int i = 0; i < struc.length-1; i++) {
			rv[i] = new double[struc[i+1]][struc[i]];
		}
		return rv;
	}

	// Creates an empty 2D bias array with the given structure
	private double[][] emptyBArr(int[] struc){
		double[][] rv = new double[struc.length-1][];
		for(int i = 0; i < struc.length-1; i++) {
			rv[i] = new double[struc[i+1]];
		}
		return rv;
	}

	public LabelingMethod getLabelingMethod() {
		return labelingMethod;
	}

	public double getLearningRate() {
		return learningRate;
	}

	public int getMiniumDepthOfData() {
		return miniumDepthOfData;
	}

	public boolean isLearnFromOwnData() {
		return learnFromOwnData;
	}

	public void setLabelingMethod(LabelingMethod labelingMethod) {
		this.labelingMethod = labelingMethod;
	}

	public void setLearningRate(double learningRate) {
		this.learningRate = learningRate;
	}

	public void setMiniumDepthOfData(int miniumDepthOfData) {
		this.miniumDepthOfData = miniumDepthOfData;
	}

	public void setLearnFromOwnData(boolean learnFromOwnData) {
		this.learnFromOwnData = learnFromOwnData;
	}

	// Finds models and returns ObservableList
	public static ObservableList getModelList(){

		ObservableList<String> models = FXCollections.observableArrayList();
		URL modelsURL = NN.class.getResource("../resources/models");
		if(modelsURL != null) {
			File modelDirectory = new File(modelsURL.toString().substring(5));

			if (modelDirectory != null && modelDirectory.isDirectory()) {
				System.out.println(Arrays.toString(modelDirectory.list()));
				for (String filepath: modelDirectory.list()) {
					if(filepath.endsWith(".txt")){
						models.add(filepath.replace(".txt", ""));
					}
				}
			}
		}
		return models;
	}

	// Returns description of structure
	public String getStructure(){
		if(structure.length == 0) return "--";

		String rv = String.format("%d", structure[0]);
		for(int i = 1; i < structure.length; i++){
			rv += String.format(" x %d",structure[i]);
		}
		return rv;
	}

	public int getTrainingCursor() {
		return trainingCursor;
	}

	public void setTrainingCursor(int trainingCursor) {
		this.trainingCursor = trainingCursor;
	}

}
