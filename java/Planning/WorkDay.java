package Planning;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * Class representing a single working day for an employee.
 * It stores the start time and the end time of their shift.
 */
public class WorkDay implements Serializable {

    // The time the employee starts working
    private LocalTime startTime;

    // The time the employee finishes working
    private LocalTime endTime;

    // Constructor: initializes the shift with a specific start and end time
    public WorkDay(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Returns the start time of the shift
    public LocalTime getStartTime() {
        return startTime;
    }

    // Returns the end time of the shift
    public LocalTime getEndTime() {
        return endTime;
    }

    // Formats the working hours as a string (for example "08:00 -> 17:00")
    @Override
    public String toString() {
        return startTime + " -> " + endTime;
    }
}