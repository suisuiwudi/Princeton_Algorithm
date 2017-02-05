package wordNet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.princeton.cs.algs4.BreadthFirstDirectedPaths;
import edu.princeton.cs.algs4.Digraph;

public class SAP {
	private Digraph graph;
	private HashMap<String, ProcessDist> result;

	/**
	 * constructor takes a digraph (not necessarily a DAG)
	 * 
	 * @param G
	 *            DAG
	 */
	public SAP(Digraph G) {
		this.graph = G;
		this.result = new HashMap<String, ProcessDist>();
	}

	private boolean validV(Iterable<Integer> v) {
		for (int i : v) {
			if (i < 0 || i >= this.graph.V())
				return false;
		}
		return true;
	}

	/**
	 * length of shortest ancestral path between v and w; -1 if no such path
	 * 
	 * @param v
	 * @param w
	 * @return
	 */
	public int length(int v, int w) {
		List<Integer> vTemp = new ArrayList<Integer>();
		List<Integer> wTemp = new ArrayList<Integer>();
		vTemp.add(v);
		wTemp.add(w);
		return getResult(vTemp, wTemp).distance;
	}

	/**
	 * a common ancestor of v and w that participates in a shortest ancestral
	 * path; -1 if no such path
	 * 
	 * @param v
	 * @param w
	 * @return
	 */
	public int ancestor(int v, int w) {
		List<Integer> vTemp = new ArrayList<Integer>();
		List<Integer> wTemp = new ArrayList<Integer>();
		vTemp.add(v);
		wTemp.add(w);
		return getResult(vTemp, wTemp).ancestor;
	}

	/**
	 * length of shortest ancestral path between any vertex in v and any vertex
	 * in w; -1 if no such path
	 * 
	 * @param v
	 * @param w
	 * @return
	 */
	public int length(Iterable<Integer> v, Iterable<Integer> w) {
		return getResult(v, w).distance;
	}

	/**
	 * a common ancestor that participates in shortest ancestral path; -1 if no
	 * such path
	 * 
	 * @param v
	 * @param w
	 * @return
	 */
	public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
		return getResult(v, w).ancestor;
	}

	/**
	 * 
	 * @param v
	 * @param w
	 * @return
	 */

	private ProcessDist getResult(Iterable<Integer> v, Iterable<Integer> w) {
		if (!validV(v) || !validV(w))
			throw new ArrayIndexOutOfBoundsException();
		String key = v.toString() + "-" + w.toString();
		if (result.containsKey(key)) {
			ProcessDist ans = result.get(key);
			return ans;
		}
		ProcessDist ans = new ProcessDist(v, w);
		result.put(key, ans);
		return ans;
	}

	/**
	 * 
	 * @author SHURUI
	 *
	 */
	private class ProcessDist {
		private int ancestor;
		private int distance;

		public ProcessDist(Iterable<Integer> v, Iterable<Integer> w) {
			BreadthFirstDirectedPaths vBFS = new BreadthFirstDirectedPaths(graph, v);
			BreadthFirstDirectedPaths wBFS = new BreadthFirstDirectedPaths(graph, w);
			process(vBFS, wBFS);
		}

		public void process(BreadthFirstDirectedPaths vBFS, BreadthFirstDirectedPaths wBFS) {
			List<Integer> ancestors = new ArrayList<>();
			for (int i = 0; i < graph.V(); i++) {
				if (vBFS.hasPathTo(i) && wBFS.hasPathTo(i)) {
					ancestors.add(i);
				}
			}
			distance = Integer.MAX_VALUE;
			ancestor = -1;

			for (int i : ancestors) {
				int dist = vBFS.distTo(i) + wBFS.distTo(i);
				if (dist < distance) {
					distance = dist;
					ancestor = i;
				}
			}
			if (distance == Integer.MAX_VALUE) {
				distance = -1;
			}

		}

	}

}
