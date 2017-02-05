
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

/**
 * 
 * @author SHURUI
 *
 */
public class Outcast {
	private WordNet wordnet;

	/**
	 * constructor takes a WordNet object
	 * 
	 * @param wordnet
	 *            input file
	 */
	public Outcast(WordNet wordnet) {
		this.wordnet = wordnet;
	}

	/**
	 * given an array of WordNet nouns, return an outcast
	 * 
	 * @param nouns
	 *            several nouns as strings
	 * @return max distance from other nouns
	 */
	public String outcast(String[] nouns) {
		int outcastMaxStringIndex = -1;
		int outcastMaxDist = 0;
		for (int i = 0; i < nouns.length; i++) {
			int outcastDist = 0;
			for (int j = 0; j < nouns.length; j++)
				if (i != j) {
					outcastDist += wordnet.distance(nouns[i], nouns[j]);
				}
			if (outcastDist > outcastMaxDist) {
				outcastMaxDist = outcastDist;
				outcastMaxStringIndex = i;
			}
		}
		return nouns[outcastMaxStringIndex];
	}

	/**
	 * see test client below
	 * 
	 * @param args
	 *            input files
	 */
	public static void main(String[] args) {
		WordNet wordnet = new WordNet(args[0], args[1]);
		Outcast outcast = new Outcast(wordnet);
		for (int t = 2; t < args.length; t++) {
			In in = new In(args[t]);
			String[] nouns = in.readAllStrings();
			StdOut.println(args[t] + ": " + outcast.outcast(nouns));
		}
	}
}