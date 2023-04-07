import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sarah on 5/2/17.
 * @author Sarah Higgins and Ying Sun
 * ScoreBoardPane class is the class the displays the Scoreboard from the Main GUI.
 * It shows all the Player's names and their scores.
 */
public class ScoreBoardTable {

    /**
     * attributes
     */
    private Map<Bracket, Integer> scores;
    private static final int MAX_PLAYER_NUMBER = 16;
    private TableView<Bracket> table;
    private ObservableList<Bracket> data;

    /**
     * ScoreBoardPane constructor
     */
    @SuppressWarnings("unchecked")
    public ScoreBoardTable() {
        table = new TableView<>();
        data = FXCollections.observableArrayList();
        scores = new HashMap<>();

        /**
         * TableColumn userNameCol is the column on the left side of the table.
         * userNameCol.setCellValueFactory() passes the data to the TableView object, which is
         *                                   automatically sorted with the TableColumn.SortType.DESCENDING
         *                                   code line.
         */
        TableColumn<Bracket, String> userNameCol = new TableColumn<>("Username");
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

        /*
         * TableView table_view is what the user sees in the GUI. This creates the table.
         *
         */
        
        table.setItems(data);
        table.setEditable(false);
        table.getColumns().setAll(userNameCol, totalPtsCol);
        table.getSortOrder().addAll(totalPtsCol, userNameCol);
        table.sort();
    }

    public TableView<Bracket> start() {
                
        return table;
    }

    //Ying's code, method addPlayer adds a player to the Bracket
    public void addPlayer(Bracket name, int score) {
        try {
            if (scores == null) {
                scores = new HashMap<Bracket, Integer>();
            }
            //only allow to update the existing player score or add new player if there
            //is less than 16 players
            if (scores.get(name) != null || scores.size() < MAX_PLAYER_NUMBER) {
                scores.put(name, score);
                data.add(name);
                table.sort();
                //System.out.println("added: " + name.getPlayerName() + " " + score);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
