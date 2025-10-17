package in.sfit.miniproject;

import java.io.Serializable;

class PriorityItem implements Serializable {
    private String text;

    public PriorityItem(String text) {
        this.text = text;
    }

    public String getText() { return text; }
}