/**
 * The class 'Department' allows to manage a department with his name et his list employees. We can add or delete employee in this list.
 */

package Entreprise;

import Employee.Employee;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Department implements Serializable{

	//- - - ATTRIBUTES - - -
	private static final long serialVersionUID = 1L; /** for serialization compatibility. */

	private String depName; /** name of departement */

	private List <Employee> employeeList; /** list of employees who are in this departement */

	//- - - CONSTRUCTOR - - -
	/**
	 * Constructs a new Department with the name who is in the parameter and an empty employee list.
	 * @param sDepName : The name of the department.
	 */
	public Department(String sDepName)
	{
		this.depName = sDepName;

		this.employeeList = new ArrayList<>();
	}

	//- - - GETTER - - -
	/**
	 * return the name of department
	 * @return sDepName : The name of the department.
	 */
	public String getDepartment(){ return this.depName;} //allows access to the depName value

	/**
	 * return the employees list of department
	 * @return employeeList : The employee list.
	 */
	public List <Employee> getEmployeeList(){ return new ArrayList<>(employeeList);} //allows access to the employeeList list

	//- - - SETTER - - -
	/**
	 * edit the name of the department
	 * @param sDepName : string
	 */
	public void setDepartment(String sDepName){ this.depName = sDepName;} //allows edit to the depName value

	//- - - METHODS - - -
	/**
	 * allows to add a new employee in the employeeList list
	 * @param employee : Employee (object of this class)
	 */
	public void addEmployee(Employee employee)
	{
		if (employee == null)
			return;
		for(int loop = 0; loop < employeeList.size(); loop++)
		{
			if(employee == employeeList.get(loop))
				return;
		}
		this.employeeList.add(employee);
	}

	/**
	 * allows to remove employee in the employeeList list
	 * @param employee : Employee (object of this class)
	 */
	public void removeEmployee(Employee employee)
	{
		this.employeeList.remove(employee);
	}

	/**
	 * it's essential for allow to JavaFX to display the name of the department
	 * @return string : the name of the department
	 */
	@Override
	public String toString() { return this.depName; }
}