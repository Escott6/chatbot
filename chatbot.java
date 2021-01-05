import java.util.*;
import java.io.*;

public class Chatbot {
	private static String filename = "./WARC201709_wid.txt";

	private static ArrayList<Integer> readCorpus() {
		ArrayList<Integer> corpus = new ArrayList<Integer>();
		try {
			File f = new File(filename);
			Scanner sc = new Scanner(f);
			while (sc.hasNext()) {
				if (sc.hasNextInt()) {
					int i = sc.nextInt();
					corpus.add(i);
				} else {
					sc.next();
				}
			}
		} catch (FileNotFoundException ex) {
			System.out.println("File Not Found.");
		}
		return corpus;
	}

	static public void main(String[] args) {
		ArrayList<Integer> corpus = readCorpus();
		int flag = Integer.valueOf(args[0]);

		if (flag == 100) {
			int w = Integer.valueOf(args[1]);
			int count = 0;
			for (int word : corpus) {
				if (w == word) {
					count++;
				}
			}
			System.out.println(count);
			System.out.println(String.format("%.7f", count / (double) corpus.size()));
		} else if (flag == 200) {
			double n1 = Integer.valueOf(args[1]);
			double n2 = Integer.valueOf(args[2]);
			double r = (n1 / n2);
			SuccessorWord newWord = unigram(corpus, r);
			System.out.println(newWord.getWord());
			System.out.println(String.format("%.7f", newWord.getIntervalLoc() - newWord.getProb()));
			System.out.println(String.format("%.7f", newWord.getIntervalLoc()));
		} else if (flag == 300) {
			int h = Integer.valueOf(args[1]);
			int w = Integer.valueOf(args[2]);
			int count = 0;
			ArrayList<Integer> words_after_h = new ArrayList<Integer>();
			for (int i = 0; i < corpus.size() - 1; i++) {
				if (h == corpus.get(i)) {
					if (corpus.get(i + 1) == w) {
						count++;
					}
					words_after_h.add(corpus.get(i + 1));
				}
			}
			System.out.println(count);
			System.out.println(words_after_h.size());
			System.out.println(String.format("%.7f", count / (double) words_after_h.size()));
		} else if (flag == 400) {
			double n1 = Integer.valueOf(args[1]);
			double n2 = Integer.valueOf(args[2]);
			double h = Integer.valueOf(args[3]);
			double r = (n1 / n2);
			SuccessorWord newWord = bigram(corpus, r, h);
			System.out.println(newWord.getWord());
			System.out.println(String.format("%.7f", newWord.getIntervalLoc() - newWord.getProb()));
			System.out.println(String.format("%.7f", newWord.getIntervalLoc()));
		} else if (flag == 500) {
			double h1 = Integer.valueOf(args[1]);
			double h2 = Integer.valueOf(args[2]);
			double w = Integer.valueOf(args[3]);
			int count = 0;
			ArrayList<Integer> words_after_h1h2 = new ArrayList<Integer>();
			// finds all successive words and their counts(after H)
			for (int i = 0; i < corpus.size() - 2; i++) {
				if (h1 == corpus.get(i) && h2 == corpus.get(i + 1)) {
					words_after_h1h2.add(corpus.get(i + 2));
					if (corpus.get(i + 2) == w) {
						count++;
					}
				}
			}
			System.out.println(count);
			System.out.println(words_after_h1h2.size());
			if (words_after_h1h2.size() == 0)
				System.out.println("undefined");
			else
				System.out.println(String.format("%.7f", count / (double) words_after_h1h2.size()));
		} else if (flag == 600) {
			double n1 = Integer.valueOf(args[1]);
			double n2 = Integer.valueOf(args[2]);
			double h1 = Integer.valueOf(args[3]);
			double h2 = Integer.valueOf(args[4]);
			double r = (n1 / n2);
			SuccessorWord newWord = trigram(corpus, r, h1, h2);
			if (newWord != null) {
				System.out.println(newWord.getWord());
				System.out.println(String.format("%.7f", newWord.getIntervalLoc() - newWord.getProb()));
				System.out.println(String.format("%.7f", newWord.getIntervalLoc()));
			}
		} else if (flag == 700) {
			int seed = Integer.valueOf(args[1]);
			int t = Integer.valueOf(args[2]);
			double h1 = 0;
			double h2 = 0;
			Random rng = new Random();
			if (seed != -1)
				rng.setSeed(seed);

			if (t == 0) {
				// Generates first word using r
				double r = rng.nextDouble();
				SuccessorWord newWord1 = unigram(corpus, r);
				h1 = newWord1.getWord();
				System.out.println(newWord1.getWord());
				if (h1 == 9 || h1 == 10 || h1 == 12) {
					return;
				}
				// Generates second word using r
				r = rng.nextDouble();
				SuccessorWord newWord2 = bigram(corpus, r, (double) newWord1.getWord());
				h2 = newWord2.getWord();
				System.out.println(newWord2.getWord());
			} else if (t == 1) {
				h1 = Integer.valueOf(args[3]);
				double r = rng.nextDouble();
				SuccessorWord newWord2 = bigram(corpus, r, h1);
				h2 = newWord2.getWord();
				System.out.println(newWord2.getWord());
			} else if (t == 2) {
				h1 = Integer.valueOf(args[3]);
				h2 = Integer.valueOf(args[4]);
			}

			while (h2 != 9 && h2 != 10 && h2 != 12) {
				double r = rng.nextDouble();
				SuccessorWord newWord3 = trigram(corpus, r, h1, h2);
				System.out.println(newWord3.getWord());
				h1 = h2;
				h2 = newWord3.getWord();
			}
		}
		return;
	}

