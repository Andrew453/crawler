package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Robot {
    private static final String USER = "guest";
    private static final String HOST = "127.0.0.1";
    private static final String PASS = "guest";
    private String datePattern = "\\d{4}-\\d{2}-\\d{2}";

    private Pattern regular = Pattern.compile(datePattern);
    private String RECIEVE_QUEUE = "planner_queue";
    private String SEND_QUEUE = "crawler_queue";
    private String SEND_KEY = "cr_to_pl";
    private String RECIEVE_KEY = "pl_to_cr";
    private String EXCHANGE = "parser";
    private int threadsCount;
    Listener listener;

    Channel channel;

    Robot(int threadCounbt) throws IOException {
        this.threadsCount = threadCounbt;
        this.listener = new Listener(RECIEVE_KEY, EXCHANGE, RECIEVE_QUEUE);
        // создание фабрики соединений
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setUsername(USER);
        factory.setPassword(PASS);

        factory.useNio();
        factory.setConnectionTimeout(50000);
        factory.setRequestedHeartbeat(100);

        // создание соединения
        Connection connection;
        try {
            connection = factory.newConnection();
        } catch (Exception e) {
            System.out.println("Queue()");
            System.out.println(e);
            return;
        }
        Channel channel;
        try {
            channel = connection.createChannel();
        } catch (Exception e) {
            System.out.println("connection.createChannel");
            System.out.println(e);
            return;
        }
        try {
            channel.exchangeDeclare(EXCHANGE, "direct");
        } catch (Exception e) {
            System.out.println("channel.exchangeDeclare");
            System.out.println(e);
            return;
        }

        try {
            channel.queueDeclare(SEND_QUEUE, false, false, false, null);
        } catch (Exception e) {
            System.out.println("channel.queueDeclare");
            System.out.println(e);
            return;
        }
//        Thread.sleep(5000);

        this.channel = channel;
    }

    public void setThreadsCount(int threadsCount) {
        this.threadsCount = threadsCount;
    }

    public void Listen() throws IOException {
        listener.Listen(start);
    }

    public void Send(String msg) {
        try {
            channel.basicPublish(EXCHANGE, SEND_KEY, null, msg.getBytes());
        } catch (Exception e) {
            System.out.println("channel.basicPublish");
            System.out.println(e);
        }
    }

    DeliverCallback start = (consumerTag, delivery) -> {

        String jsonBody = new String(delivery.getBody(), StandardCharsets.UTF_8);
        JSONArray jarr = null;
        try {
            jarr = new JSONArray(jsonBody);
        } catch (Exception e) {
            System.out.println("Incorrect input string");
            System.out.println(jsonBody);
            return;
        }
        listener.getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        System.out.println("start");

        ArrayList<String> urls = new ArrayList<>();
        for (int i = 0; i < jarr.length(); i++) {
            urls.add(jarr.getString(i));
        }
        System.out.println(urls);
        try {
            Start(urls);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Finish");
    };

    public void Start(ArrayList<String> urls) throws InterruptedException {
        ExecutorService executor = null;
        try {
            executor = Executors.newFixedThreadPool(this.threadsCount);
        } catch (Exception e) {
            System.out.println("Error: Executors.newFixedThreadPool()");
            System.out.println(e.getMessage());
            ;
            return;
        }

        for (String url : urls) {
            AtomicReference<News> n = new AtomicReference<>(new News());
            executor.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                News news =  ParseNews(url);
//                if (news.Valid()) {
                    n.set(news);
//                }
                synchronized (Objects.requireNonNull(n)) {
                    this.Response(n.get());
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    public News ParseNews(String url) {
        News n = new News();
        try {
            // Получаем HTML-страницу с сайта
            Document document = Jsoup.connect(url).get();
            int statusCode = document.connection().response().statusCode();
            switch (statusCode) {
                case 200:
                    break;
                case 404:
                    System.out.println("Error: page not found");
                    return null;
                case 500:
                    System.out.println("Internal server error");
                    return null;
                default:
                    System.out.printf("Incorrect status code: %d\n", statusCode);
                    return null;
            }
            Elements arcls = document.select("article");
            Elements aside = document.select("aside.textML");
            Elements time = aside.select("time");
            Elements title = arcls.select("h1");
            Elements texts = document.select("p");
            n.link = url;
            StringBuilder sb = new StringBuilder();
            for (Element text : texts) {
                sb.append(text.text()).append(" ");
            }
            n.text = sb.toString();
            n.header = title.text();
            String date = time.attr("datetime");
            Matcher matcher = regular.matcher(date);

            if (matcher.find()) {
                n.date = matcher.group();
            }

            n.print();
            n.printText();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return n;
    }

    private void Response(News ni) {
        ObjectMapper objectMapper = new ObjectMapper();
        String json;
        try {
            json = objectMapper.writeValueAsString(ni);
        } catch (JsonProcessingException e) {
            System.out.println("objectMapper.writeValueAsString(ni)");
            System.out.println(e);
            return;
        }
        Send(json);
    }


}
