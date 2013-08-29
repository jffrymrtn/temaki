package com.jmartin.temaki.model;

/**
 * Created by jeff on 2013-08-29.
 */
public class TemakiItem {
    private String text;
    private boolean isHighlighted = false;
    private boolean isFinished = false;

    public TemakiItem(String text) {
        this.text = text;
    }

    public boolean isFinished() {
        return this.isFinished;
    }

    public boolean isHighlighted() {
        return this.isHighlighted;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String newText) {
        this.text = newText;
    }

    public void toggleFinished() {
        this.isFinished = !this.isFinished;
    }

    public void toggleHighlighted() {
        this.isHighlighted = !this.isHighlighted;
    }
}
