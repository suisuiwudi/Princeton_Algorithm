package wordNet;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.DirectedCycle;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

public class WordNet {
	private SAP sap;
	private Digraph graph;
	private Map<Integer, String> idToNounsWithSpace;
	private Map<String, Set<Integer>> nounToIds;

	// constructor takes the name of the two input files
	public WordNet(String synsets, String hypernyms) {
		idToNounsWithSpace = new HashMap<>();
		nounToIds = new HashMap<>();
		createWordConnection(synsets);
		createGraph(hypernyms);
		sap = new SAP(this.graph);
	}

	private void createWordConnection(String synsets) {
		In synFile = new In(synsets);
		while (synFile.hasNextLine()) {
			String[] line = synFile.readLine().split(",");
			int id = Integer.parseInt(line[0]);
			idToNounsWithSpace.put(id, line[1]);
			String[] nouns = line[1].split(" ");
			for (String noun : nouns) {
				Set<Integer> ids = new HashSet<>();
				if (nounToIds.containsKey(noun)) {
					ids = nounToIds.get(noun);
				}
				ids.add(id);
				nounToIds.put(noun, ids);
			}
		}
	}

	private void createGraph(String hypernyms) {
		In hyperFile = new In(hypernyms);
		this.graph = new Digraph(idToNounsWithSpace.size() + 1);
		while (hyperFile.hasNextLine()) {
			String[] line = hyperFile.readLine().split(",");
			for (int i = 1; i < line.length; i++) {
				// System.out.println(line[0]+' '+line[i]);
				this.graph.addEdge(Integer.parseInt(line[0]), Integer.parseInt(line[i]));
			}
		}
		DirectedCycle cycle = new DirectedCycle(this.graph);
		if (cycle.hasCycle()) {
			throw new IllegalArgumentException("Input has a cycle");
		}
		if (!oneRoot(this.graph)) {
			throw new IllegalArgumentException("Input has mutible roots");
		}

	}

	private boolean oneRoot(Digraph graph) {
		int roots = 0;
		for (int i = 0; i < graph.V(); i++) {
			if (graph.outdegree(i) == 0 && this.idToNounsWithSpace.containsKey(i)) {
				roots++;
				if (roots > 1) {
					return false;
				}
			}

		}
		return roots == 1;
	}

	// returns all WordNet nouns
	public Iterable<String> nouns() {
		return nounToIds.keySet();
	}

	// is the word a WordNet noun?
	public boolean isNoun(String word) {
		if (word == null) {
			throw new NullPointerException();
		}
		if (word == "") {
			throw new IllegalArgumentException();
		}
		return nounToIds.containsKey(word);
	}

	// distance between nounA and nounB (defined below)
	public int distance(String nounA, String nounB) {
		isNoun(nounA);
		isNoun(nounB);
		return sap.length(nounToIds.get(nounA), nounToIds.get(nounB));

	}

	// a synset (second field of synsets.txt) that is the common ancestor of
	// nounA and nounB
	// in a shortest ancestral path (defined below)
	public String sap(String nounA, String nounB) {
		isNoun(nounA);
		isNoun(nounB);
		return idToNounsWithSpace.get(sap.ancestor(nounToIds.get(nounA), nounToIds.get(nounB)));
	}

	// do unit testing of this class
	// public static void main(String[] args)
}