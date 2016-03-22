package bigram_Implementation;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * 
 * 
 */

/**
 * @author Anantha
 *
 */
public class NBTrain {

	// Naive Bayes Bigram Implementation

	private static HashMap<Reviews, HashMap<File, File>> D;
	private static HashMap<Reviews, Reviews> C;
	private static HashMap<String, HashMap<Reviews, Double>> vocabulary;
	private static double countDocs;
	private static double countPositiveDocs;
	private static double countNegativeDocs;
	private static double priorPositive;
	private static double priorNegative;
	private static double sumOfTermCountForPositive;
	private static double sumOfTermCountForNegative;
	private static TreeMap<Double, TreeMap<String, String>> positiveToNegative;
	private static TreeMap<Double, TreeMap<String, String>> negativeToPositive;


	// Pseudo code
	//	TrainMultinomialNB(C, D)
	//	C - set of positive and negative class NOT SURE
	//	D - Collection of text files
	//	V - extract vocabulary with (key, value) pair to collect unique words
	//	N - Size of D
	//	Nc (Nc positive and Nc negative) - Number of positive documents in D 
	//	and Number of negative documents in D
	//	prior[c] - probability of each class with respect total documents
	//	textc (textc positive and textc negative) - concatenate all the 
	//	text of a particular class from files in D
	//	Tct - count of every unique vocabulary word in textc
	//	condProb[t][c] - (Tct + 1)/∑(Tct + 1) , 
	//	Tct of each term in V divided by summation of all terms Tct. add 1 for laplace smoothing

	public static void readAndAddFileContent(String path, Reviews review){

		// collection of filenames to be trained from the given path

		File dir = new File(path);
		HashMap<File, File> reviews = new HashMap<File, File>();
		for (File file : dir.listFiles()) {

			reviews.put(file, file);

		}

		D.put(review, reviews);

	}

	public static void extractVocabularyAndTextAndTokenCount(){

		// method to calculate create vocabulary of words in the file and 
		// count their total occurrence (token count) in each type of file

		for(Reviews key : D.keySet()){
			HashMap<File, File> temp = D.get(key);
			for(File s : temp.keySet()){
				Scanner scanner;
				try{
					scanner = new Scanner(s);
					while(scanner.hasNextLine()){
						String token = "";
						int count = 0;
						String line1 = scanner.nextLine().replaceAll("&nbsp", ""); 
						String[] line = line1.split("\\s+");
						for(int i = 0; i<line.length; i++){
							token += line[i] + " ";
							count ++;
							if(count == 2){
								token.trim();
								if(vocabulary.containsKey(token)){
									if(key.equals(Reviews.NEGATIVE)){
										if(vocabulary.get(token).containsKey(Reviews.NEGATIVE)){
											double cnt = vocabulary.get(token).get(Reviews.NEGATIVE);
											vocabulary.get(token).put(Reviews.NEGATIVE, cnt+1);
											sumOfTermCountForNegative += 1;
										} else{
											vocabulary.get(token).put(Reviews.NEGATIVE, (double)1);
											sumOfTermCountForNegative += 2;
										}

									} else if(key.equals(Reviews.POSITIVE)){
										if(vocabulary.get(token).containsKey(Reviews.POSITIVE)){
											double cnt = vocabulary.get(token).get(Reviews.POSITIVE);
											vocabulary.get(token).put(Reviews.POSITIVE, cnt+1);
											sumOfTermCountForPositive += 1;
										}else{
											vocabulary.get(token).put(Reviews.POSITIVE, (double)1);
											sumOfTermCountForPositive += 2;
										}

									}
								} else {
									if(key.equals(Reviews.NEGATIVE)){
										vocabulary.put(token, new HashMap<Reviews, Double>());
										vocabulary.get(token).put(Reviews.NEGATIVE, (double)1);
										sumOfTermCountForNegative += 2;

									} else if(key.equals(Reviews.POSITIVE)){
										vocabulary.put(token, new HashMap<Reviews, Double>());
										vocabulary.get(token).put(Reviews.POSITIVE, (double)1);
										sumOfTermCountForPositive += 2;
									}
								}
								token = "";
								count = 0;
							}

						}
					}

				}
				catch(Exception e) {

					e.printStackTrace();

				}

			}

		}

	}

