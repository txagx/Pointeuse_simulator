/**
 * The 'ClockingManager' class allows you to manipulate a pointer to display it live,
 * as well as save it in a file .ser.
 */

package PrincipalApplication;

import Check.Check;
import Check.CheckType;
import Serialization.Serialization;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClockingManager implements Serializable {

    //- - - ATTRIBUTES - - -
    /**
     * variable for the file name for serialized clocking
     * 'static' because it's unique and 'final' for that no one modify it
     */
    private static final String fileName = "pointages.ser";

    /**
     * list who contains clocking, 'ObservableList' allows the list to be dynamic with the interface
     * 'transient' because 'ObservableList' it's not serializable (he contains JavaFx code)
     * 'final' for that no one modify it
     */
    private final transient ObservableList<Check> clockingList = FXCollections.observableArrayList();

    /**
     * 'clockingList' isn't serializable so 'globalHistory' is the list of clocking for the serialisartion (use for the save and the read)
     * 'final' for that no one modify it
     */
    private final List<Check> globalHistory = new ArrayList<>();

    //- - - CONSTRUCTOR - - -
    /**
     * Constructs a new ClockingManager object
     */
    public ClockingManager()
    {
        //We read the clocking file with the Serialisation class, then we cast it so that it is a list of check
        List<Check> loadHistory = (List<Check>) Serialization.loadObject(fileName);

        //We check the list of check
        if (loadHistory != null && !loadHistory.isEmpty()) {
            //we add all the check in the both lists
            this.globalHistory.addAll(loadHistory);
            this.clockingList.addAll(loadHistory);
            System.out.println("Clocking history restored : " + globalHistory.size() + " elements.");
        } else {
            System.out.println("No clocking history found => Creation of a new file");
        }
    }

    //- - - GETTER - - -
    /**
     * we return the clocking list
     * @return clockingList : the list of clocking for JavaFx display
     */
    public ObservableList<Check> getClockingList() { return clockingList; }

    /**
     * we return the global history list
     * @return globalHistory : the list of clocking for the file .ser
     */
    public List<Check> getGlobalHistory() { return globalHistory; }

    //- - - METHODS - - -
    /**
     * Builds a Check with the correct CheckType calculated automatically.
     * @param employeeId : UUID
     * @param date : LocalDate
     * @param time : LocalTime
     * @return Check : object Check class
     */
    public Check createAutomaticClocking(UUID employeeId, LocalDate date, LocalTime time)
    {
        CheckType nextType = determineNextType(employeeId, date);
        return new Check(date, time, nextType, employeeId); //we return a new check
    }

    /**
     * This method allows to determine the type of check
     * We take several parameters for determine the type (ex : the date : today or tomorrow)
     * @param employeeId : UUID
     * @param dateNewClocking : LocalDate
     * @return Check : object Check class
     */
    private CheckType determineNextType(UUID employeeId, LocalDate dateNewClocking)
    {
        Check lastClocking = getLastCheckForEmployee(employeeId);

        //if no previous clocking, it's IN
        if (lastClocking == null) {
            return CheckType.IN;
        }

        LocalDate dateLastClocking = lastClocking.getDate();
        CheckType typeLastClocking = lastClocking.getCheckType();

        //two cases :
        //it's today, we change the type
        if (dateNewClocking.isEqual(dateLastClocking))
        {
            //if the last clocking is OUT, then it's IN
            //else it's OUT
            return (typeLastClocking == CheckType.IN) ? CheckType.OUT : CheckType.IN;
        }

        //it's the next day
        if (dateNewClocking.isAfter(dateLastClocking))
        {

            //If the last clocking from the previous day was IN, then there is a oversight
            if (typeLastClocking == CheckType.IN)
            {
                System.out.println("Attention : oversight of clocking OUT undetected for the employee " + employeeId + " the " + dateLastClocking);
            }
            //In any case, the first time on a new day is an IN.
            return CheckType.IN;
        }

        //default security
        return CheckType.IN;
    }

    /**
     * return the most recent clocking record for a given employee in parameter.
     * @param employeeId : UUID
     * @return check or null
     */
    private Check getLastCheckForEmployee(UUID employeeId) {
        //we go through the list upside down to find the most recent one.
        for (int i = globalHistory.size() - 1; i >= 0; i--)
        {
            Check check = globalHistory.get(i); //we recup the check at the position i
            if (check.getEmployeeUUID().equals(employeeId)) //if the employee id in the check is the same as the one in the parameter
            {
                return check; //we return the check
            }
        }
        return null; //else we return null because there is no one who matches
    }

    /**
     * add a cloking in the both lists of check and we update the display of the main IHM
     * @param check : Check
     */
    public synchronized void addClocking(Check check)
    {
        globalHistory.add(check); //we add the check in the global history
        saveData(); //we save the check in the clocking file with the serialisation

        //we try to see if JavaFx is launched to add the check
        try
        {
            javafx.application.Platform.runLater(() -> {
                clockingList.add(check);
            });
        } catch (IllegalStateException error) {
            //if JavaFx is not launched, we ignore
        }
    }

    /**
     * we save the data in the clocking file with the help of Serialization class
     */
    private void saveData()
    {
        Serialization.saveObject(new ArrayList<>(globalHistory), fileName);
    }

    /**
     * we delete a clocking both lists
     * @param check : Check
     */
    public void deleteClocking(Check check)
    {
        //We check that 'selectedCheck' isn't null
        if (check == null)
        {
            displaySelectionAlert();
            return;
        }

        globalHistory.remove(check); //we delete 'check' in global history
        //if JavaFx is launched, we delete 'check' in clocking list
        javafx.application.Platform.runLater(() -> {
            clockingList.remove(check);
        });

        saveData(); //we save the delete in clocking file
    }

    /**
     * we allow to modify the clocking in both lists
     * @param editCheck : Check
     */
    public void editClocking(Check editCheck)
    {
        //we check the parameter 'editCheck' isn't null
        if (editCheck == null)
        {
            displaySelectionAlert();
            return;
        }

        //We open a small graphic window that allows us to see the different types of checks (here IN or OUT)
        javafx.scene.control.ChoiceDialog<CheckType> dialog = new javafx.scene.control.ChoiceDialog<>(
                editCheck.getCheckType(),
                java.util.Arrays.asList(CheckType.values())
        );

        dialog.setTitle("Modifier un pointage");
        dialog.setContentText("Choisir le nouveau type :");

        //we wait that the user choose and valid his choice
        java.util.Optional<CheckType> result = dialog.showAndWait();

        //the choice of user is stocked in newType
        result.ifPresent(newType -> {
            //we look for the position ot the parameter in the both lists
            int indexHistory = globalHistory.indexOf(editCheck);
            int indexClocking = clockingList.indexOf(editCheck);

            //Now, we edit the type of the parameter with the setter of the CheckType class
            editCheck.setCheckType(newType);

            //update of the global history
            if (indexHistory != -1)
            {
                globalHistory.set(indexHistory, editCheck);
            }

            //update the display and the clocking list
            if (indexClocking != -1)
            {
                javafx.application.Platform.runLater(() -> {
                    clockingList.set(indexClocking, editCheck);
                });
            }

            saveData(); //we save the data bacause we change the type of the parameter
        });
    }

    /**
     * we allow to manage the different error and alert the user
     * So the alert is detailed
     */
    private void displaySelectionAlert()
    {
        //Platform.runLater ensures that the pop-up opens on the main IHM
        //we manage the various possible errors
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Selection Required");
            alert.setHeaderText(null);
            alert.setContentText("Select a clocking in the table !");
            alert.showAndWait();
        });
    }
}