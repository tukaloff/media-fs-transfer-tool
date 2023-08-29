package com.tukaloff.mediafstransfertool;

public class Progress {

    private double max;
    private int value = 0;

    public void setFilesCount(long filesCountInSource) {
        this.max = filesCountInSource;
    }

    public void increment() {
        this.value++;
    }

    public double progress() {
        return value/max;
    }

    public String getString() {
        double progress = progress();
        int progInt = (int)(progress * 100);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<100; i++) {
            if (i < progInt) {
                sb.append("=");
            } else if (i == progInt) {
                sb.append(">");
            } else {
                sb.append(" ");
            }
        }
        sb.append("|");
        sb.append(String.format("%3.2f", progress * 100.0));
        sb.append("%\r");
        return sb.toString();
    }

}

