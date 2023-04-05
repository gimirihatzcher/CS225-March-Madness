import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *  Class used to create, compose, and display the Graphical interface
 *  for the user.
 *
 *  @author Grant Osborn
 */
public class MarchMadnessGUI extends Application {
    /** Root pane of application */
    private BorderPane rootPane;
    /** Top row of buttons container */
    private ToolBar topToolBar;
    /** Bottom row of buttons container */
    private ToolBar bottomToolBar;
    /** Launches simulation of each game */
    private Button simulateBtn;
    /** Perform the login feature */
    private Button loginBtn;
    /** Display the scoreboard */
    private Button scoreBoardBtn;
    /** Allow user to view the bracket they simulated */
    private Button viewBracketBtn;
    /** reset the bracket to remove selections */
    private Button clearBtn;
    /** reset the bracket to remove selections */
    private Button resetBtn;
    /** Locks in user's choices for the bracket */
    private Button finalizeBtn;
    /** allows you to navigate back to division selection screen */
    private Button backBtn;
    /** A default initialized bracket */
    private Bracket startingBracket;
    /** reference to currently logged in bracket */
    private Bracket selectedBracket;
    /** Bracket modified to house results of simulation */
    private Bracket simResultBracket;
    /** Collection of all saved Brackets converted into Objects */
    private ArrayList<Bracket> savedPlayerBrackets;
    /** Map linking a players name with their bracket */
    private HashMap<String, Bracket> savedBracketsMap;
    /** Component to display bracket results, contains a TableView<Bracket> */
    private ScoreBoardTable scoreBoard;
    /** Houses Graphical components used to display bracket window */
    private BracketPane bracketPane;
    /** Window to house login feature and display to user */
    private GridPane loginPane;
    /** Data structure for Team objects involved in tournament */
    private TournamentInfo teamInfo;

    /**
     * Initialize class components and compose window setting
     */
    @Override
    public void init() {
        try{
            teamInfo = new TournamentInfo();
            startingBracket = new Bracket(teamInfo.loadStartingBracket());
            simResultBracket = new Bracket(teamInfo.loadStartingBracket()); // TODO: does this need to be initialized at all?
        } catch (IOException ex) {
            showError(new Exception("Can't find " + ex.getMessage(), ex),true);
        }

        // Convert all .ser files current saved into Bracket objects
        savedPlayerBrackets = loadSavedBrackets();

        // import all saved player Brackets
        savedBracketsMap = new HashMap<>();
        for(Bracket b : savedPlayerBrackets){
            savedBracketsMap.put(b.getPlayerName(), b);
        }

        rootPane = new BorderPane();
        scoreBoard = new ScoreBoardTable();
        loginPane = createLoginWindow();
        initToolBars();

        // Connect Events to appropriate call methods
        loginBtn.setOnAction(e-> displayLoginWindow());
        simulateBtn.setOnAction(e-> simulateBracketGames());
        scoreBoardBtn.setOnAction(e->swapDisplayWindow(scoreBoard.getScoreTable()));
        viewBracketBtn.setOnAction(e-> displaySimulatedBracket());
        clearBtn.setOnAction(e->clear());
        resetBtn.setOnAction(e->reset());
        finalizeBtn.setOnAction(e->finalizeBracket());
        backBtn.setOnAction(e->{
            bracketPane = new BracketPane(selectedBracket);
            swapDisplayWindow(bracketPane);
        });

        // compose window by combing components
        rootPane.setTop(topToolBar);
        rootPane.setBottom(bottomToolBar);
    }

    /**
     * Called to start running the application
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {
        //display login screen
        displayLoginWindow();

        Scene scene = new Scene(rootPane);
        primaryStage.setMaximized(true);
        primaryStage.setTitle("March Madness Bracket Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    
    
    /**
     * simulates the tournament  
     * simulation happens only once and
     * after the simulation no more users can log in
     */
    private void simulateBracketGames(){
        loginBtn.setDisable(true);
        simulateBtn.setDisable(true);
        scoreBoardBtn.setDisable(false);
        viewBracketBtn.setDisable(false);

        teamInfo.simulate(simResultBracket); // TODO: modifies reference to variable, should return
        for(Bracket b : savedPlayerBrackets) {
            scoreBoard.addPlayer(b,b.scoreBracket(simResultBracket));
        }

        swapDisplayWindow(scoreBoard.getScoreTable());
    }

