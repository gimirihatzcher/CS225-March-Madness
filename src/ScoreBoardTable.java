import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.util.HashMap;
/**
 * ScoreBoardPane class is the class the displays the Scoreboard from the Main GUI.
 * It shows all the Player's names and their scores.
 */
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
    /**
     * List that reflects its changes immediately in JavaFX
     */
    private ObservableList<Bracket> data;
    /**
     * ScoreBoardPane default constructor
     */
    public ScoreBoardTable() {
        scoreTable = new TableView<>();
        data = FXCollections.observableArrayList();
        scores = new HashMap<>();

        userNameCol = new TableColumn<>("Username");
        userNameCol.setMinWidth(140);
        userNameCol.setMaxWidth(140);
        userNameCol.setStyle("-fx-border-width: 3px");
        userNameCol.setCellValueFactory(b -> new SimpleStringProperty(b.getValue().getPlayerName()));
        userNameCol.setSortable(true);

        /**
         * TableColumn totalPtsCol is the column on the right side of the table
         * totalPtsCol.setCellValueFactory() passes the data to the TableView object, which is
         *                                   automatically sorted with the TableColumn.SortType.ASCENDING
         *                                   code line.
         */
        TableColumn<Bracket, Number> totalPtsCol = new TableColumn<>("Total Points");
        totalPtsCol.setMinWidth(140);
        totalPtsCol.setMaxWidth(140);
        totalPtsCol.setStyle("-fx-border-width: 3px");
        totalPtsCol.setCellValueFactory(b -> new SimpleIntegerProperty(scores.get(b.getValue())));
        totalPtsCol.setSortable(true);
        totalPtsCol.setSortType(TableColumn.SortType.ASCENDING); //sorts column from lowest to highest

        scoreTable.setItems(data);
        scoreTable.setEditable(false);
        scoreTable.getColumns().setAll(userNameCol, totalPtsCol);
        scoreTable.getSortOrder().addAll(totalPtsCol, userNameCol);
    }
    /**
     * Accesses the table to be shown by the GUI
     */
    public TableView<Bracket> getScoreTable() {
        return scoreTable;
    }
    /**
     * Adds a passed in player and their score to scores.
     * Will update the existing player score or add new player if and only if there are fewer than 16 players.
     */
    public void addPlayer(Bracket name, int score) {
        try {
            if (scores == null) {
                scores = new HashMap<Bracket, Integer>();
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

}