	public static SuccessorWord unigram(ArrayList<Integer> corpus, double random) {
		double currentLoc = 0;
		int count[] = new int[4700];
		double[] probability = new double[4700];
		for (int i = 0; i < corpus.size(); i++) {
			count[corpus.get(i)] += 1;
		}
		for (int i = 0; i < count.length; i++) {
			probability[i] = (count[i] / (double) corpus.size());
		}
		for (int i = 0; i < probability.length; i++) {
			currentLoc += probability[i];
			if (currentLoc >= random) {
				SuccessorWord w = new SuccessorWord(i, currentLoc, probability[i]);
				return w;
			}
		}
		return null;
	}

	public static SuccessorWord bigram(ArrayList<Integer> corpus, double random, double h1) {
		double currentLoc = 0;
		int countH = 0;
		double secondCount[] = new double[4700];
		// finds all successive words and their counts(after H)
		for (int i = 0; i < corpus.size() - 1; i++) {
			if (h1 == corpus.get(i)) {
				countH++;
				secondCount[corpus.get(i + 1)] += 1;
			}
		}
		// gets the probability of each (w|h)
		double[] probability = new double[secondCount.length];
		int probCount = 0;
		for (int i = 0; i < secondCount.length; i++) {
			probability[probCount] = (secondCount[i] / countH);
			probCount++;
		}
		for (int i = 0; i < probability.length; i++) {
			currentLoc += probability[i];
			if (currentLoc >= random) {
				SuccessorWord w = new SuccessorWord(i, currentLoc, probability[i]);
				return w;
			}
		}
		return null;
	}

	private static SuccessorWord trigram(ArrayList<Integer> corpus, double r, double h1, double h2) {
		double currentLoc = 0;
		double secondCount[] = new double[4700];
		int wordCount = 0;
		ArrayList<Integer> words_after_h1h2 = new ArrayList<Integer>();
		for (int i = 0; i < corpus.size() - 2; i++) {
			if (h1 == corpus.get(i) && h2 == corpus.get(i + 1)) {
				words_after_h1h2.add(corpus.get(i + 2));
				secondCount[corpus.get(i + 2)] += 1;
				wordCount++;
			}
		}
		if (words_after_h1h2.size() == 0) {
			System.out.println("undefined");
		}
		double[] probability = new double[secondCount.length];
		int probCount = 0;
		for (int i = 0; i < secondCount.length; i++) {
			probability[probCount] = (secondCount[i] / wordCount);
			probCount++;
		}
		for (int i = 0; i < probability.length; i++) {
			currentLoc += probability[i];
			if (currentLoc >= r) {
				SuccessorWord w = new SuccessorWord(i, currentLoc, probability[i]);
				return w;
			}
		}
		return null;
	}

}

class SuccessorWord {
	static int word;
	static int count;
	static double intervalLoc;
	static double probability;

	public SuccessorWord(int word) {
		this.word = word;
	}

	public SuccessorWord(int word, double interLoc, double prob) {
		this.word = word;
		this.intervalLoc = interLoc;
		this.probability = prob;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setProb(double prob) {
		this.probability = prob;
	}

	public void setIntervalLoc(double loc) {
		this.intervalLoc = loc;
	}

	public int getWord() {
		return word;
	}

	public double getIntervalLoc() {
		return intervalLoc;
	}

	public int getCount() {
		return count;
	}

	public double getProb() {
		return probability;
	}
}