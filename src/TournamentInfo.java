import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 * Created by Artem on 5/2/2017.
 */
public class TournamentInfo{
    /** Access Team objects using their Team.name */
    HashMap<String, Team> teams;

    /**
     * Constructor
     * @throws IOException - Loading "teamInfo.txt" file
     * */
    public TournamentInfo() throws IOException{
        teams = new HashMap<>();
        loadFromFile();
    }

    /**
     * This private method will load all the team information from the teamInfo.txt file via a BufferedReader and load each team into
     * the teams HashMap using their name as the key and the actual Team object as the data.
     * @authors Artem, Rodrigo
     * @throws IOException - while loading "teamInfo.txt" file if missing.
     */
    private void loadFromFile() throws IOException{
        String name;
        String nickname;
        String info;
        int ranking;
        double offensivePPG;
        double defensivePPG;


        InputStream u = getClass().getResourceAsStream("teamInfo.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(u));

        while((name = br.readLine()) != null){
            nickname = br.readLine();
            info = br.readLine();
            ranking = Integer.parseInt(br.readLine());
            offensivePPG = Double.parseDouble(br.readLine());
            defensivePPG = Double.parseDouble(br.readLine());

            Team newTeam = new Team(name, nickname, info, ranking, offensivePPG, defensivePPG); //creates team with info

            br.readLine();   //gets rid of empty line between team infos

            teams.put(newTeam.getName(), newTeam);   //map team name with respective team object
        }

        br.close();

    }

    /**
     * This will be the method that actually does the work of determining the outcome of the games.
     * It will use the seed/ranking from each team on the bracket and put it into an algorithm to somewhat randomly generate a winner
     * @authors Artem, Dan, Matt
     * @param startingBracket -- the bracket to be simulated upon. The master bracket
     */
    public void simulate(Bracket startingBracket){
        for (int i = 62; i >= 0; i--) {
            int index1 = 2 * i + 1;
            int index2 = 2 * i + 2;

            Team team1 = teams.get(startingBracket.getBracket().get(index1));
            Team team2 = teams.get(startingBracket.getBracket().get(index2));

            int score1 = 0;
            int score2 = 0;
            while(score1 == score2) {
                /* [chris] 4/7/23: This formula generates a random integer between 56 and 137,
                with the range of values skewed towards the upper end for higher-ranked
                teams. The rankWeight variable adjusts the random number generated
                by Math.random() based on the ranking of the team, making it more
                likely for higher-ranked teams to obtain a higher final score.
                */
                double rankWeight1 = 0.7 + (team1.getRanking() * 0.02);
                score1 = (int) (((Math.random() * 61 * rankWeight1) + 75) * rankWeight1);

                double rankWeight2 = 0.7 + (team2.getRanking() * 0.02);
                score2 = (int) (((Math.random() * 61 * rankWeight2) + 75) * rankWeight2);
            }

            startingBracket.setTeamScore(index1, score1);
            startingBracket.setTeamScore(index2, score2);

            if(score1 > score2) {
                startingBracket.moveTeamUp(index1);
            } else {
                startingBracket.moveTeamUp(index2);
            }
        }
    }

    /**
     * reads Strings from initialMatches.txt into an ArrayList in order to construct the starting bracket
     * @authors Matt, Artem
     * @return ArrayList of Strings
     * @throws IOException - loading "initialMatches.txt";
     */
    public ArrayList<String> loadStartingBracket() throws IOException{
        String name;
        ArrayList<String> starting = new ArrayList<>();

        InputStream u = getClass().getResourceAsStream("initialMatches.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(u));

        while((name = br.readLine()) != null){
            starting.add(name);
        }

        br.close();
        return starting;
    }

    /**
     * This method will take a parameter of a team name and return the Team object corresponding to it.
     * If it is unsuccessful, meaning the team does not exist, it will throw an exception.
     * @authors Artem
     * @param teamName -- the name of the team to be found
     * @return the Team object for that team
     */
    public Team getTeam(String teamName){
        return teams.get(teamName);
    }
}
