/**
 * This class 'TimeClockMMI' manage the diplay of time clock and the clocking manager.
 * The user can choose his name and send his clocking to the server so that it sends it to the main application.
 *
 */

package TimeClock;

import Check.CheckType;
import Employee.Employee;
import Serveur.Message;
import Serialization.Serialization;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.TextField;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TimeClockMMI extends Application {

    // - - - ATTRIBUTES - - -
    /**
     * this list stock the checks before send it to the server
     */
    private static List<Message> clockingBuffer = Collections.synchronizedList(new ArrayList<>());

    /**
     * this attribute is the IP address of server
     */
    private static String serverIp = "localhost";

    /**
     * this attribute is the server port
     */
    private static int serverPort = 5005;

    /**
     * this attribute is the time that we take for the refresh
     */
    private static int refreshSeconds = 5;

    /**
     * the auth token for "secure" TCP communication
     * this will be developped further in the report but a hard-coded string
     * is a good compromise when the app is intended for the staff
     * 
     * SSL implementation would be better but we don't have enough time for that
     * plus it's a little overkill
     */
    private static final String AUTH_TOKEN = "Viv3P0LYTECH2026!!!";

    // - - - SETTERS - - -

    /**
     * it's the setter of server IP
     * @param ip : String
     */
    public static synchronized void setServerIp(String ip) {serverIp = ip;}

    /**
     * it's the setter of server port
     * @param port : int
     */
    public static synchronized void setServerPort(int port)
    {
        //We check that the port in parameter exits
        if (port > 0 && port <= 65535)
            serverPort = port;
    }

    /**
     * it's the setter for the refresh time
     * @param seconds : int
     */
    public static synchronized void setRefreshSeconds(int seconds)
    {
        //We can check that the time it's positive (a negative refresh time is impossible)
        if (seconds > 0)
            refreshSeconds = seconds;
    }

    /**
     * it's the setter for the rclocking buffer
     * @param clockingBuffer : List<Message>
     */
    public static void setClockingBuffer(List<Message> clockingBuffer) { TimeClockMMI.clockingBuffer = clockingBuffer; }

    // - - - GETTER - - -
    /**
     * getter for the refresh time
     * @return int
     */
    public static synchronized int getRefreshSeconds() {return refreshSeconds;}

    /**
     * return the buffer oh the clocking
     * @return clockingBuffer
     */
    public static List<Message> getClockingBuffer() { return clockingBuffer; }

    // - - - STATIC BLOCK - - -
    /**
     * static block to restore the data in the 'buffer_pointeuse.ser' file and be able to display it afterwards
     */
    static
    {
        @SuppressWarnings("unchecked")
        List<Message> loadBuffer = (List<Message>) Serialization.loadObject("buffer_pointeuse.ser");
        if (loadBuffer != null) //We check that the loadBuffer has been load
        {
            clockingBuffer.addAll(loadBuffer);
            System.out.println("clocking restored : " + clockingBuffer.size());
        }
    }

    // - - - METHODS - - - 
    /**
     * main for lunch the javaFx application
     * @param args : String
     */
    public static void main(String[] args)
    {
        launch(args);
    }

    /**
     * This method initializes and then displays the interface of the time clock emulator in JavaFX
     * So it allows you to configure the time display to have the time of attendance.
     * It updates the files. ser
     * It allows you to configure the information of the server with which it communicates.
     * @param primaryStage : Stage : the primary window for the application
     */
    @Override
    public void start(Stage primaryStage)
    {
        //start the thread who send the information to server.
        startThread();

        primaryStage.setTitle("Emulator Time Clock");

        //display the date and time
        Label labelDate = new Label("loading..."); //the time that the date is displayed
        labelDate.setFont(Font.font("Arial", 20));

        Label labelTime = new Label("loading..."); //the time that the hour is displayed
        labelTime.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        //this is the time rounded to a quarter of an hour
        Label labelRoundHeure = new Label("loading...");
        labelRoundHeure.setFont(Font.font("Arial", 12));

        //the list of employees (dynamic) who can point
        ComboBox<Employee> choiceEmployer = new ComboBox<>();

        //Here, we load the company’s employees using the 'employees.ser' file.
        Runnable refreshEmployees = () -> {
            @SuppressWarnings("unchecked")
            //We load the 'employees.ser' file
            List<Employee> employeeList = (List<Employee>) Serialization.loadObject("employees.ser");
            if (employeeList != null && !employeeList.isEmpty())  //we check that there are employees and it's well load
            {
                //we save the employee selected
                Employee employeeSelected = choiceEmployer.getValue();

                //we update the dynamic list of employee
                choiceEmployer.setItems(FXCollections.observableArrayList(employeeList));

                //we manage the case where there are no selected employees
                if (employeeSelected == null)
                {
                    //so we chose the first employee of the list
                    choiceEmployer.getSelectionModel().selectFirst();
                    return;
                }

                //Here we recup the employee selected by the user, which we will stock in a variable of type 'Employee'.
                //This allows you to keep the employee selected even if there is a refresh. 
                //else, the display returns to the first employer in the list.
                Employee sameEmployee = null;
                for (Employee employeeInProgress : employeeList)
                {
                    //if the id is the same
                    if (employeeInProgress.getEmployeeId() != null && employeeInProgress.getEmployeeId().equals(employeeSelected.getEmployeeId()))
                    {
                        sameEmployee = employeeInProgress;
                        break; //we leave the loop because we found the employer
                    }
                }

                if (sameEmployee != null) //if the employee has been found, he is reassigned his place
                    choiceEmployer.setValue(sameEmployee);
                else //else it’s the first one
                    choiceEmployer.getSelectionModel().selectFirst();
            }
        };

        //start the refresh employee
        refreshEmployees.run();

        //we refresh the employees who are selectable every five seconds
        Timeline autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            refreshEmployees.run();
        }));
        //these commands allow to refresh every five seconds automatically
        autoRefreshTimeline.setCycleCount(Animation.INDEFINITE);
        autoRefreshTimeline.play();

        Button checkButton = new Button("Check in/out");
        //Button btnRefresh = new Button("-><-");
        Button settingsButton = new Button("⚙");

        //manage the event on the settings button
        settingsButton.setOnAction(event -> {

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Server configuration");

            //the input fields of the IP and the port
            TextField ipField = new TextField(serverIp);
            TextField portField = new TextField(String.valueOf(serverPort));

            //grid for the display
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            grid.add(new Label("IP :"), 0, 0);
            grid.add(ipField, 1, 0);

            grid.add(new Label("Port :"), 0, 1);
            grid.add(portField, 1, 1);

            dialog.getDialogPane().setContent(grid);

            //the button for save the changement
            ButtonType saveButton = new ButtonType("Save");
            dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

            Optional<ButtonType> resultChange = dialog.showAndWait();

            if (resultChange.isPresent() && resultChange.get() == saveButton) {
                try { //we try to change the information port and IP with that the user enter
                    setServerIp(ipField.getText());
                    setServerPort(Integer.parseInt(portField.getText()));

                    System.out.println("New configuration : " + serverIp + ":" + serverPort);

                } catch (NumberFormatException error) { //if it's invalid, we manage the error
                    System.out.println("Invalid port.");
                }
            }
        });


        //the center pannel for the time and date display
        VBox timeDisplay = new VBox(10);
        timeDisplay.setAlignment(Pos.CENTER);
        timeDisplay.setPadding(new Insets(20, 0, 0, 0));

        //add of the labels in the display
        timeDisplay.getChildren().addAll(labelDate, labelTime, labelRoundHeure);

        //the top bar with parameter button
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT); //in the top right corner
        topBar.setPadding(new Insets(10));
        topBar.getChildren().add(settingsButton); //we add the settings button

        //container in the center of display
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(topBar, timeDisplay);

        //bottom bar with the list and the button check
        HBox bottomBar = new HBox(15);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(0, 0, 20, 0));
        bottomBar.getChildren().addAll(choiceEmployer, checkButton);

        //principal container with the top and bottom container
        BorderPane root = new BorderPane();
        root.setCenter(topContainer);
        root.setBottom(bottomBar);;

        //manage the event on the check button
        checkButton.setOnAction(event -> {
            Employee employeeSelect = choiceEmployer.getValue(); //we recup the employee selected
            //we check that teh employee selected is not null
            if (employeeSelect != null)
            {
                UUID employeeSelectId = employeeSelect.getEmployeeId();

                LocalDateTime timeNow = LocalDateTime.now(); //we recup the time

                //we round up the time to a quarter of an hour
                int moduloTime = timeNow.getMinute() % 15;
                int minutesToAdd = (moduloTime < 8) ? -moduloTime : (15 - moduloTime);
                LocalDateTime roundTime = timeNow.plusMinutes(minutesToAdd).truncatedTo(ChronoUnit.MINUTES);

                //we create a message with the class 'Message' for save the check and send to the server later
                Message messageCheck = new Message(employeeSelectId, CheckType.OUT, roundTime, AUTH_TOKEN);                clockingBuffer.add(messageCheck); //add to the buffer

                System.out.println("save clocking for " + employeeSelect.getFirstName());
            }
        });

        //manage the event on the dropdown list
        choiceEmployer.setOnAction(event -> {
            Employee employe = choiceEmployer.getValue(); //we recup the employee choose
            if (employe != null) //if not null, we prevent the user in the terminal for his choice
                System.out.println("you have chosen : " + employe.getFirstName() + " " + employe.getLastName());
        });

        //manage the event for update the display for the time round to a quarter of an hour
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {

            LocalDateTime timeNow = LocalDateTime.now(); //we recup the time

            //time formatter hh:mm:ss
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.FRENCH);
            labelTime.setText(timeNow.format(timeFormatter));

            //exact date
            DateTimeFormatter exactDate = DateTimeFormatter.ofPattern("EEEE d MMMM, yyyy", Locale.FRENCH);
            labelDate.setText(timeNow.format(exactDate));

            //we round up the time to a quarter of an hour
            int moduloTime = timeNow.getMinute() % 15;
            int minutesToAdd = (moduloTime < 8) ? -moduloTime : (15 - moduloTime);
            LocalDateTime roundHour = timeNow.plusMinutes(minutesToAdd).truncatedTo(ChronoUnit.MINUTES);

            //we formate the time to a quarter of an hour
            DateTimeFormatter dateFormatterRoundQuarter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.FRENCH);
            labelRoundHeure.setText("the time at quarter of an hour is  " + roundHour.format(dateFormatterRoundQuarter));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        //Graceful serialization saving when user closes window frame
        primaryStage.setOnCloseRequest(e -> {
            Serialization.saveObject(new ArrayList<>(clockingBuffer), "buffer_pointeuse.ser");
            System.out.println("Buffer sauvegardé avant fermeture.");
            Platform.exit(); //stop JavaFx
            System.exit(0); //stop all of the processus
        });

        //Create and display the final scene
        Scene finalScene = new Scene(root, 400, 300);
        primaryStage.setScene(finalScene);
        primaryStage.show(); //we display
    }


    /**
     * run the server for exchange the data between the time clock and the principal application.
     * They will exchange the check for the principal application display and manage it
     */
    private static void startThread() {
        //we create a thread for exchange data without the principal application
        Thread threadEnvoi = new Thread(() -> {
            //while the program turn, the server turn also
            while (true)
            {
                try
                {
                    Thread.sleep(getRefreshSeconds() * 1000L); //
                } catch (InterruptedException error) { //we manage the potentials errors
                    error.printStackTrace();
                    break;
                }

                //if the buffer of clocking contains clocking not sent, we send them
                if (!clockingBuffer.isEmpty()) {
                    System.out.println("attempt to send (There are " + clockingBuffer.size() + " message(s) waiting)");

                    //while the buffer isn't empty, we send the clocking to the principal application
                    while (!clockingBuffer.isEmpty()) {
                        Message messageToSend = clockingBuffer.get(0); //we recup the first message (check) of the list
                        //try to open a server connection
                        try (Socket socket = new Socket(serverIp, serverPort); ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()))
                        {

                            oos.writeObject(messageToSend); //send the Check in the network with the server
                            oos.flush(); //force sends it (just in case)

                            //we remove the first element because he has been sent
                            clockingBuffer.remove(0);
                            System.out.println("Message sent to the server");

                        } catch (Exception error) {
                            System.out.println("server unreachable. We will try it again in the next try");
                            break;
                        }
                    }
                }
            }
        });

        //Allows to the Thread to automatically stop if the JavaFX application closes
        threadEnvoi.setDaemon(true);
        threadEnvoi.start();
    }
}