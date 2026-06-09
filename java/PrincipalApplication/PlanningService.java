package PrincipalApplication;

import Employee.Employee;
import Planning.WorkDay;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;

import java.time.DayOfWeek;

/**
 * Service used to visually display an employee's schedule.
 */
public class PlanningService {

    /**
     * Loads and displays the visual schedule of an employee for a specific day.
     */
    public void loadSchedule(HBox bar, Label labelInfo, Employee emp, DayOfWeek day) {
        if (emp != null && emp.getPlanning() != null) {

            // Clear the previous visual bar
            bar.getChildren().clear();
            bar.setSpacing(2);

            WorkDay wd = emp.getPlanning().getWorkDay(day);

            if (wd != null && wd.getStartTime() != null && wd.getEndTime() != null) {

                // Calculate grid indexes (1 hour = 4 blocks of 15 mins, so 24h = 96 rectangles)
                int start = wd.getStartTime().getHour() * 4 + (wd.getStartTime().getMinute() / 15);
                int end = wd.getEndTime().getHour() * 4 + (wd.getEndTime().getMinute() / 15);

                // Create the 96 rectangles to represent the whole day
                for (int i = 0; i < 96; i++) {
                    Rectangle r = new Rectangle(12, 25);

                    // Color the rectangle blue if it's working time, otherwise gray
                    r.setFill((i >= start && i < end) ? Color.CORNFLOWERBLUE : Color.LIGHTGRAY);
                    bar.getChildren().add(r);
                }

                // Update the information text
                labelInfo.setText("Schedule for " + day + " for " + emp.getFirstName() + ": " + wd.getStartTime() + " - " + wd.getEndTime());
            } else {
                bar.getChildren().clear();
                labelInfo.setText("No working hours defined for this day for " + emp.getFirstName());
            }
        } else {
            bar.getChildren().clear();
            labelInfo.setText("No schedule available for this employee.");
        }
    }
}