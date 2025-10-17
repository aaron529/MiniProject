package in.sfit.miniproject;

import java.io.Serializable;

class TodoItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String text;
    private boolean completed;

    public TodoItem(String text) {
        this.text = text;
        this.completed = false;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}