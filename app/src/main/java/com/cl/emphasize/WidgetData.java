package com.cl.emphasize;

import java.io.Serializable;

public class WidgetData implements Serializable{
    int widgetId, blinkDelay;
    String fileName, fileContents, backgroundColor;

    public WidgetData(int widgetId, String fileName,
                           String fileContents, int blinkDelay, String backgroundColor) {
        this.widgetId = widgetId;
        this.fileName = fileName;
        this.fileContents = fileContents;
        this.blinkDelay = blinkDelay;
        this.backgroundColor = backgroundColor;
    }

    public int getWidgetId() {
        return widgetId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileContents() {
        return fileContents;
    }

    public int getBlinkDelay() {
        return blinkDelay;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setWidgetId(int newId) {
        this.widgetId = newId;
    }

    public void setFileName(String newFileName) {
        this.fileName = newFileName;
    }

    public void setFileContents(String newFileContents) {
        this.fileContents = newFileContents;
    }

    public void setBlinkDelay(int newBlinkDelay) {
        this.blinkDelay = newBlinkDelay;
    }

    public void setBackgroundColor(String newBackgroundColor) {
        this.backgroundColor = newBackgroundColor;
    }
}
