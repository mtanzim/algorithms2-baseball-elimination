/* *****************************************************************************
 *  Name: BaseballElimination
 *  Date: Sep 30, 2019
 *  Description: Baseball Elimination using mincut/maxflow
 **************************************************************************** */

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class BaseballElimination {
    private ArrayList<String> teamNames = new ArrayList<String>();
    private HashMap<String, Team> teams = new HashMap<String, Team>();
    private int teamCount = 0;

    private class Team {
        int id;
        int w;
        int l;
        int r;
        int[] matchup;

        public Team(int id, int w, int l, int r, int[] matchup) {
            this.id = id;
            this.w = w;
            this.l = l;
            this.r = r;
            this.matchup = matchup;
        }
    }

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        boolean debug = false;
        In file = new In(filename);
        teamCount = file.readInt();
        if (debug)
            StdOut.println(teamCount);
        int i = 0;
        while (file.hasNextLine()) {
            String curLine = file.readLine();
            if (i != 0) {
                String[] tokens = curLine.split("\\s+");
                if (debug)
                    StdOut.println(Arrays.toString(tokens));
                if (debug)
                    StdOut.println(curLine);
                teamNames.add(tokens[0]);
                int matchup[] = new int[teamCount];
                for (int k = 4; k < tokens.length; k++) {
                    matchup[k - 4] = Integer.parseInt(tokens[k]);
                }
                if (debug)
                    StdOut.println(Arrays.toString(matchup));
                Team curTeam = new Team(i - 1, Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]),
                        Integer.parseInt(tokens[3]), matchup);
                teams.put(tokens[0], curTeam);
            }

            i++;
        }

    }

    // number of teams
    public int numberOfTeams() {
        return teamCount;
    }

    // all teams
    public Iterable<String> teams() {
        return teamNames;
    }

    // number of wins for given team
    public int wins(String team) {
        return teams.get(team).w;
    }

    // number of losses for given team
    public int losses(String team) {
        return teams.get(team).l;

    }

    // number of remaining games for given team
    public int remaining(String team) {
        return teams.get(team).r;

    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        return teams.get(team1).matchup[teams.get(team2).id];

    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        return true;
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        return new ArrayList<String>();
    }

    public static void main(String[] args) {
        StdOut.println("testing: " + args[0]);
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            for (String teamB : division.teams()) {
                StdOut.println(team + " vs " + teamB + " : " + division.against(team, teamB));
            }
            StdOut.println();
        }
        for (String team : division.teams()) {
            StdOut.println(team);
            StdOut.println(division.wins(team));
            StdOut.println(division.losses(team));
            StdOut.println(division.remaining(team));
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
    }
}
