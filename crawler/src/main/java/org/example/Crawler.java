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

public class Crawler {
    private static final String USER = "guest";
    private static final String HOST = "127.0.0.1";
    private static final String PASS = "guest";
    private String datePattern = "\\d{2}\\.\\d{2}\\.\\d{4}";

    private Pattern regex = Pattern.compile(datePattern);
    private String RECIEVE_QUEUE_NAME ="planner_queue";
    private String SEND_QUEUE_NAME ="crawler_queue";
    private String SEND_ROUTING_KEY = "cr_to_pl";
    private String RECIEVE_ROUTING_KEY = "pl_to_cr";
    private String EXCHANGE_NAME = "parser";
    private int numThreads;
    Queue queue;

    Channel channel;

    Crawler(int numThreads) throws IOException {
        this.numThreads = numThreads;
        this.queue = new Queue(RECIEVE_ROUTING_KEY,EXCHANGE_NAME, RECIEVE_QUEUE_NAME);
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
            channel.exchangeDeclare(EXCHANGE_NAME,"direct");
        } catch (Exception e) {
            System.out.println("channel.exchangeDeclare");
            System.out.println(e);
            return;
        }

        try {
            channel.queueDeclare(SEND_QUEUE_NAME,false,false,false,null);
        } catch (Exception e) {
            System.out.println("channel.queueDeclare");
            System.out.println(e);
            return;
        }
//        Thread.sleep(5000);

        this.channel = channel;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public void Listen() throws IOException {
        queue.Listen(start);
    }

    public void SendMessage(String msg) {
        try {
            channel.basicPublish(EXCHANGE_NAME, SEND_ROUTING_KEY, null,msg.getBytes());
        } catch (Exception e) {
            System.out.println("channel.basicPublish");
            System.out.println(e);
        }
    }
    DeliverCallback start = (consumerTag, delivery) -> {

        String jsonString = new String(delivery.getBody(), StandardCharsets.UTF_8);
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (Exception e) {
            queue.GetChannel().basicAck(delivery.getEnvelope().getDeliveryTag(),false);
            System.out.println("Incorrect input string");
            System.out.println(jsonString);
            return;
        }
        queue.GetChannel().basicAck(delivery.getEnvelope().getDeliveryTag(),true);
        System.out.println("start");

        // Преобразуем JSONArray в ArrayList<String>
        ArrayList<String> urls = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            urls.add(jsonArray.getString(i));
        }
        System.out.println(urls);
        try {
            StartParse(urls);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("DONE");
    };

    public void StartParse(ArrayList<String> urls) throws InterruptedException {
        ExecutorService executor = null;
        try {
             executor = Executors.newFixedThreadPool(this.numThreads);
        } catch (Exception e) {
            System.out.println("Error: Executors.newFixedThreadPool()");
            System.out.println(e.getMessage());;
            return;
        }

        for (String url :urls) {
            AtomicReference<NewsInfo> ni = new AtomicReference<>(new NewsInfo());
            executor.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                ni.set(Parse(url));
                synchronized (Objects.requireNonNull(ni)) {
                    this.Response(ni.get());
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    private NewsInfo Parse(String url) {
        NewsInfo ni = new NewsInfo();
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

            Elements texts = document.select("div.article__text");
            ni.link = url;
            StringBuilder sb = new StringBuilder();
            for (Element text : texts) {
                sb.append(text.text()).append(" ");
            }
            ni.text = sb.toString();
            Elements date = document.select("div.article__info-date");

            Matcher matcher = regex.matcher(date.text());

            if (matcher.find()) {
                ni.date = matcher.group();
            }
            Elements title = document.select("div.article__title");
            ni.header = title.text();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return ni;
    }

    private void Response(NewsInfo ni) {
        ObjectMapper objectMapper = new ObjectMapper();
        String json;
        try {
            json = objectMapper.writeValueAsString(ni);
        } catch (JsonProcessingException e) {
            System.out.println("objectMapper.writeValueAsString(ni)");
            System.out.println(e);
            return;
        }
        SendMessage(json);
//        ni.print();
//        ni.printText();
    }


}
