package org.example;

import com.rabbitmq.client.DeliverCallback;
import org.json.JSONArray;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
          Crawler crawler = new Crawler(1);
          crawler.Listen();
    }
}