    /**
     * Used to change what is displayed in the center of 'rootPane'
     * @param p - new Pane to display
     */
    private void swapDisplayWindow(Node p) {
        rootPane.setCenter(p);
        BorderPane.setAlignment(p,Pos.CENTER);
    }
    
    /**
     * Displays the login screen
     */
    private void displayLoginWindow(){
        loginBtn.setDisable(true);
        simulateBtn.setDisable(true);
        scoreBoardBtn.setDisable(true);
        viewBracketBtn.setDisable(true);
        bottomToolBar.setDisable(true);
        swapDisplayWindow(loginPane);
    }
    
     /**
      * Displays Simulated Bracket
      */
    private void displaySimulatedBracket(){
        selectedBracket = simResultBracket;
        bracketPane = new BracketPane(selectedBracket);
        GridPane full = bracketPane.getFullPane();
        full.setAlignment(Pos.CENTER);
        full.setDisable(true);
        swapDisplayWindow(new ScrollPane(full));
    }
    
    /**
    * allows user to choose bracket
    */
    private void displaySelectedBracket() {
        bottomToolBar.setDisable(false);
        bracketPane = new BracketPane(selectedBracket); // TODO: creates new object instead of altering existing object
        swapDisplayWindow(bracketPane);
    }

    /**
    * resets current selected subtree
    * for final4 reset Ro2 and winner
    */
    private void clear(){
        bracketPane.clear();
        bracketPane = new BracketPane(selectedBracket);
        swapDisplayWindow(bracketPane);
    }
    
    /**
     * resets entire bracket
     */
    private void reset(){
        if(confirmReset()){
            //horrible hack to reset
            selectedBracket = new Bracket(startingBracket);
            bracketPane = new BracketPane(selectedBracket);
            swapDisplayWindow(bracketPane);
        }
    }
    
    private void finalizeBracket(){
        if(bracketPane.isComplete()){
            bottomToolBar.setDisable(true);
            bracketPane.setDisable(true);
            simulateBtn.setDisable(false);
            loginBtn.setDisable(false);
            //save the bracket along with account info
            serializeBracket(selectedBracket);
        }else{
            infoAlert("You can only finalize a bracket once it has been completed.");
        }
    }

    
    /**
     * Creates toolBar and buttons.
     * Add buttons to the toolbar and saves global references to them
     */
    private void initToolBars(){
        topToolBar = new ToolBar();
        bottomToolBar = new ToolBar();
        loginBtn = new Button("Login");
        simulateBtn = new Button("Simulate");
        scoreBoardBtn = new Button("ScoreBoard");
        viewBracketBtn = new Button("View Simulated Bracket");
        clearBtn = new Button("Clear");
        resetBtn = new Button("Reset");
        finalizeBtn = new Button("Finalize");

        // compose toolbars by adding components
        topToolBar.getItems().addAll(
                createSpacer(),
                loginBtn,
                simulateBtn,
                scoreBoardBtn,
                viewBracketBtn,
                createSpacer()
        );
        bottomToolBar.getItems().addAll(
                createSpacer(),
                clearBtn,
                resetBtn,
                finalizeBtn,
                backBtn = new Button("Choose Division"),
                createSpacer()
        );
    }
    
    /**
     * Creates a spacer for centering buttons in a ToolBar
     */
    private Pane createSpacer(){
        Pane spacer = new Pane();
        HBox.setHgrow(
                spacer,
                Priority.SOMETIMES
        );
        return spacer;
    }

