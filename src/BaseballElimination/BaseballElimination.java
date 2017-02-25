package BaseballElimination;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.princeton.cs.algs4.*;

public class BaseballElimination {
	private int firstLayerNums;
	private int secondLayerNums;
	private int teamNums;
	private int[] wins;
	private int[] losses;
	private int[] remaining;
	private int[][] games;
	private boolean[] isEliminated;
	private String[] teamName;
	private HashMap<String, Integer> teamToIds;
	private HashMap<String, List<String>> certificateOfElimination;

	public BaseballElimination(String filename) throws IOException {
		// create a baseball division from given filename in format specified
		// below
		In in = new In(filename);
		certificateOfElimination = new HashMap<>();
		teamNums = in.readInt();
		teamName = new String[teamNums];
		wins = new int[teamNums];
		losses = new int[teamNums];
		remaining = new int[teamNums];
		games = new int[teamNums][teamNums];
		teamToIds = new HashMap<>();
		firstLayerNums = (teamNums - 1) * (teamNums - 2) / 2;
		secondLayerNums = (teamNums - 1);
		for (int i = 0; i < teamNums; i++) {
			String read ="";
			read = in.readString();
			teamToIds.put(read, i);
			teamName[i] = read;
			wins[i] = Integer.parseInt(in.readString());
			losses[i] = Integer.parseInt(in.readString());
			remaining[i] = Integer.parseInt(in.readString());
			for (int j = 0; j < teamNums; j++) {
				games[i][j] = Integer.parseInt(in.readString());
			}

		}
		isEliminated = new boolean[teamNums];
		for (int i = 0; i < teamNums; i++) {
			isEliminated[i] = checkElimination(i);
		}

	}

	public int numberOfTeams() {
		return teamNums;
		// number of teams
	}

	public Iterable<String> teams() {
		// all teams
		return teamToIds.keySet();
	}

	public int wins(String team) {
		if (!teamToIds.containsKey(team)) throw new IllegalArgumentException("No such a team");
		// number of wins for given team
		return wins[teamToIds.get(team)];
	}

	public int losses(String team) {
		if (!teamToIds.containsKey(team)) throw new IllegalArgumentException("No such a team");
		// number of losses for given team
		return losses[teamToIds.get(team)];
	}

	public int remaining(String team) {
		if (!teamToIds.containsKey(team)) throw new IllegalArgumentException("No such a team");
		// number of remaining games for given team
		return remaining[teamToIds.get(team)];
	}

	public int against(String team1, String team2) {
		if (!teamToIds.containsKey(team1)) throw new IllegalArgumentException("No such a team");
		if (!teamToIds.containsKey(team2)) throw new IllegalArgumentException("No such a team");
		// number of remaining games between team1 and team2
		return games[teamToIds.get(team1)][teamToIds.get(team2)];
	}

	public boolean isEliminated(String team) {
		if (!teamToIds.containsKey(team)) throw new IllegalArgumentException("No such a team");
		// is given team eliminated?
		return isEliminated[teamToIds.get(team)];
	}

	public Iterable<String> certificateOfElimination(String team) {
		if (!teamToIds.containsKey(team)) throw new IllegalArgumentException("No such a team");
		// subset R of teams that eliminates given team; null if not eliminated
		return certificateOfElimination.get(team);
	}

	private int largerThanProcessId(int a, int b) {
		if (a > b)
			return a;
		else
			return a + 1;
	}

	private boolean checkElimination(int teamId) {
		boolean eliminated = false;
		Set<String> subsetR = new HashSet<>();
		for (int i = 0; i < teamNums; i++) {
			if (i != teamId && wins[teamId] + remaining[teamId] - wins[i] < 0) {
				eliminated = true;
				subsetR.add(teamName[i]);
			}
		}
		if (eliminated) {
			certificateOfElimination.put(teamName[teamId], new LinkedList<String>(subsetR));
			return true;
		}
		int totVertices = 2 + firstLayerNums + secondLayerNums;
		int maxFlow = 0;
		FlowNetwork g = new FlowNetwork(totVertices);
		int againstVertices = 0;
		for (int i = 0; i < teamNums; i++) {
			if (i != teamId) {
				g.addEdge(new FlowEdge(firstLayerNums + largerThanProcessId(i, teamId), totVertices - 1,
						wins[teamId] + remaining[teamId] - wins[i]));
				for (int j = i + 1; j < teamNums; j++) {
					if (i != j && i != teamId && j != teamId) {
						againstVertices++;
						g.addEdge(new FlowEdge(0, againstVertices, games[i][j]));
						maxFlow += games[i][j];
						g.addEdge(new FlowEdge(againstVertices, firstLayerNums + largerThanProcessId(i, teamId),
								Double.POSITIVE_INFINITY));
						g.addEdge(new FlowEdge(againstVertices, firstLayerNums + largerThanProcessId(j, teamId),
								Double.POSITIVE_INFINITY));
					}
				}
			}
		}

		for (int i = 0; i < teamNums; i++)
			if (i != teamId) {

			}
		FordFulkerson maxFlowUsingFFk = new FordFulkerson(g, 0, totVertices - 1);
		againstVertices = 0;
		for (int i = 0; i < teamNums; i++) {
			for (int j = i + 1; j < teamNums; j++) {
				if (i != j && i != teamId && j != teamId) {
					againstVertices++;
					if (maxFlowUsingFFk.inCut(againstVertices)) {
						subsetR.add(teamName[i]);
						subsetR.add(teamName[j]);
					}
					;
				}
			}
		}
		
		if (maxFlowUsingFFk.value() != maxFlow){
			certificateOfElimination.put(teamName[teamId], new LinkedList<String>(subsetR));
			return true;
		}
		return false;
	}

	public static void main(String[] args) throws IOException {
		String fileName = "";
		try {
			fileName = args[0];
			BaseballElimination division = new BaseballElimination(fileName);
			for (String team : division.teams()) {
				if (division.isEliminated(team)) {
					StdOut.print(team + " is eliminated by the subset R = { ");
					for (String t : division.certificateOfElimination(team)) {
						StdOut.print(t + " ");
					}
					StdOut.println("}");
				} else {
					StdOut.println(team + " is not eliminated");
				}
			}
		} catch (FileNotFoundException exc) {
			System.out.println("File not found: " + fileName);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}
}
