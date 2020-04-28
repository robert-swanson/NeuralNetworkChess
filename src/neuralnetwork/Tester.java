package neuralnetwork;

import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;


public class Tester {

	static NN currentNN;
	static LinkedList<Image> images;
	static Scanner in;


	public static void main(String[] args) {
		currentNN = new NN("Best.txt");
		images = Image.getTestImageList();
		in = new Scanner(System.in);
		System.out.println("Welcome to the NN CLI, type \"help\" to list commands");
		do{
		    System.out.flush();
			String[] input = in.nextLine().split(" ");
			switch (input[0].toLowerCase()){
				case "":
					continue;
				case "train":
					int batchSize = 100;
					double learningRate = 0.2;
					if(input.length > 1) batchSize = Integer.parseInt(input[1]);
					if(input.length > 2) learningRate = Double.parseDouble(input[2]);

					currentNN.infiniteTrain(batchSize, learningRate);
					break;
				case "evaluate":
					double[][] stats = currentNN.testWithImages(images);
					System.out.printf("Overall Accuracy: %-2.3f%%\n", stats[0][0]*100);
					printCertainty(stats[1]);
					break;
				case "browse":
					do{
						testRandomImage();
						if(in.nextLine().equalsIgnoreCase("stop")) break;
					}while(in.hasNextLine());
					break;
				case "real":
					while(in.hasNextLine()){
						input = in.nextLine().split(" ");
						if(input[0].equalsIgnoreCase("stop")) break;
						else if(input[0].equalsIgnoreCase("all")) {
							File dir = new File("assets/images/");
							File[] directoryListing = dir.listFiles();
							if (directoryListing != null) {
								int count = 0, total = 0;
								for (File child : directoryListing) {
									System.out.printf("\nTesting File: %s\n", child.getName());
									int label = child.getName().charAt(1)-'0';
									try{
										int guess = testRealImage(child.getName(), false);
										if(label >= 0 && label <= 9){
											total++;
											count += guess == label ? 1 : 0;
										}
									}catch (Exception e) {
										System.out.println("ERROR: " + e.toString());
									}
								}
								System.out.printf("Total Accuracy: %d correct out of %d tries = %.3f%% accurate\n", count,total, (double)count/total*100);
							}
						} else {
							try{
								testRealImage(input[0], true);
							} catch (Exception e) {
								System.err.println(e.getMessage());
							}
						}
					}
					break;
				case "help":
					System.out.printf("train:\t\tloads the current network and continues to train it\nevaluate:\ttests the current network and prints out it accuracy\nbrowse:\t\tlook at random test images one by one\nreal:\t\tload images by filename and classify them\n\n");
					break;
				case "graph":
					currentNN.printGraph();
					break;
				case "printb":
					currentNN.printBiases();
					break;
				case "printw":
					currentNN.printWeights();
					break;
				case "printa":
					currentNN.printActivations();
					break;

				default:
					System.err.printf("Unrecognized command %s\n", input[0]);

			}

		}while(in.hasNextLine());
	}


	// Overload Prints
	public static void print(double[] a) {
		System.out.print("[");
		for (int i = 0; i < a.length - 1; i++)
			System.out.print(a[i] + ", ");
		System.out.println(a[a.length - 1] + "]");
	}

	public static void print(int[] a) {
		System.out.print("[");
		for (int i = 0; i < a.length - 1; i++)
			System.out.print(a[i] + ", ");
		System.out.println(a[a.length - 1] + "]");
	}

	// Fetches and classifies the 28x28 image with the given filename
	public static int testRealImage(String filename, boolean verbose) throws Exception {
		Image image = new Image(filename,-1);
		if(verbose) image.roughPrint();
		int guess = currentNN.classify(image);
		System.out.printf("Guess: %d\n", guess);
		if(verbose) printCertainty(currentNN.getOutput());
		return guess;
	}

	public static void testRandomImage() {
		Image image = images.get((int)(Math.random()*images.size()));
		image.roughPrint();
		System.out.printf("Real: %d Guess: %d\n",image.label, currentNN.classify(image));
		printCertainty(currentNN.getOutput());
	}

	private static void printCertainty(double[] certainties){
		double minStars = 1;
		double maxStars = 0;
		for(double d: certainties) {
			minStars = Math.min(minStars, d);
			maxStars = Math.max(maxStars, d);
		}
		for(int d = 0; d < 10; d++) {
			double val = (certainties[d]-minStars)/(maxStars-minStars)*10;
			System.out.printf("%d: %2.0f%%:",d,certainties[d]*100);
			for(int ij = 0; ij < val; ij++) {
				System.out.print("*");
			}
			System.out.println();
		}
	}
}