    /**
     * Create and compose the window used for user login
     * @return - new window
     */
    private GridPane createLoginWindow(){
        GridPane loginPane = new GridPane();
        loginPane.setAlignment(Pos.CENTER);
        loginPane.setHgap(10);
        loginPane.setVgap(10);
        loginPane.setPadding(new Insets(5, 5, 5, 5));

        Text welcomeMessage = new Text("March Madness Login Welcome");
        loginPane.add(welcomeMessage, 0, 0, 2, 1);

        Label userName = new Label("User Name: ");
        loginPane.add(userName, 0, 1);

        TextField enterUser = new TextField();
        loginPane.add(enterUser, 1, 1);

        Label password = new Label("Password: ");
        loginPane.add(password, 0, 2);

        PasswordField passwordField = new PasswordField();
        loginPane.add(passwordField, 1, 2);

        Button signButton = new Button("Sign in");
        loginPane.add(signButton, 1, 4);
        signButton.setDefaultButton(true);//added by matt 5/7, lets you use sign in button by pressing enter

        Label message = new Label();
        loginPane.add(message, 1, 5);

        signButton.setOnAction(event -> {
            // the name user enter
            String name = enterUser.getText();
            // the password user enter
            String playerPass = passwordField.getText();
            if (savedBracketsMap.get(name) != null) {
                //check password of user
                Bracket tmpBracket = this.savedBracketsMap.get(name);
                String password1 = tmpBracket.getPassword();
                if (Objects.equals(password1, playerPass)) {
                    // load bracket
                    selectedBracket = savedBracketsMap.get(name);
                    displaySelectedBracket();
                }else{
                   infoAlert("The password you have entered is incorrect!");
                }
            } else {
                //check for empty fields
                if(!name.equals("")&&!playerPass.equals("")){
                    //create new bracket
                    Bracket tmpPlayerBracket = new Bracket(startingBracket, name);
                    savedPlayerBrackets.add(tmpPlayerBracket);
                    tmpPlayerBracket.setPassword(playerPass);

                    savedBracketsMap.put(name, tmpPlayerBracket);
                    selectedBracket = tmpPlayerBracket;
                    //alert user that an account has been created
                    infoAlert("No user with the Username \""  + name + "\" exists. A new account has been created.");
                    displaySelectedBracket();
                }
            }
        });

        return loginPane;
    }

    
    /**
     * The Exception handler
     * Displays an error message to the user and kills program on fatal error.
     * @param fatal true if the program should exit. false otherwise
     */
    private void showError(Exception e, boolean fatal){
        String msg = e.getMessage();
        if(fatal){
            msg = msg + " \n\nthe program will now close";
        }
        Alert alert = new Alert(AlertType.ERROR,msg);
        alert.setResizable(true);
        alert.getDialogPane().setMinWidth(420);   
        alert.setTitle("Error");
        alert.setHeaderText("something went wrong");
        alert.showAndWait();
        if(fatal){ 
            System.exit(666);
        }   
    }
    
    /**
     * alerts user to the result of their actions in the login pane 
     * @param msg the message to be displayed to the user
     */
    private void infoAlert(String msg){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("March Madness Bracket Simulator");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    /**
     * Prompts the user to confirm that they want
     * to clear all predictions from their bracket
     * @return true if the yes button clicked, false otherwise
     */
    private boolean confirmReset(){
        Alert alert = new Alert(AlertType.CONFIRMATION, 
                "Are you sure you want to reset the ENTIRE bracket?", 
                ButtonType.YES,  ButtonType.CANCEL);
        alert.setTitle("March Madness Bracket Simulator");
        alert.setHeaderText(null);
        alert.showAndWait();
        return (alert.getResult() == ButtonType.YES); // TODO: .equals?
    }

    /**
     * Tayon Watson 5/5
     * Export the Bracket to a .ser file by serializing it.
     * @param B The bracket the is going to be serialized
     */
    private void serializeBracket(Bracket B){
        FileOutputStream outStream;
        ObjectOutputStream out;
        try {
            outStream = new FileOutputStream(B.getPlayerName()+".ser");
            out = new ObjectOutputStream(outStream);
            out.writeObject(B);
            out.close();
        } catch(IOException e) {
          showError(new Exception("Error saving bracket \n"+e.getMessage(),e),false);
        }
    }

    /**
     * Tayon Watson 5/5
     * import a .ser file and deserialize it into a Bracket object
     * @param filename of the serialized bracket file
     * @return deserialized bracket 
     */
    private Bracket deserializeBracket(String filename){
        Bracket bracket = null;
        FileInputStream inStream;
        ObjectInputStream in;
        try {
            inStream = new FileInputStream(filename);
            in = new ObjectInputStream(inStream);
            bracket = (Bracket) in.readObject();
            in.close();
        } catch (IOException | ClassNotFoundException e) {
            showError(new Exception("Error loading bracket \n"+e.getMessage(),e),false);
        }

        return bracket;
    }
    
    /**
     * Tayon Watson 5/5
     * Import all .ser files from project folder and Instantiate Bracket objects for each file
     * @return List of new Bracket objects
     */
    private ArrayList<Bracket> loadSavedBrackets() {
        ArrayList<Bracket> list = new ArrayList<>();
        File dir = new File(".");

        // check if directory exists
        if(!dir.exists()) {
            return list;
        }

        // list all files in the directory
        File[] files = dir.listFiles();

        // iterate over each file and check if it has .ser extension
        for (File fileEntry : files) {
            String fileName = fileEntry.getName();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

            if (extension.equals("ser")) {
                list.add(deserializeBracket(fileName));
            }
        }

        return list;
    }
}
