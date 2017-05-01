package com.lowlightstudios.watlo.models;

/**
 * Created by damato on 4/30/17.
 */

public class InfoCard {
    protected String title;

    public InfoCard(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
