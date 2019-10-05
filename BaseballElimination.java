/* *****************************************************************************
 *  Name: BaseballElimination
 *  Date: Sep 30, 2019
 *  Description: Baseball Elimination using mincut/maxflow
 **************************************************************************** */

import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class BaseballElimination {
    private ArrayList<String> teamNames = new ArrayList<String>();
    private HashMap<String, Team> teams = new HashMap<String, Team>();
    private int teamCount = 0;
    private int maxWins = -1;
    private String teamWithMax = null;

    private class Team {
        int id;
        int w;
        int l;
        int r;
        int[] matchup;
        boolean isElim = false;
        ArrayList<String> certificateOfElimination = null;

        public Team(int id, String name, int w, int l, int r, int[] matchup) {
            this.id = id;
            this.w = w;
            this.l = l;
            this.r = r;
            this.matchup = matchup;
            this.certificateOfElimination = null;
            if (w > maxWins) {
                maxWins = w;
                teamWithMax = name;
            }
        }

        public void setCert(ArrayList<String> names) {
            certificateOfElimination = names;
        }

        public ArrayList<String> getCert() {
            return certificateOfElimination;
        }

        public boolean getElim() {
            return isElim;
        }

        public void setElim(boolean isElim) {
            this.isElim = isElim;
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
                curLine = curLine.trim();
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
                Team curTeam = new Team(i - 1, tokens[0], Integer.parseInt(tokens[1]),
                                        Integer.parseInt(tokens[2]),
                                        Integer.parseInt(tokens[3]), matchup);
                teams.put(tokens[0], curTeam);
            }

            i++;
        }

        for (String team:teamNames) {
            elim(team);
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
        if (!teams.containsKey(team)) throw new IllegalArgumentException("invalid key");
        return teams.get(team).w;
    }

    // number of losses for given team
    public int losses(String team) {
        if (!teams.containsKey(team)) throw new IllegalArgumentException("invalid key");
        return teams.get(team).l;

    }

    // number of remaining games for given team
    public int remaining(String team) {
        if (!teams.containsKey(team)) throw new IllegalArgumentException("invalid key");
        return teams.get(team).r;

    }

    private int getTeamId(String team) {
        if (!teams.containsKey(team)) throw new IllegalArgumentException("invalid key");
        return teams.get(team).id;

    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        if (!teams.containsKey(team1)) throw new IllegalArgumentException("invalid key");
        if (!teams.containsKey(team2)) throw new IllegalArgumentException("invalid key");
        return teams.get(team1).matchup[teams.get(team2).id];

    }
    // https://stackoverflow.com/questions/2201113/combinatoric-n-choose-r-in-java-math
    private int binomial(final int N, final int K) {
        long ret = 1;
        for (int k = 0; k < K; k++) {
            ret = (ret * (N-k))/(k+1);
        }
        return (int) ret;
    }

    public boolean isEliminated(String team){
        if (!teams.containsKey(team)) throw new IllegalArgumentException("invalid key");
        return teams.get(team).getElim();
    }

    // is given team eliminated?
    private void elim(String team) {
        boolean debug = false;
        if (debug) StdOut.println("\nChecking: " + team);

        //trivial elimination
        if (maxWins > remaining(team) + wins(team)) {
            Team curTeam = teams.get(team);
            curTeam.setElim(true);
            ArrayList<String> elimnators = new ArrayList<String>();
            elimnators.add(teamWithMax);
            curTeam.setCert(elimnators);
            Team trivialTeam = teams.get(team);
            trivialTeam.setCert(elimnators);
            trivialTeam.setElim(true);

            if (debug) StdOut.println("trivial");
            return;
        }

        //no trivial elimination, construct flow network
        // team nodes are identified on the flow network by their id [0 => n-1]
        int SOURCE_NODE = numberOfTeams();
        int TARGET_NODE = SOURCE_NODE + 1;
        // versus nodes are identified by [n+2 => nCr(n-1) + n + 2)
        int ncr = binomial(numberOfTeams()-1,2);
        FlowNetwork flowN = new FlowNetwork(numberOfTeams() + ncr + 2);

        // int[] teamNodes = new int[numberOfTeams()];
        String[] teamNameLU = new String[numberOfTeams()];
        // int[][] vsNodes = new int[fact(numberOfTeams() - 1)][2];
        ArrayList<String> teamCombos = new ArrayList<String>();

        int teamI = 0;
        int vsI = 0;
        int vsNodeTracker = TARGET_NODE + 1;
        for (String teamName : teams()) {
            // teamNodes[teamI] = getTeamId(teamName);
            teamNameLU[teamI] = teamName;
            if (getTeamId(teamName) == getTeamId(team)) {
                teamI++;
                continue;
            }

            for (String teamInner : teams()) {
                if (getTeamId(teamInner) == getTeamId(team) || getTeamId(teamName) == getTeamId(
                        teamInner)) continue;

                if (!teamCombos.contains(teamInner + "" + teamName)) {
                    flowN.addEdge(
                            new FlowEdge(SOURCE_NODE, vsNodeTracker, against(teamName, teamInner)));
                    flowN.addEdge(
                            new FlowEdge(vsNodeTracker, getTeamId(teamName),
                                         Double.POSITIVE_INFINITY));
                    flowN.addEdge(
                            new FlowEdge(vsNodeTracker, getTeamId(teamInner),
                                         Double.POSITIVE_INFINITY));
                    vsNodeTracker++;
                    teamCombos.add(teamName + "" + teamInner);


                    if (debug) {
                        StdOut.println(
                                "\n\tsource => " + teamName + " - " + teamInner + ", capacity: "
                                        + against(teamName, teamInner));
                        StdOut.println("\t" + teamName + " - " + teamInner + " => " + teamName
                                               + ", capacity: " + Double.POSITIVE_INFINITY);
                        StdOut.println("\t" + teamName + " - " + teamInner + " => " + teamInner
                                               + ", capacity: " + Double.POSITIVE_INFINITY);


                    }

                }
                vsI++;
            }
            // wx + rx - wn
            int capper = wins(team) + remaining(team) - wins(teamName);
            flowN.addEdge(
                    new FlowEdge(getTeamId(teamName), TARGET_NODE,
                                 capper));
            if (debug) StdOut.println("\t" + teamName + " => target, capacity: " + capper);
            teamI++;
        }

        if (debug) StdOut.println("\n");
        if (debug) StdOut.println(flowN.toString());

        FordFulkerson maxFlow = new FordFulkerson(flowN, SOURCE_NODE, TARGET_NODE);
        ArrayList<String> elimnators = new ArrayList<String>();

        boolean isElim = false;
        if (debug) StdOut.print("Team Name LU: ");
        if (debug) StdOut.print(Arrays.toString(teamNameLU));
        for (int v = 0; v < numberOfTeams(); v++) {
            if (maxFlow.inCut(v)) {
                if (debug) StdOut.print("\nEliminated by: " + v);
                isElim = true;
                elimnators.add(teamNameLU[v]);
                if (debug) StdOut.print(teamNameLU[v] + " ");
            }
        }

        Team curTeam = teams.get(team);
        curTeam.setCert(elimnators);
        curTeam.setElim(isElim);

        if (debug) StdOut.println("\n");

        return;
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        return teams.get(team).getCert();
    }

    public static void main(String[] args) {
        boolean debug = false;
        StdOut.println("testing: " + args[0]);
        BaseballElimination division = new BaseballElimination(args[0]);
        if (debug) {
            for (String team : division.teams()) {
                for (String teamB : division.teams()) {
                    StdOut.println(
                            team + " vs " + teamB + " : " + division.against(team, teamB));
                }
                StdOut.println();
            }
        }
        for (String team : division.teams()) {
            if (debug) StdOut.println(team);
            if (debug) StdOut.println(division.wins(team));
            if (debug) StdOut.println(division.losses(team));
            if (debug) StdOut.println(division.remaining(team));
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
