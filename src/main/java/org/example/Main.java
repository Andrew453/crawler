package org.example;

import com.rabbitmq.client.DeliverCallback;
import org.json.JSONArray;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Crawler crawler = new Crawler(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        crawler.Listen();
    }
}