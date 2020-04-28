package neuralnetwork;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Defines an individual image of a digit and it label
 */
public class Image {
	/**
	 * The pixel values of the image
	 */
	final int[][] data;
	/**
	 * The integer value of the digit shown in the image
	 */
	final int label;

	final int finalLength = 28;
	static LinkedList<Image> trainingImages = new LinkedList<>();
	static LinkedList<Image> testingImages = new LinkedList<>();

	/**
	 * Constructs image with 2D data
	 * @param imageData 2D array containing grayscale pixel values
	 * @param label integer value represented by the image
	 */
	public Image(int[][] imageData, int label) {
		data = imageData;
		this.label = label;
	}

	/**
	 * Constructs image given 1D data for 28x28 image and label
	 * @param imageData 1D array containing 784 pixel values
	 * @param label integer value represented by the image
	 */
	public Image(int[] imageData, int label) {
		data = new int[28][28];
		int i = 0;
		for(int y = 0; y < 28; y++) {
			for(int x = 0; x < 28; x++)
				data[y][x] = imageData[i++];
		}
		this.label = label;
	}

	public Image(String filename, int label) throws Exception {
		this.label = label;
		data = new int[finalLength][finalLength];
		BufferedImage img;
		try {
			img = ImageIO.read(new File("assets/images/" + filename));
			if (img == null) throw new FileNotFoundException();
		} catch (IOException e) {
			throw e;
		}

        if (img.getWidth() != img.getHeight()) {
			String message = String.format("unable to load file at assets/images/%s: image must be square",filename);
			throw new Exception(message);
		}

		// Resize image
		int length = img.getWidth();
		int[] rgbPixels = new int[length * length * 4];
		int[] grayscalePixels = new int[length * length];
		img.getRaster().getPixels(0, 0, length, length, rgbPixels);
		for(int i = 0; i < rgbPixels.length; i+=4){
			grayscalePixels[i/4] = getGrayScale(rgbPixels[i], rgbPixels[i+1], rgbPixels[i+2]);
		}

		if (length >= finalLength || length < finalLength) { // Making smaller or no change or any case really
			double multiplier = (double)length/finalLength;
			for(int finalY = 0; finalY < finalLength; finalY++) {
				for(int finalX = 0; finalX < finalLength; finalX++) {
					double pixel = 0.0;
					int total = 0;
				    for(int averageY = (int)(finalY*multiplier); (averageY < (int)((finalY+1)*multiplier) && (averageY < length)); averageY++) {
						for(int averageX = (int)(finalX*multiplier); (averageX < (int)((finalX+1)*multiplier) && (averageX < length)); averageX++) {
							pixel += grayscalePixels[xyToIndex(averageX, averageY, length)];
							total++;
						}
					}
					data[finalY][finalX] = (int)(pixel / total);
				}
			}
		}

	}

	private static int xyToIndex(int x, int y, int length){
		return x + y * length;
	}

	/**
	 * Prints image using symbols for pixels
	 */
	public void roughPrint() {
		for(int y = 0; y < data.length; y++) {
			for(int x = 0; x < data.length; x++) {
				int n = data[y][x];
				char c = ' ';
				if(n > 250) c = '•';
				else if(n > 200) c = '#';
				else if(n > 150) c = '*';
				else if(n > 100) c = '-';
				else if(n > 50) c = '.';
				System.out.print(c);
			}
			System.out.println();
		}
	}

	/**
	 * Converts an RGB pixel value to grayscale
	 * @param r Red value
	 * @param g Green value
	 * @param b Blue value
	 * @return Greyscale value
	 */
	public static int getGrayScale(int r, int g, int b) {
		return 255-(int)(r * 0.299 + g * 0.587 + b * 0.114);
	}

	/**
	 * Gets the one dimensional data for the image
	 * @return a double array containing the pixel values
	 */
	public double[] getInput() {
		double[] rv = new double[784];
		int i = 0;
		for(int y = 0; y < data.length; y++) {
			for(int x = 0; x < data[y].length; x++) {
				rv[i++] = data[y][x];
			}
		}
		return rv;
	}

	/**
	 * Returns the output layer based off of the label
	 * @return a double[10] where arr[n] = 1.0 where n is the value of the label
	 */
	public double[] getOutput() {
		double[] rv = new double[10];
		rv[label] = 1;
		return rv;
	}

	/**
	 * Loads the training data from file
	 * @return a linked list containing the training images
	 */
	public static LinkedList<Image> getTrainingImageList(){
		if(trainingImages.isEmpty()) {
			int[] labels = MnistReader.getLabels("assets/dataset/training labels");
			List<int[][]> imageData = MnistReader.getImages("assets/dataset/training data");
			trainingImages = getImageList(labels, imageData);
		}
		return trainingImages;
	}

	/**
	 * Loads the testing data from file
	 * @return a linked list containing the testing images
	 */
	public static LinkedList<Image> getTestImageList(){
	    if(testingImages.isEmpty()){
			int[] labels = MnistReader.getLabels("assets/dataset/test labels");
			List<int[][]> imageData = MnistReader.getImages("assets/dataset/test data");
			testingImages = getImageList(labels, imageData);
		}
	    return testingImages;
	}

	/**
	 * Constructs a list of images based off the provided list of data and labels
	 * @param labels an integer array containing the labels of the images in order
	 * @param imageData a list containing the image data in order
	 * @return a linked list containing the images
	 */
	public static LinkedList<Image> getImageList(int[] labels, List<int[][]> imageData){
		assert labels.length == imageData.size();
		assert imageData.get(0).length == 28 && imageData.get(0)[0].length == 28;
		LinkedList<Image> rv = new LinkedList<>();
		for(int i = 0; i < labels.length; i++)
			rv.add(new Image(imageData.get(i), labels[i]));
		return rv;
	}
}