	public static void condProbability(){

		positiveToNegative = new TreeMap<Double, TreeMap<String, String>>();
		negativeToPositive = new TreeMap<Double, TreeMap<String, String>>();

		// elimination of fewer occurrence tokens 5 in this case
		for(Iterator<Map.Entry<String, HashMap<Reviews, Double>>> it = vocabulary.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, HashMap<Reviews, Double>> entry = it.next();
			double pos = 0;
			double neg = 0;
			if(entry.getValue().containsKey(Reviews.POSITIVE)){
				pos = entry.getValue().get(Reviews.POSITIVE);
			}

			if(entry.getValue().containsKey(Reviews.NEGATIVE)){
				neg = entry.getValue().get(Reviews.NEGATIVE);
			}

			if(pos + neg < 5 ) {
				sumOfTermCountForNegative -= (neg > 0) ? (neg + 1) : neg;
				sumOfTermCountForPositive -= (pos > 0) ? (pos + 1) : pos;
				it.remove();
			}
		}


		// calculation of conditional probability ((token count + 1) / ∑ (token count + 1))
		for(String s : vocabulary.keySet()){

			double tempCountPositive = 0;
			double tempCountNegative = 0;

			if(vocabulary.get(s).containsKey(Reviews.POSITIVE)){
				tempCountPositive = vocabulary.get(s).get(Reviews.POSITIVE);
			}

			if(vocabulary.get(s).containsKey(Reviews.NEGATIVE)){
				tempCountNegative = vocabulary.get(s).get(Reviews.NEGATIVE);
			}

			double condProbPositive = (tempCountPositive + 1)/sumOfTermCountForPositive;
			double condProbNegative = (tempCountNegative + 1)/sumOfTermCountForNegative;

			vocabulary.get(s).put(Reviews.POSITIVE, condProbPositive);
			vocabulary.get(s).put(Reviews.NEGATIVE, condProbNegative);

			//double tempPosToNeg = Math.log(tempCountPositive + 1/countPositiveDocs) - 
			//	Math.log(tempCountNegative + 1/countNegativeDocs);

			//double tempNegToPos = Math.log(tempCountNegative + 1/countNegativeDocs) - 
			//		Math.log(tempCountPositive + 1/countPositiveDocs);

			// log ratio calculation log(P(W|Pos)/P(W|Neg)),  log(condProbPos/condProbNeg)
			double tempPosToNeg = Math.log(condProbPositive) - Math.log(condProbNegative);

			// log ratio calculation log(P(W|Neg)/P(W|Pos)),  log(condProbNeg/condProbPos)
			double tempNegToPos = Math.log(condProbNegative) - Math.log(condProbPositive);

			// 20 highest (log) ratio of positive to negative weight.
			if(positiveToNegative.size() == 20){

				if(tempPosToNeg > positiveToNegative.firstKey()){

					if(!(positiveToNegative.containsKey(tempPosToNeg))){

						positiveToNegative.remove(positiveToNegative.firstKey());
						positiveToNegative.put(tempPosToNeg, new TreeMap<String, String>());
						positiveToNegative.get(tempPosToNeg).put(s, s);

					} else {

						if(positiveToNegative.get(tempPosToNeg).containsKey(s)){

						} else {

							TreeMap<String, String> temp = positiveToNegative.get(tempPosToNeg);
							temp.put(s, s);
							positiveToNegative.put(tempPosToNeg, temp);

						}

					}

				}

			} else {
				if(!(positiveToNegative.containsKey(tempPosToNeg))){

					positiveToNegative.put(tempPosToNeg, new TreeMap<String, String>());
					positiveToNegative.get(tempPosToNeg).put(s, s);

				}

			}

			// 20 highest (log) ratio of negative to positive weight.
			if(negativeToPositive.size() == 20){

				if(tempNegToPos > negativeToPositive.firstKey()){

					if(!(negativeToPositive.containsKey(tempNegToPos))){

						negativeToPositive.remove(negativeToPositive.firstKey());
						negativeToPositive.put(tempNegToPos, new TreeMap<String, String>());
						negativeToPositive.get(tempNegToPos).put(s, s);

					} else {

						if(negativeToPositive.get(tempNegToPos).containsKey(s)){

						} else {

							TreeMap<String, String> temp = negativeToPositive.get(tempNegToPos);
							temp.put(s, s);
							negativeToPositive.put(tempNegToPos, temp);

						}

					}

				}

			} else {
				if(!(negativeToPositive.containsKey(tempNegToPos))){

					negativeToPositive.put(tempNegToPos, new TreeMap<String, String>());
					negativeToPositive.get(tempNegToPos).put(s, s);

				}

			}
		} 

	}

	public HashMap<String, Object> trainMultinomialNB(String s){

		C = new HashMap<Reviews, Reviews>();
		C.put(Reviews.POSITIVE, Reviews.POSITIVE);
		C.put(Reviews.NEGATIVE, Reviews.NEGATIVE);
		D = new HashMap<Reviews, HashMap<File, File>>();

		// Prepare C and D to pass to the function trainMultinomialNB(C, D)
		String[] paths = s.split(",");
		readAndAddFileContent(paths[0], Reviews.NEGATIVE);
		readAndAddFileContent(paths[1], Reviews.POSITIVE);

		// Collect the unique words from positive and negative reviews to create vocabulary
		// and text concatenation in each class and token count of each term in each
		// review type
		vocabulary = new HashMap<String, HashMap<Reviews,Double>>();
		extractVocabularyAndTextAndTokenCount();

		// total number of documents and documents in both postive and negative
		countDocs = D.get(Reviews.NEGATIVE).size() + D.get(Reviews.POSITIVE).size();
		countNegativeDocs = D.get(Reviews.NEGATIVE).size();
		countPositiveDocs = D.get(Reviews.POSITIVE).size();

		// calculate prior value for positive and negative reviews
		priorNegative = countNegativeDocs/countDocs;
		priorPositive = countPositiveDocs/countDocs;

		// Replacing count of terms in positive and negative to condProbability of positive
		// and Negative
		condProbability();
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("C", C);
		result.put("Vocabulary", vocabulary);
		result.put("PriorNegative", priorNegative);
		result.put("PriorPositive", priorPositive);
		result.put("PositiveToNegative", positiveToNegative);
		result.put("NegativeToPositive", negativeToPositive);

		System.out.println(countDocs);
		System.out.println(countPositiveDocs);
		System.out.println(countNegativeDocs);

		System.out.println(positiveToNegative);
		System.out.println(negativeToPositive);

		return result;

	}

}
