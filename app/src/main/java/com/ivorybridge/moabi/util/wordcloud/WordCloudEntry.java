package com.ivorybridge.moabi.util.wordcloud;

public class WordCloudEntry {

    private String entryName;
    private long numberOfEntry;
    private long entryType;

    public WordCloudEntry(String entryName, long numberOfEntry, long entryType) {
        this.entryName = entryName;
        this.numberOfEntry = numberOfEntry;
        this.entryType = entryType;
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public long getNumberOfEntry() {
        return numberOfEntry;
    }

    public void setNumberOfEntry(long numberOfEntry) {
        this.numberOfEntry = numberOfEntry;
    }

    public long getEntryType() {
        return entryType;
    }

    public void setEntryType(long entryType) {
        this.entryType = entryType;
    }
}
