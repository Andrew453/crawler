package org.example;

import java.util.Objects;

public class News {
    String text;
    String header;
    String date;
    String link;

    public void print() {
        System.out.println();
        System.out.println("Header: " + header);
        System.out.println("Date: " + date);
        System.out.println("Link: " + link);
        System.out.println();
    }

    public boolean Valid() {
        if (Objects.equals(header, "")) {
            return false;
        }
        if (Objects.equals(text, "")) {
            return false;
        }
        if (Objects.equals(date, "")) {
            return false;
        }
        if (Objects.equals(link, "")) {
            return false;
        }
        return true;
    }

    public void printText() {
        System.out.println();
        System.out.println(this.text);
        System.out.println();
    }

    public String getText() {
        return text;
    }

    public String getHeader() {
        return header;
    }

    public String getDate() {
        return date;
    }

    public String getLink() {
        return link;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
