import java.util.ArrayList;
import java.io.Serializable; 

/**
 * Class representing a sports bracket like those in March Madness.
 * A bracket contains a list of strings (64 in total) representing participating teams.
 * Each bracket is linked with its own user account.
 *
 * @author Matt, Dan, Hillary
 * @since 5/1/2017
 */
public class Bracket implements Serializable {
    /* results of previous simulations are not saved when this object is serialized(exported to file) */
    private final transient int[] teamScores = new int[NUM_OF_TEAM_SCORES];
    private final static int NUM_OF_TEAM_SCORES = 127;
    public static final long serialVersionUID = 5609181678399742983L;
    private final ArrayList<String> bracket;
    private String playerName;
    private String password;

    /**
     * Creates a new bracket containing the teams specified in the list passed in as an argument.
     * @param teams A list containing the 64 teams competing in the tournament.
     */
    public Bracket(ArrayList<String> teams) {
        bracket = new ArrayList<>(teams);
        while (bracket.size() < NUM_OF_TEAM_SCORES) {
            bracket.add(0,"");
        }
    }

    /**
     * Constructor using another Bracket to start
     * @param starting master bracket pre-simulation
     */
    public Bracket(Bracket starting) {
        bracket = new ArrayList<>(starting.getBracket());
    }

    /**
     * added by matt 5/2
     * Constructor that creates a new bracket with a users name
     * @param starting master bracket pre-simulation
     * @param user name of the new bracket owner
     */
    public Bracket(Bracket starting, String user) {
        bracket = new ArrayList<>(starting.getBracket());
        playerName = user;
    }

    /**
     * Moves a team up the bracket.
     * @param position The starting position of the team to be moved.
     */
    public void moveTeamUp(int position) {
        int newPos = ((position - 1) / 2);

        /* Check that the team isn't already in the destination position. */
        if (!(bracket.get(position).equals(bracket.get(newPos)))) {
            bracket.set(newPos, bracket.get(position));
        }
    }

    /**
     * Resets all children of root location except for initial teams at final children
     * special behavior if root = 0; just resets the final 4.
     * @param root everything below and including this is reset
     * @author Matt
     * @since 5/1
     */
    public void resetSubtree(int root) {
        if (root == 0) {//special behavior to reset final 4
            for (int i = 0; i < 7; i++) {
                bracket.set(i, "");
            }
        } else {
            int child1 = 2 * root + 1;
            int child2 = 2 * root + 2;

            if (child1 < 64) {//child is above round 1
                resetSubtree(child1);
            }
            if (child2 < 64) {
                resetSubtree(child2);
            }
            bracket.set(root, "");
        }
    }

    /**
     * removes all future wins of a team, including spot that this is called from
     * @param child index of the first place that the team gets deselected
     */
    public void removeAbove(int child) {//renamed by matt 5/1
        if (child == 0)
            bracket.set(child, "");
        else {
            int parent = ((child - 1) / 2);
            if (bracket.get(parent).equals(bracket.get(child))) {
                removeAbove(parent);
            }
            bracket.set(child, "");
        }
    }

    /**
     * Hillary:
     * returns true or false depending on whether there are any empty slots on the bracket.
     * If a position has an empty string then the advancing team has not been chosen for that spot and the whole bracket is not complete.
     * @return boolean.
     */
    public boolean isComplete() {
        for (String team : bracket) {
            if (team.equals("")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Matt 5/2
     * Scores the bracket by assigning points of each correct winner
     * number of points is based on round
     * @param master the master bracket of true winners to which all brackets are compared
     */
    public int scoreBracket(Bracket master) {
        int score = 0;
        if (bracket.get(0).equals(master.getBracket().get(0)))//finals
            score += 32;
        for (int i = 1; i < 3; i++) {
            if (bracket.get(i).equals(master.getBracket().get(i)))//semi
                score += 16;
        }
        for (int i = 3; i < 7; i++) {
            if (bracket.get(i).equals(master.getBracket().get(i)))//quarters
                score += 8;
        }
        for (int i = 7; i < 15; i++) {
            if (bracket.get(i).equals(master.getBracket().get(i)))//sweet 16
                score += 4;
        }
        for (int i = 15; i < 31; i++) {
            if (bracket.get(i).equals(master.getBracket().get(i)))//round of 32
                score += 2;
        }
        for (int i = 31; i < 63; i++) {
            if (bracket.get(i).equals(master.getBracket().get(i)))//round of 64
                score += 1;
        }
        return score;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setTeamScore(int game, int score){
        teamScores[game] = score;
    }

    public ArrayList<String> getBracket(){
        return bracket;
    }
}