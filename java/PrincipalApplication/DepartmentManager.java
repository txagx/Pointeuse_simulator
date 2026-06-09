/**
 * The 'DepartmentManager' class allows you to manipulate a list to display your department in your principal interface.
 * It also contains all the function to add/remove/modify a department.
 */
package PrincipalApplication;

import Employee.Employee;
import Entreprise.Department;
import Serialization.Serialization;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

public class DepartmentManager {

    //- - - ATTRIBUTES - - -

    /**
     * list of departments displayed in JavaFX
     */
    private ObservableList<Department> departmentList;

    /**
     * variable for the file name for serialized clocking
     * 'static' because it's unique and 'final' for that no one modify it
     */
    private static final String fileName = "departments.ser";

    //- - - CONSTRUCTOR - - -

    /**
     * Constructs a new DepartmentManager object
     */
    public DepartmentManager(List<Department> initialDepartments) {

        this.departmentList = FXCollections.observableArrayList();

        //we try to load previously saved departments from file
        List<Department> loaded = (List<Department>) Serialization.loadObject(fileName);

        //if data exists, we restore it, otherwise we use initial data
        if (loaded != null && !loaded.isEmpty()) {
            this.departmentList.addAll(loaded);
        } else {
            this.departmentList.addAll(initialDepartments);
        }
    }

    //- - - GETTER - - -
    /**
     * we return the department list
     * @return departmentList : the list of department for JavaFx display
     */
    public ObservableList<Department> getDepartmentList() {
        return departmentList;
    }

    //- - - METHODS - - -
    /**
     * Opens a dialog to add a new department
     */
    public void addDepartment() {

        // creation and personalization of the dialog
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Department");
        dialog.setHeaderText(null);
        dialog.setContentText("Department name :");

        dialog.showAndWait().ifPresent(name -> {
            //we check that the input is not empty
            if (!name.trim().isEmpty()) {
                //we create and add the new department
                departmentList.add(new Department(name));
                //we save the updated list
                save();
            }
        });
    }

    /**
     * Removes a department and removes its reference from employees
     * @param dep : Department
     * @param employees : ObservableList<Employee>
     */
    public void deleteDepartment(Department dep, ObservableList<Employee> employees) {
        //check if dep is null -> safety check
        if (dep == null) return;
        //we remove department reference from all employees linked to it
        //emp.department set to null to display N/A in the future
        for (Employee emp : employees) {
            if (emp.getDepartment() != null &&
                    emp.getDepartment().equals(dep)) {
                emp.setDepartment(null);
            }
        }

        //we remove the department from the list
        departmentList.remove(dep);
        //we save changes
        save();
    }

    /**
     * we save the data in the department file with the help of Serialization class
     */
    private void save() {
        Serialization.saveObject(new ArrayList<>(departmentList), fileName);
    }
}