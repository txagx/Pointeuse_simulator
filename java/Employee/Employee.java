/**
 * The 'Employee' class represents a single employee in the system.
 * It stocks information related to this employee.
 */
package Employee;

import Entreprise.Department;
import Planning.Planning;

import java.util.UUID;
import java.io.Serializable;

public class Employee implements Serializable{

	//- - - ATTRIBUTES - - -
	private final UUID employeeId; /** UUID of the employee */

	private String firstName; /** Employee's firstname */
	private String lastName; /** Employee's lastname */

	private Department department; /** The department where our employee works */

	private Planning planning; /** The planning associated with our employee*/

	// - - - CONSTRUCTOR
	/**
	 * builds an employee object
	 * @param newFirstName : String
	 * @param newLastName : String
	 * @param newDepartment : Department
	 * @param newPlanning : Planning
	 */
	public Employee(String newFirstName, String newLastName, Department newDepartment, Planning newPlanning) {

		employeeId = UUID.randomUUID(); /** The UUID is randomized */

		firstName = newFirstName;
		lastName = newLastName;

		department = newDepartment;
		planning = newPlanning;
	}

	// - - - GETTERS - - -
	/**
	 * returns the ID of an employee
	 * @return employeeId : UUID
	 */
	public UUID getEmployeeId() {
		return employeeId;
	}

	/**
	 * returns the firstname of an employee
	 * @return firstName : String
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * returns the lastname of an employee
	 * @return lastName : String
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * returns the department of an employee
	 * @return department : Department
	 */
	public Department getDepartment() {
		return department;
	}

	/**
	 * returns the planning of an employee
	 * @return planning : Planning
	 */
	public Planning getPlanning() {
		return planning;
	}

	// - - - SETTERS - - -

	/**
	 * edit the firstname of an employee
	 * @param firstName : String
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * edit the lastName of an employee
	 * @param lastName : String
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * edit the department of an employee
	 * @param department : Department
	 */
	public void setDepartment(Department department) {
		this.department = department;
	}

	/**
	 * edit the planning of an employee
	 * @param planning : Planning
	 */
	public void setPlanning(Planning planning) {
		this.planning = planning;
	}

	//- - - METHODS - - -

	/**
	 * return a String. Used for a correct display of an employee.
	 * @return A combination of firstname and lastname : String
	 */
	@Override
	public String toString() {
		return firstName + " " + lastName;
	}
}