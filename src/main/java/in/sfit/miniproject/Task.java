package in.sfit.miniproject;

import java.io.Serializable;
import java.time.LocalDate;

class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDate date;
    private String timeSlot;
    private String taskName;
    private String description;
    private boolean completed;

    public Task(LocalDate date, String timeSlot, String taskName, String description) {
        this.date = date;
        this.timeSlot = timeSlot;
        this.taskName = taskName;
        this.description = description;
        this.completed = false;
    }

    public LocalDate getDate() { return date; }
    public String getTimeSlot() { return timeSlot; }
    public String getTaskName() { return taskName; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}