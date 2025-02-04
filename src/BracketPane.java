import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.Region;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * The BracketPane class represents a pane that displays a tournament bracket structure.
 * It allows users to interact with the bracket, including simulating matches and displaying team information.
 * The BracketPane is responsible for initializing the bracket, handling user interactions, and updating the bracket.
 * The class is designed to work in conjunction with the Bracket, Team, and TournamentInfo classes.
 * Created by Richard and Ricardo on 5/3/17. Updated by Dorin Tihon 04/04/2023.
 */
public class BracketPane extends BorderPane {
        /**
         * Reference to the graphical representation of the nodes within the bracket.
         */
        private static ArrayList<BracketNode> nodes;
        /**
         * Maps the text "buttons" to its respective grid-pane
         */
        private HashMap<StackPane, Pane> panes;
        /**
         * Reference to the current bracket.
         */
        private Bracket currentBracket;
        /**
         * Reference to active subtree within current bracket.
         */
        private int displayedSubtree;
        /**
         * Important logical simplification for allowing for code that is easier
         * to maintain.
         */
        private HashMap<BracketNode, Integer> bracketMap;
        /**
         * The `nodeMap` is the reverse mapping of the `bracketMap`; it maps an index in the bracket
         * to its corresponding `BracketNode`.
         */
        private HashMap<Integer, BracketNode> nodeMap;

        /**
         * The `center` field is a `GridPane` that is used as the center of the bracket display.
         */
        private GridPane center;

        /**
         * The `fullPane` field is a `GridPane` that is the full bracket display, including the
         * `center` and any other necessary components.
         */
        private GridPane fullPane;

        /** Handles mouseExited events for BracketNode objects */
        private EventHandler<MouseEvent> exit;
        /** Handles mouseEntered events for BracketNode objects */
        private EventHandler<MouseEvent> enter;
        /** Handles clicked events for BracketNode objects */
        private EventHandler<MouseEvent> clicked;

          /**
         * Default constructor for the BracketPane class.
         */
        public BracketPane() {
                // Initialize instance variables with default values
                nodes = new ArrayList<>();
                panes = new HashMap<>();
                bracketMap = new HashMap<>();
                nodeMap = new HashMap<>();
                displayedSubtree = 0;

                // Create an empty Bracket instance for currentBracket
                this.currentBracket = null;

                // Initialize other instance variables as needed
                center = new GridPane();
                fullPane = new GridPane();

                // initialize mouseEvent handlers
                exit = this::handleExit;
                enter = this::handleEnter;
                clicked = this::handleClicked;
        }

        /**
         * Initializes the properties needed to construct a bracket.
         * The constructor takes a Bracket object to display and interact with.
         * @param currentBracket the bracket to display and interact with
         */
        public BracketPane(Bracket currentBracket) {
                this();
                displayedSubtree = 0;
                this.currentBracket = currentBracket;

                ArrayList<StackPane> buttons = createButtons();
                ArrayList<BracketTree> roots = createRoots(buttons);
                Pane finalPane = createFinalFour();
                fullPane = createFullPane(roots, finalPane);

                GridPane buttonGrid = initButtonGrid(buttons);

                panes.put(buttons.get((buttons.size() - 1)), fullPane);
                finalPane.toBack();

                // set default center to the button grid
                this.setCenter(buttonGrid);
                addEventListeners(buttons);
        }

        /**
         * Handler method for left-clicking on BracketNode(selecting a team to advance in bracket) and
         * right-clicking on BracketNode(to show team information).
         * @param event The users mouse click triggered event object
         */
        private void handleClicked(MouseEvent event) {
                //conditional added by matt 5/7 to differentiate between left and right mouse click
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                        BracketNode n = (BracketNode) event.getSource();
                        int treeNum = bracketMap.get(n);
                        int nextTreeNum = (treeNum - 1) / 2;
                        if (!nodeMap.get(nextTreeNum).getName().equals(n.getName())) {
                                currentBracket.removeAbove((nextTreeNum));
                                clearAbove(treeNum);
                                nodeMap.get((bracketMap.get(n) - 1) / 2).setName(n.getName());
                                currentBracket.moveTeamUp(treeNum);
                        }
                }
                //added by matt 5/7, shows the teams info if you right-click

