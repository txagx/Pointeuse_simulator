package Planning;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

/**
 * Class representing an employee's weekly schedule.
 * It uses a HashMap to link a specific day of the week to its working hours.
 */
public class Planning implements Serializable {

	//- - - ATTRIBUTE - - -
	private Map<DayOfWeek, WorkDay> scheduleMap; // HashMap to store the working days

	// - - - CONSTRUCTOR - - -
	public Planning() {
		this.scheduleMap = new HashMap<>();
	}

	// - - - SETTER - - -
	public void setWorkDay(DayOfWeek day, WorkDay workDay) {
		scheduleMap.put(day, workDay);
	}

	// - - - GETTER - - -
	public WorkDay getWorkDay(DayOfWeek day) { return scheduleMap.get(day); } //(returns null if the employee is not working)
}