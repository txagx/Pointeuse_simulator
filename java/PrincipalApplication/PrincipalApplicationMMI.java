/**
 * The 'PrincipalApplicationMMI' is the main interface. It allows us to see our list of employee, check and department.
 * We can add, modify or delete our employee and department. We can also modify all of the check.
 */
package PrincipalApplication;

import Check.Check;
import Check.CheckType;
import Employee.Employee;
import Entreprise.Department;
import Serveur.Server;
import Serialization.Serialization;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrincipalApplicationMMI extends Application {
    //- - - ATTRIBUTES - - -

    /**
     * name of the file of department
     */
    private static final String DEPARTMENT_FILE = "departments.ser";

    //- - - METHODS - - -

    /**
     * It's the main function of our interface, it launches the interface and do all we need to do to have a functional interface.
     * @param stage : Stage
     */
    @Override
    public void start(Stage stage) {

        // Main container of our application
        // It displays the navbar and allows to change pages dynamically
        BorderPane root = new BorderPane();

        /* ==================== Initialisation of the backend ==================== */

        // Manager responsible for stockage and managing checks
        ClockingManager clockingManager = new ClockingManager();

        // ObservableList containing the departments
        ObservableList<Department> departments = FXCollections.observableArrayList();

        // Loading of previously saved departments
        List<Department> loaded = (List<Department>) Serialization.loadObject(DEPARTMENT_FILE);

        // if loaded exists, it's added to our ObservableList
        if (loaded != null) {
            departments.addAll(loaded);
        }

        // Manager of our employees which use our department list
        EmployeeManager employeeManager = new EmployeeManager(departments);

        // Start of the server
        Server server = new Server(clockingManager);
        server.start();

        /* ==================== NAVBAR ==================== */

        // Buttons that allows the navigation between pages
        Button btnEmployee = new Button("Employee");
        Button btnCheck = new Button("Check");
        Button btnDepartment = new Button("Department");

        // Application of style defined in our CSS
        btnEmployee.getStyleClass().add("nav-button");
        btnCheck.getStyleClass().add("nav-button");
        btnDepartment.getStyleClass().add("nav-button");

        // Horizontal organization of our buttons
        HBox navbar = new HBox(15, btnEmployee, btnCheck, btnDepartment);
        navbar.getStyleClass().add("navbar");

        /* ==================== Table Employees ==================== */

        // Table that display all of our employee
        TableView<Employee> tableEmployee = new TableView<>();

        // Columns containing major information for an employee
        TableColumn<Employee, String> colEmpId = new TableColumn<>("UUID");
        TableColumn<Employee, String> colFirstName = new TableColumn<>("First Name");
        TableColumn<Employee, String> colLastName = new TableColumn<>("Last Name");
        TableColumn<Employee, String> colDepartment = new TableColumn<>("Department");

        // Association between our columns and attributes of an employee
        colEmpId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        // Display of the name of a department
        // If there is no department, N/A is displayed
        colDepartment.setCellValueFactory(cellData -> {
            Department dep = cellData.getValue().getDepartment();
            String name = (dep == null || dep.getDepartment() == null) ? "N/A" : dep.getDepartment();
            return new javafx.beans.property.SimpleStringProperty(name);
        });

        // Adding the columns to our table
        tableEmployee.getColumns().addAll(colEmpId, colFirstName, colLastName, colDepartment);

        // Linking the table with the observableList of employees.
        tableEmployee.setItems(employeeManager.getEmployeeList());
        tableEmployee.setPrefHeight(500);
        // The table takes all the free horizontal spaces
        tableEmployee.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Buttons to manages the slice of life of an employee
        Button btnAddEmployee = new Button("Add Employee");
        Button btnEditEmployee = new Button("Modify Employee");
        Button btnDeleteEmployee = new Button("Delete Employee");

        HBox employeeActions = new HBox(10, btnAddEmployee, btnEditEmployee, btnDeleteEmployee);

        // Opening of the adding fen
        btnAddEmployee.setOnAction(e -> employeeManager.addEmployee());

        // Modifying of the employee currently selected
        btnEditEmployee.setOnAction(e -> {
            Employee selection = tableEmployee.getSelectionModel().getSelectedItem();
            employeeManager.modifyEmployee(selection);
        });

        // Deleting of the selected employee
        btnDeleteEmployee.setOnAction(e -> {
            Employee selection = tableEmployee.getSelectionModel().getSelectedItem();
            employeeManager.deleteEmployee(selection);
        });

        PlanningService planningService = new PlanningService();

        // Container that will display the visual representation of the schedule
        HBox visualScheduleBar = new HBox();

        // Label displaying information about the selected employee schedule
        Label lblScheduleInfo = new Label("Select an employee to display their schedule");

        lblScheduleInfo.setStyle("-fx-font-weight: bold;" + "-fx-text-fill: #5C4033;" + "-fx-font-size: 14px;");

        // Container containing all day selection buttons
        HBox daySelector = new HBox(10);

        // Group allowing only one day to be selected at a time
        ToggleGroup dayGroup = new ToggleGroup();

        // Array containing all days of the week
        java.time.DayOfWeek[] daysOfWeek = {
                java.time.DayOfWeek.MONDAY,
                java.time.DayOfWeek.TUESDAY,
                java.time.DayOfWeek.WEDNESDAY,
                java.time.DayOfWeek.THURSDAY,
                java.time.DayOfWeek.FRIDAY,
                java.time.DayOfWeek.SATURDAY,
                java.time.DayOfWeek.SUNDAY
        };

        // Names displayed on the buttons
        String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        // Creation of one button for each day of the week
        for (int i = 0; i < daysOfWeek.length; i++) {
            ToggleButton btnDay = new ToggleButton(dayNames[i]);

            // Linking the button to the ToggleGroup
            btnDay.setToggleGroup(dayGroup);

            // Storing the corresponding day inside the button
            btnDay.setUserData(daysOfWeek[i]);

            // Monday selected by default
            if (i == 0) btnDay.setSelected(true);

            daySelector.getChildren().add(btnDay);
        }

        // Refreshing the schedule when another employee is selected
        tableEmployee.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateVisualSchedule(tableEmployee, dayGroup, planningService, visualScheduleBar, lblScheduleInfo);
        });

        // Refreshing the schedule when another day is selected
        dayGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                // Preventing no day from being selected
                dayGroup.selectToggle(oldVal);
            } else {
                updateVisualSchedule(tableEmployee, dayGroup, planningService, visualScheduleBar, lblScheduleInfo);
            }
        });

        // Title of the employee page
        Label employeeTitle = new Label("Employees");
        employeeTitle.getStyleClass().add("page-title");

        VBox pageEmployee = new VBox(
                15,
                employeeTitle,
                employeeActions,
                tableEmployee,
                new Separator(),
                daySelector,
                lblScheduleInfo,
                visualScheduleBar
        );

        pageEmployee.setPadding(new Insets(15));

        /* ==================== TABLE CHECK ==================== */

        // Table displaying all checks
        TableView<Check> tablePointage = new TableView<>();

        // Columns containing employee information
        TableColumn<Check, String> colCheckFirstName = new TableColumn<>("First Name");
        TableColumn<Check, String> colCheckLastName = new TableColumn<>("Last Name");
        TableColumn<Check, String> colCheckDept = new TableColumn<>("Department");

        // Columns containing check information
        TableColumn<Check, LocalDate> colDate = new TableColumn<>("Date");
        TableColumn<Check, LocalTime> colTime = new TableColumn<>("Time");
        TableColumn<Check, CheckType> colType = new TableColumn<>("Type");

        // Retrieving the employee first name from its UUID
        colCheckFirstName.setCellValueFactory(cellData -> {
            UUID empId = cellData.getValue().getEmployeeUUID();
            Employee emp = findEmployeeById(employeeManager.getEmployeeList(), empId);
            return new javafx.beans.property.SimpleStringProperty(emp != null ? emp.getFirstName() : "Unknown");
        });

        // Retrieving the employee last name from its UUID
        colCheckLastName.setCellValueFactory(cellData -> {
            UUID empId = cellData.getValue().getEmployeeUUID();
            Employee emp = findEmployeeById(employeeManager.getEmployeeList(), empId);
            return new javafx.beans.property.SimpleStringProperty(emp != null ? emp.getLastName() : "Unknown");
        });

        // Retrieving the employee department from its UUID
        colCheckDept.setCellValueFactory(cellData -> {
            UUID empId = cellData.getValue().getEmployeeUUID();
            Employee emp = findEmployeeById(employeeManager.getEmployeeList(), empId);

            return new javafx.beans.property.SimpleStringProperty(
                    (emp != null && emp.getDepartment() != null)
                            ? emp.getDepartment().getDepartment()
                            : "N/A"
            );
        });

        // Linking columns with Check attributes
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colType.setCellValueFactory(new PropertyValueFactory<>("checkType"));

        // Adding all columns to the table
        tablePointage.getColumns().addAll(colCheckFirstName, colCheckLastName, colCheckDept, colDate, colTime, colType);

        
        // new filteredCheckList on the main data list
        javafx.collections.transformation.FilteredList<Check> filteredCheckList = 
        new javafx.collections.transformation.FilteredList<>(clockingManager.getClockingList(), p -> true);

        // linking
        tablePointage.setItems(filteredCheckList);
        
        // UI elements: combobox, datepicker
        ComboBox<Employee> filterEmployee = new ComboBox<>();
        filterEmployee.setItems(employeeManager.getEmployeeList());
        filterEmployee.setPromptText("Filtrer par employé");

        ComboBox<Department> filterDepartment = new ComboBox<>();
        filterDepartment.setItems(departments);
        filterDepartment.setPromptText("Filtrer par département");

        DatePicker filterDate = new DatePicker();
        filterDate.setPromptText("Filtrer par date");

        Button btnClearFilters = new Button("Réinitialiser");

        // horizontal bar to align data
        HBox filterBar = new HBox(10, filterEmployee, filterDepartment, filterDate, btnClearFilters);
        filterBar.setPadding(new Insets(10, 0, 10, 0));

        // filtering logic
        Runnable updatePredicate = () -> {
            Employee selectedEmp = filterEmployee.getValue();
            Department selectedDept = filterDepartment.getValue();
            LocalDate selectedDate = filterDate.getValue();

            filteredCheckList.setPredicate(check -> {
                // per employee
                if (selectedEmp != null && !check.getEmployeeUUID().equals(selectedEmp.getEmployeeId())) {
                    return false;
                }
                // per dept
                if (selectedDept != null) {
                    Employee emp = findEmployeeById(employeeManager.getEmployeeList(), check.getEmployeeUUID());
                    if (emp == null || emp.getDepartment() == null || !emp.getDepartment().equals(selectedDept)) {
                        return false;
                    }
                }
                // per date
                if (selectedDate != null && !check.getDate().isEqual(selectedDate)) {
                    return false;
                }
                return true; // elt matching all active filters
            });
        };

        // filter as soon as a value is modified
        filterEmployee.setOnAction(e -> updatePredicate.run());
        filterDepartment.setOnAction(e -> updatePredicate.run());
        filterDate.setOnAction(e -> updatePredicate.run());

        // empty filters button
        btnClearFilters.setOnAction(e -> {
            filterEmployee.setValue(null);
            filterDepartment.setValue(null);
            filterDate.setValue(null);
            filteredCheckList.setPredicate(p -> true);
        });

        // Custom row coloring according to employee schedule
        tablePointage.setRowFactory(tv -> new TableRow<Check>() {

            @Override
            protected void updateItem(Check check, boolean empty) {
                super.updateItem(check, empty);

                // Empty rows are not colored
                if (check == null || empty) {
                    setStyle("");
                    return;
                }

                // Retrieving the employee linked to the check
                Employee emp = findEmployeeById(
                        employeeManager.getEmployeeList(),
                        check.getEmployeeUUID()
                );

                // If no planning exists, default style is applied
                if (emp == null || emp.getPlanning() == null) {
                    setStyle("");
                    return;
                }

                // Retrieving the day corresponding to the check
                java.time.DayOfWeek day = check.getDate().getDayOfWeek();
                Planning.WorkDay workDay = emp.getPlanning().getWorkDay(day);

                // Red color if the employee is not supposed to work this day
                if (workDay == null) {
                    setStyle("-fx-background-color: #ff4d4d;");
                    return;
                }

                LocalTime start = workDay.getStartTime();
                LocalTime end = workDay.getEndTime();

                LocalTime time = check.getTime();

                // Orange color if the check is outside working hours
                if (time.isBefore(start) || time.isAfter(end)) {
                    setStyle("-fx-background-color: #ffb84d;");
                    return;
                }

                // Normal style if everything is valid
                setStyle("");
            }
        });

        tablePointage.setPrefHeight(500);

        // The table takes all the free horizontal spaces
        tablePointage.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Buttons allowing modification and deletion of checks
        Button btnEditCheck = new Button("Modify Check");
        Button btnDeleteCheck = new Button("Delete Check");

        HBox checkActions = new HBox(10, btnEditCheck, btnDeleteCheck);

        // Opening the modification window
        btnEditCheck.setOnAction(e -> {
            Check selection = tablePointage.getSelectionModel().getSelectedItem();
            clockingManager.editClocking(selection);
        });

        // Deleting the selected check
        btnDeleteCheck.setOnAction(e -> {
            Check selection = tablePointage.getSelectionModel().getSelectedItem();
            clockingManager.deleteClocking(selection);
        });

        // Title of the check page
        Label checkTitle = new Label("Check");
        checkTitle.getStyleClass().add("page-title");

        VBox pagePointage = new VBox(
        15,
        checkTitle,
        filterBar,
        checkActions,
        tablePointage
        );

        pagePointage.setPadding(new Insets(15));

        /* ==================== TABLE DEPARTMENT ==================== */

        // Table displaying all departments
        TableView<Department> tableDepartment = new TableView<>();

        // Column containing the department name
        TableColumn<Department, String> colDepName = new TableColumn<>("Department");

        // Linking the column with the department attribute
        colDepName.setCellValueFactory(new PropertyValueFactory<>("department"));

        tableDepartment.getColumns().add(colDepName);

        // Linking the table with the observableList of departments
        tableDepartment.setItems(departments);

        // The table takes all the free horizontal spaces
        tableDepartment.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Buttons allowing management of departments
        Button btnAddDepartment = new Button("Add Department");
        Button btnDeleteDepartment = new Button("Delete Department");
        Button btnModifyDepartment = new Button("Modify Department");

        // Creation of a new department
        btnAddDepartment.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New Department");
            dialog.setContentText("Department name:");

            dialog.showAndWait().ifPresent(name -> {

                // Preventing empty department names
                if (!name.trim().isEmpty()) {
                    Department d = new Department(name);
                    departments.add(d);

                    // Saving departments after modification
                    Serialization.saveObject(new ArrayList<>(departments), DEPARTMENT_FILE);
                }
            });
        });

        // Deletion of the selected department
        btnDeleteDepartment.setOnAction(e -> {
            Department dep = tableDepartment.getSelectionModel().getSelectedItem();

            if (dep == null) return;

            // Removing the department from employees using it
            for (Employee emp : employeeManager.getEmployeeList()) {
                if (dep.equals(emp.getDepartment())) {
                    emp.setDepartment(null);
                }
            }

            // Removing the department from the list
            departments.remove(dep);

            // Refreshing tables impacted by the modification
            tableEmployee.refresh();
            tablePointage.refresh();

            // Saving departments after modification
            Serialization.saveObject(new ArrayList<>(departments), DEPARTMENT_FILE);
        });

        // Modification of the selected department
        btnModifyDepartment.setOnAction(e -> {

            Department dep =
                    tableDepartment.getSelectionModel().getSelectedItem();

            if (dep == null) return;

            // Dialog pre-filled with the current department name
            TextInputDialog dialog =
                    new TextInputDialog(dep.getDepartment());

            dialog.setTitle("Modify Department");
            dialog.setHeaderText(null);
            dialog.setContentText("New department name:");

            dialog.showAndWait().ifPresent(newName -> {

                // Preventing empty department names
                if (!newName.trim().isEmpty()) {

                    dep.setDepartment(newName);

                    // Refreshing tables impacted by the modification
                    tableDepartment.refresh();
                    tableEmployee.refresh();
                    tablePointage.refresh();

                    // Saving departments after modification
                    Serialization.saveObject(
                            new ArrayList<>(departments),
                            DEPARTMENT_FILE
                    );
                }
            });
        });

        HBox departmentActions = new HBox(10, btnAddDepartment, btnModifyDepartment, btnDeleteDepartment);

        // Title of the department page
        Label departmentTitle = new Label("Departments");
        departmentTitle.getStyleClass().add("page-title");

        VBox pageDepartment = new VBox(
                15,
                departmentTitle,
                departmentActions,
                tableDepartment
        );

        pageDepartment.setPadding(new Insets(15));

        /* ==================== NAVIGATION ==================== */

        // Changing the displayed page when a navigation button is clicked
        btnEmployee.setOnAction(e -> root.setCenter(pageEmployee));
        btnCheck.setOnAction(e -> root.setCenter(pagePointage));
        btnDepartment.setOnAction(e -> root.setCenter(pageDepartment));

        // Navbar always stays at the top of the application
        root.setTop(navbar);

        // Employee page displayed by default
        root.setCenter(pageEmployee);

        // Retrieving the available screen size
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        // Applying CSS style to employee action buttons
        btnAddEmployee.getStyleClass().add("action-button");
        btnEditEmployee.getStyleClass().add("action-button");
        btnDeleteEmployee.getStyleClass().add("action-button");

        // Applying CSS style to check action buttons
        btnEditCheck.getStyleClass().add("action-button");
        btnDeleteCheck.getStyleClass().add("action-button");

        // Applying CSS style to department action buttons
        btnAddDepartment.getStyleClass().add("action-button");
        btnDeleteDepartment.getStyleClass().add("action-button");
        btnModifyDepartment.getStyleClass().add("action-button");

        // Applying CSS style to pages
        pageEmployee.getStyleClass().add("page");
        pagePointage.getStyleClass().add("page");
        pageDepartment.getStyleClass().add("page");

        // Creation of the scene using the screen size
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

        // Loading the CSS stylesheet
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Final configuration of the stage
        stage.setScene(scene);
        stage.setTitle("Application");

        // Displaying the application window
        stage.show();
    }

    /**
     * It allows us to find an employee by a given id.
     * @param list : ObservableList<Employee>
     * @param id : UUID
     * @return Employee : may be the employee we're looking for or null.
     */
    private Employee findEmployeeById(ObservableList<Employee> list, UUID id) {
        if (id == null) return null;
        // Scrolling of the entire employee list
        for (Employee e : list) {
            // comparison to find the employee we're looking for
            if (id.equals(e.getEmployeeId())) return e;
        }
        return null;
    }

    /**
     * It updates the schedule in real time for a given employee.
     * @param table : TableView<Employee>
     * @param group : ToggleGroup
     * @param service : HBox
     * @param bar : UUID
     * @param label : Label
     */
    private void updateVisualSchedule(TableView<Employee> table, ToggleGroup group, PlanningService service, HBox bar, Label label) {
        // retrieve the selected employee in the table
        Employee selectedEmp = table.getSelectionModel().getSelectedItem();
        // retrieve the selected day selected
        ToggleButton selectedDayBtn = (ToggleButton) group.getSelectedToggle();

        // verify that the employee and a day is really selected
        if (selectedEmp != null && selectedDayBtn != null) {
            //retrieve the day associated with the button
            java.time.DayOfWeek day = (java.time.DayOfWeek) selectedDayBtn.getUserData();
            // load and display the scheduling
            service.loadSchedule(bar, label, selectedEmp, day);
        }
    }

    public static void main(String[] args) {
        launch();
    }

}