                else if (event.getButton().equals(MouseButton.SECONDARY)) {

                        String text = "";
                        BracketNode n = (BracketNode) event.getSource();
                        int treeNum = bracketMap.get(n);
                        String teamName = currentBracket.getBracket().get(treeNum);
                        try {
                                TournamentInfo info = new TournamentInfo();
                                Team t = info.getTeam(teamName);
                                //by Tyler - added the last two pieces of info to the pop-up window
                                text += "Team: " + teamName + " | Ranking: " + t.getRanking() + "\nMascot: "
                                        + t.getNickname() + "\nInfo: " + t.getInfo() + "\nAverage Offensive PPG: "
                                        + t.getOffensePPG() + "\nAverage Defensive PPG: "+ t.getDefensePPG();
                        } catch (IOException e) {//if for some reason TournamentInfo isn't working, it will display info not found
                                text += "Info for " + teamName + "not found";
                        }
                        //create a popup with the team info
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.CLOSE);
                        alert.setTitle("March Madness Bracket Simulator");
                        alert.setHeaderText(null);
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        alert.showAndWait();
                }
        }

        /**
         * Handler method for hovering mouse over team name. Changes color of text.
         * @param event The users mouse click triggered event object
         */
        private void handleEnter(MouseEvent event) {
                BracketNode tmp = (BracketNode) event.getSource();
                tmp.setEffect(new InnerShadow(10, Color.DARKBLUE));
        }

        /**
         * Handler method for moving mouse away from hovering over team name. Removes color change effect of text.
         * @param event The users mouse click triggered event object
         */
        private void handleExit(MouseEvent event) {
                BracketNode tmp = (BracketNode) event.getSource();
                tmp.setStyle(null);
                tmp.setEffect(null);
        }

        /**
         * Clears the entries of a team future wins
         * @param treeNum the tree number to start clearing from
         */
        private void clearAbove(int treeNum) {
                int nextTreeNum = (treeNum - 1) / 2;
                if (!nodeMap.get(nextTreeNum).getName().isEmpty()) {
                        nodeMap.get(nextTreeNum).setName("");
                        clearAbove(nextTreeNum);
                }
        }

        /**
         * Clears the current subtree from the displayed subtree.
         */
        public void clear(){
                clearSubtree(displayedSubtree);
        }

        /**
         * Gets the full pane containing the entire bracket.
         *
         * @return fullPane, a GridPane that contains the entire bracket.
         */
        public GridPane getFullPane() {
                return fullPane;
        }

        /**
         * Adds event listeners to the region selection buttons
         * This allows hovering over a region button to alter the color, creating a highlight effect.
         *
         * @param buttons The list of region buttons
         */
        private void addEventListeners(ArrayList<StackPane> buttons) {
                for (StackPane t : buttons) {
                        t.setOnMouseEntered(mouseEvent -> {
                                t.setEffect(new InnerShadow(10, Color.GREENYELLOW));
                        });
                        t.setOnMouseExited(mouseEvent -> {
                                t.setEffect(null);
                        });
                        t.setOnMouseClicked(mouseEvent -> {
                                setCenter(null);
                                center.add(new ScrollPane(panes.get(t)), 0, 0);
                                center.setAlignment(Pos.CENTER);
                                setCenter(center);
                        });
                }
        }

        /**
         * The `initButtonGrid` method creates a `GridPane` for the buttons and adds the buttons to it.
         * The `GridPane` is aligned to the center.
         *
         * @param buttons - the list of buttons to add to the `GridPane`
         *
         * @return - the `GridPane` with the buttons
         */
        private GridPane initButtonGrid(ArrayList<StackPane> buttons) {
                GridPane buttonGrid = new GridPane();
                for (int i = 0; i < buttons.size(); i++)
                        buttonGrid.add(buttons.get(i), 0, i);
                buttonGrid.setAlignment(Pos.CENTER);

                return buttonGrid;
        }

        /**
         * The `createFullPane` method creates the full `GridPane` that contains the bracket trees and
         * final four bracket node `Pane`. The bracket trees and final four `Pane` are added to the full
         * `GridPane` in the appropriate layout. The full `GridPane` is aligned to the center.
         *
         * @param roots - the list of bracket trees
         * @param finalPane - the final four bracket node `Pane`
         *
         * @return - the full `GridPane`
         */
        private GridPane createFullPane(ArrayList<BracketTree> roots, Pane finalPane) {
                GridPane fullPane = new GridPane();
                GridPane gp1 = new GridPane();
                gp1.add(roots.get(0), 0, 0);
                gp1.add(roots.get(1), 0, 1);
                GridPane gp2 = new GridPane();
                gp2.add(roots.get(2), 0, 0);
                gp2.add(roots.get(3), 0, 1);
                gp2.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                fullPane.add(gp1, 0, 0);
                fullPane.add(finalPane, 1, 0, 1, 2);
                fullPane.add(gp2, 2, 0);
                fullPane.setAlignment(Pos.CENTER);

                return fullPane;
        }

        /**
         * The `createRoots` method creates the bracket trees and adds them to the `roots` list.
         * The `panes` map is updated with the buttons and their corresponding bracket trees.
         *
         * @param buttons - the list of buttons
         *
         * @return - the list of bracket trees
         */
        public ArrayList<BracketTree> createRoots(ArrayList<StackPane> buttons){
                ArrayList<BracketTree> roots = new ArrayList<>();
                for (int m = 0; m < buttons.size() - 1; m++) {
                        roots.add(new BracketTree(3 + m));
                        panes.put(buttons.get(m), roots.get(m));
                }
                return roots;
        }

        /**
         * The `createButtons` method creates the buttons and adds them to the `buttons` list.
         *
         * @return - the list of buttons
         */
        public ArrayList<StackPane> createButtons(){
                ArrayList<StackPane> buttons = new ArrayList<>();
                buttons.add(customButton("EAST"));
                buttons.add(customButton("WEST"));
                buttons.add(customButton("MIDWEST"));
                buttons.add(customButton("SOUTH"));
                buttons.add(customButton("FULL"));
                return buttons;
        }

        /**
         * Helpful method to retrieve our magical numbers
         *
         * @param root the root node (3,4,5,6)
         * @param pos  the position in the tree (8 (16) , 4 (8) , 2 (4) , 1 (2))
         * @return The list representing the valid values.
         */
        public ArrayList<Integer> helper(int root, int pos) {
                ArrayList<Integer> positions = new ArrayList<>();
                int base = 0;
                int tmp = (root * 2) + 1;
                if (pos == 8) {
                        base = 3;
                }else if (pos == 4) {
                        base = 2;
                }else if (pos == 2) {
                        base = 1;
                }
                for (int i = 0; i < base; i++) {
                        tmp = (tmp * 2) + 1;
                }
                for (int j = 0; j < pos * 2; j++) {
                        positions.add(tmp + j);
                }
                return positions; //                while ((tmp = ((location * 2) + 1)) <= 127) ;
        }

        /**
         * Sets the current bracket to,
         * @param target The bracket to replace currentBracket
         */
        public void setBracket(Bracket target) {
                currentBracket = target;
        }

        /**
         * Clears the subtree from,
         * @param position The position to clear after
         */
        public void clearSubtree(int position) {
                currentBracket.resetSubtree(position);
        }

        /**
         * Requests a message from current bracket to tell if the bracket
         * has been completed.
         * @return True if completed, false otherwise.
         */
        public boolean isComplete() {
                return currentBracket.isComplete();
        }

        /**
         * Returns a custom "Button" with specified
         * @param name The name of the button
         * @return pane The stack-pane "button"
         */
        private StackPane customButton(String name) {
                StackPane pane = new StackPane();
                Rectangle r = new Rectangle(100, 50, Color.TRANSPARENT);
                Text t = new Text(name);
                t.setTextAlignment(TextAlignment.CENTER);
                pane.getChildren().addAll(r, t);
                return pane;
        }

        /**
         * The `createFinalFour` method creates a `Pane` that holds the final four bracket nodes.
         * The final four bracket nodes are created using the `initializeBracketNode` method.
         * The final `Pane` is returned.
         *
         * @return - the final `Pane` with the final four bracket nodes
         */
        public Pane createFinalFour() {
                Pane finalPane = new Pane();

                BracketNode nodeFinal0 = initializeBracketNode(162, 300, 0);
                BracketNode nodeFinal1 = initializeBracketNode(75, 400, 1);
                BracketNode nodeFinal2 = initializeBracketNode(250, 400, 2);

                finalPane.getChildren().addAll(nodeFinal0, nodeFinal1, nodeFinal2);
                finalPane.setMinWidth(400.0);

                return finalPane;
        }

        /**
         * The `initializeBracketNode` method creates and initializes a bracket node.
         * The bracket node's name is set to the value at the given `index` in the `currentBracket` bracket.
         * The node is added to the `bracketMap` and `nodeMap`. Mouse event handlers are added to the node.
         *
         * @param x - the x coordinate of the bracket node
         * @param y - the y coordinate of the bracket node
         * @param index - the index of the node in the bracket
         *
         * @return - the initialized bracket node
         */
        private BracketNode initializeBracketNode(int x, int y, int index) {
                BracketNode node = new BracketNode(currentBracket.getBracket().get(index), x, y, 70, 0);

                node.setOnMouseClicked(clicked);
                node.setOnMouseDragEntered(enter);
                node.setOnMouseDragExited(exit);
                node.setStyle("-fx-border-color: red");

                bracketMap.put(node, index);
                nodeMap.put(index, node);

                return node;
        }

        /**
         * Creates the graphical representation of a subtree.
         */
        private class BracketTree extends Pane {
                private int location;

                public BracketTree(int location) {
                        this.location = location;
                        createVertices(420, 200, 100, 20, 0, 0);
                        createVertices(320, 119, 100, 200, 1, 0);
                        createVertices(220, 60, 100, 100, 2, 200);
                        createVertices(120, 35, 100, 50, 4, 100);
                        createVertices(20, 25, 100, 25, 8, 50);
                        for (BracketNode n : nodes) {
                                n.setOnMouseClicked(clicked);
                                n.setOnMouseEntered(enter);
                                n.setOnMouseExited(exit);
                        }
                }

                /**
                 * The secret sauce... well not really,
                 * Creates 3 lines in appropriate location unless it is the last line.
                 * Adds these lines and "BracketNodes" to the Pane of this inner class
                 */
                private void createVertices(int initialX, int initialY, int offsetX, int offsetY, int num, int increment) {

                        if (num == 0 && increment == 0) {
                                createSingleNode(initialX, initialY, offsetX);
                        } else {
                                createMultipleNodes(initialX, initialY, offsetX, offsetY, num, increment);
                        }

                }
                /**
                 * The `createSingleNode` method creates a single bracket node and adds it to the `nodes` list,
                 * as well as the `getChildren` list. The bracket node's name is set to the value at the current
                 * `location` in the `currentBracket` bracket. The `bracketMap` and `nodeMap` are also updated
                 * with the new node and its index.
                 *
                 * @param x - the x coordinate of the bracket node
                 * @param y - the y coordinate of the bracket node
                 * @param offsetX - the offset of the x coordinate
                 */
                private void createSingleNode(int x, int y, int offsetX) {
                        BracketNode last = new BracketNode("", x, y - 20, offsetX, 20);
                        nodes.add(last);
                        getChildren().addAll(new Line(x, y, x + offsetX, y), last);
                        last.setName(currentBracket.getBracket().get(location));
                        bracketMap.put(last, location);
                        nodeMap.put(location, last);
                }

                /**
                 * The `createMultipleNodes` method creates multiple bracket nodes and adds them to the `nodes` list,
                 * as well as the `getChildren` list. The bracket nodes are connected with lines. The `updateNodes` method
                 * is called to set the names of the nodes and update the `bracketMap` and `nodeMap`.
                 *
                 * @param x - the x coordinate of the bracket node
                 * @param y - the y coordinate of the bracket node
                 * @param offsetX - the offset of the x coordinate
                 * @param offsetY - the offset of the y coordinate
                 * @param num - the number of bracket nodes to create
                 * @param increment - the increment for the y coordinate
                 */
                private void createMultipleNodes(int x, int y, int offsetX, int offsetY, int num, int increment) {
                        ArrayList<BracketNode> nodeList = new ArrayList<>();

                        for (int i = 0; i < num; i++) {
                                Point2D topLeft = new Point2D(x, y);
                                Point2D topRight = new Point2D(x + offsetX, y);
                                Point2D bottomLeft = new Point2D(x, y + offsetY);
                                Point2D bottomRight = new Point2D(x + offsetX, y + offsetY);

                                BracketNode topNode = new BracketNode("", x, y - 20, offsetX, 20);
                                BracketNode bottomNode = new BracketNode("", x, y + (offsetY - 20), offsetX, 20);

                                nodeList.add(topNode);
                                nodeList.add(bottomNode);
                                nodes.add(topNode);
                                nodes.add(bottomNode);

                                Line top = new Line(topLeft.getX(), topLeft.getY(), topRight.getX(), topRight.getY());
                                Line bottom = new Line(bottomLeft.getX(), bottomLeft.getY(), bottomRight.getX(), bottomRight.getY());
                                Line right = new Line(topRight.getX(), topRight.getY(), bottomRight.getX(), bottomRight.getY());

                                getChildren().addAll(top, bottom, right, topNode, bottomNode);
                                y += increment;

                        }
                        updateNodes(nodeList, location, num);

                }

                /**

                 The updateNodes method sets the names of the bracket nodes in the nodeList and updates the
                 bracketMap and nodeMap with the new nodes and their indices. The indices of the nodes are
                 obtained using the helper method.
                 @param nodeList - the list of bracket nodes to update
                 @param location - the location in the bracket to start updating from
                 @param num - the number of bracket nodes to update
                 */
                private void updateNodes(ArrayList<BracketNode> nodeList, int location, int num){
                        ArrayList<Integer> indices = helper(location, num);
                        for (int i = 0; i < nodeList.size(); i++) {
                                BracketNode node = nodeList.get(i);
                                int index = indices.get(i);
                                node.setName(currentBracket.getBracket().get(index));
                                bracketMap.put(node, index);
                                nodeMap.put(index, node);
                        }
                }
        }

        /**
         * The BracketNode model for the Graphical display of the "Bracket"
         */
        private class BracketNode extends Pane {
                private String teamName;
                private Rectangle rect;
                private Label name;

                public BracketNode() {
                        teamName = "";
                        rect = new Rectangle(5, 2);
                        name = new Label(teamName);
                }

                /**
                 * Creates a BracketNode with,
                 *
                 * @param teamName The name if any
                 * @param x        The starting x location
                 * @param y        The starting y location
                 * @param rX       The width of the rectangle to fill pane
                 * @param rY       The height of the rectangle
                 */
                public BracketNode(String teamName, int x, int y, int rX, int rY) {
                        this.setLayoutX(x);
                        this.setLayoutY(y);
                        this.setMaxSize(rX, rY);
                        this.teamName = teamName;
                        rect = new Rectangle(rX, rY);
                        rect.setFill(Color.TRANSPARENT);
                        name = new Label(teamName);
                        name.setTranslateX(5);
                        getChildren().addAll(name, rect);
                }

                public String getName() {
                        return teamName;
                }

                /**
                 * @param teamName The name to assign to the node.
                 */
                public void setName(String teamName) {
                        this.teamName = teamName;
                        name.setText(teamName);
                }
        }
}