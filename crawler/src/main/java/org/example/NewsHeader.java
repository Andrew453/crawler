package org.example;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class NewsHeader {
    String title;
    String date;
    String link;
    Checksum crc;
    int ID;

    NewsHeader(String title, String date, String link) {
        this.title = title;
        this.date = date;
        this.link = link;
        Checksum crc32 = new CRC32();
        crc32.update((this.title + this.date + this.link).getBytes());
        this.crc = crc32;
    }

    public void Store() {
        System.out.println("Новость");
        System.out.println(this.date);
        System.out.println(this.title);
        System.out.println(this.link);
        System.out.println(this.crc.getValue());
    }
}
