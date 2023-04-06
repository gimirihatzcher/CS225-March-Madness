import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import java.util.HashMap;

/**
 * ScoreBoardPane class is the class the displays the Scoreboard from the Main GUI.
 * It shows all Player's names and their scores.
 * @author Sarah Higgins and Ying Sun
 * Created by Sarah on 5/2/17. Updated by Naomi Coakley on 4/4/23
 * */
public class ScoreBoardTable {
    /**
     * Scores mapped to their respective players
     */
    private HashMap<Bracket, Integer> scores;
    private final int maxPlayers = 16;
    /**
     * Organizes rows and columns of usernames and their scores in order of placement.
     * Displayed by MarchMadnessGUI.
     */
    private TableView<Bracket> scoreTable;
    /**
     * The leftmost column of scoreTable.
     */
    private TableColumn<Bracket, String> userNameCol;
    /**
     * The rightmost column of scoreTable.
     */
    private TableColumn<Bracket, Number> totalPtsCol;

    private ObservableList<Bracket> data;
    /**
     * ScoreBoardPane default constructor
     */
    @SuppressWarnings("unchecked")
    public ScoreBoardTable() {
        scoreTable = new TableView<>();
        data = FXCollections.observableArrayList();
        scores = new HashMap<>();

        userNameCol = new TableColumn<>("Username");
        userNameCol.setMinWidth(140);
        userNameCol.setMaxWidth(140);
        userNameCol.setStyle("-fx-border-width: 3px");
        /*
         * userNameCol.setCellValueFactory() passes the data to the TableView object, which is
         * automatically sorted by TableColumn.SortType.DESCENDING
         */
        userNameCol.setCellValueFactory(b -> new SimpleStringProperty(b.getValue().getPlayerName()));
        userNameCol.setSortable(true);

        totalPtsCol = new TableColumn<>("Total Points");
        totalPtsCol.setMinWidth(140);
        totalPtsCol.setMaxWidth(140);
        totalPtsCol.setStyle("-fx-border-width: 3px");
        /*
         * totalPtsCol.setCellValueFactory() passes the data to the TableView object, which is
         * automatically sorted by TableColumn.SortType.DESCENDING
         */
        totalPtsCol.setCellValueFactory(b -> new SimpleIntegerProperty(scores.get(b.getValue())));
        totalPtsCol.setSortable(true);
        totalPtsCol.setSortType(TableColumn.SortType.ASCENDING); //sorts column from highest to lowest

        scoreTable.setItems(data);
        scoreTable.setEditable(false);
        scoreTable.getColumns().setAll(userNameCol, totalPtsCol);
        scoreTable.getSortOrder().addAll(totalPtsCol, userNameCol);
    }

    /**
     * Adds a passed in player and their score to scores.
     * Will update the existing player score or add new player if and only if there are fewer than 16 players.
     */
    public void addPlayer(Bracket name, int score) {
        try {
            if (scores == null) {
                scores = new HashMap<>();
            }
            if (scores.get(name) != null || scores.size() < maxPlayers) {
                scores.put(name, score);
                data.add(name);
                scoreTable.sort();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //feature to implement
    /**
     * clearPlayers method clears all players from this Bracket
     */
    public void clearPlayers() {
        scores = new HashMap<Bracket, Integer>();
        data = FXCollections.observableArrayList();
    }

    public TableView<Bracket> getScoreTable() {
        return scoreTable;
    }
}
