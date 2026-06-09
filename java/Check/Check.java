/**
 * The 'Check' class represents a single check in the system.
 * It stocks information related to this check.
 */

package Check;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import java.util.Objects;

public class Check implements Serializable
{
	@Serial
    private static final long serialVersionUID = 1L;

	//- - - ATTRIBUTES - - -
	private LocalDate date; /** date of clocking */
	private LocalTime time; /** hour of clocking */
	private CheckType type; /** type of clocking (IN or OUT) */
	private UUID employeeUUID; /** id of employee who checked */

	// - - - CONSTRUCTOR
	/**
	 * builds a Check object
	 * @param nDate : LocalDate
	 * @param nTime : LocalTime
	 * @param nType : CheckType
	 * @param nEmployeeUUID : UUID
	 */
	public Check(LocalDate nDate, LocalTime nTime, CheckType nType, UUID nEmployeeUUID)
	{
		date = nDate;
		time = nTime;
		type = nType;
		employeeUUID = nEmployeeUUID;
	}

	// - - - GETTERS - - -
	/**
	 * returns the date of Check object
	 * @return date : LocalDate
	 */
	public LocalDate getDate() {return date;}
	/**
	 * returns the hours of Check object
	 * @return time : LocalTime
	 */
	public LocalTime getTime() {return time;}
	/**
	 * returns the type of Check object (IN or OUT)
	 * @return time : CheckType
	 */
	public CheckType getCheckType() {return type;}
	/**
	 * returns the id employee who made the Check
	 * @return employeeUUID : UUID
	 */
	public UUID getEmployeeUUID() {return employeeUUID;}

	// - - - SETTERS - - -

	/**
	 * edit the date of the Check object
	 * @param newDate : LocalDate
	 */
	public void setDate(LocalDate newDate) {date = newDate;}

	/**
	 * edit the hours of the Check object
	 * @param newTime : LocalTime
	 */
	public void setTime(LocalTime newTime) {time = newTime;}

	/**
	 * edit the check type of the Check object (IN or OUT)
	 * @param newCheckType : CheckType
	 */
	public void setCheckType(CheckType newCheckType) {type = newCheckType;}

	/**
	 * edit the id employee of the Check object
	 * @param newUUID : UUID
	 */
	public void setEmployeeUUID(UUID newUUID) {employeeUUID = newUUID;}

	//- - - METHODS - - -

	/**
	 * return a unique int for the object
	 * @return number of a hash Check object
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(date, time, type, employeeUUID);
	}

	/**
	 * Redefine the method equals.
	 * @param object : the reference object with which to compare.
	 * @return booleen : true or false
	 */
	@Override
	public boolean equals(Object object)
	{
		//If it’s exactly the same instance => they are equal
		if (this == object)
			return true;
		//If the other object is null or of another class => not equal
		if (object == null || getClass() != object.getClass())
			return false;

		//we convert (cast) the object in parameter in Check
		Check check = (Check) object;
		//We check that the all attributes are the same
		return Objects.equals(date, check.date) && Objects.equals(time, check.time) && type == check.type && Objects.equals(employeeUUID, check.employeeUUID);
	}

	/**
	 * main of this class
	 */
	public static void main(String[] args) {};
}
