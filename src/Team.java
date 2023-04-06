/**
 * Team class holds all the information for individual teams. Their name, info, and ranking.
 * @author Shivanie and Tyler
 */
public class Team{
  private final String name;
  private final String nickname;
  /** A brief description of the team */
  private final String info;
  /** Value of 1-16 based on performance in their region */
  private final int ranking;
  /** PPG - points per game, offense - how many ppg scored */
  public double offensePPG;
  /** PPG - points per game, defense - how many ppg this team allowed the opposing teams to score */
  public double defensePPG;

  public Team(String name, String nickname, String info, int ranking, double oPPG, double dPPG){
    this.name = name;
    this.nickname = nickname;
    this.info = info;
    this.ranking = ranking;
    offensePPG = oPPG;
    defensePPG = dPPG;
  }

  public String getName(){
    return name;
  }

  public String getNickname(){
	  return nickname;
  }

  public String getInfo(){
    return info;
  }

  public int getRanking(){
    return ranking;
  }

  public double getOffensePPG(){
    return offensePPG;
  }

  public double getDefensePPG(){
    return defensePPG;
  }
}