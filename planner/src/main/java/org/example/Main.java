package org.example;



import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        String url = "https://ria.ru/";

        Planner planner = new Planner();
        planner.getNewsLinks(url);
// Create the low-level client

//        NewsInfo ni = new NewsInfo();
//        ni.date = "test";
//        ni.link = "test";
//        ni.header = "test";
//        ni.text = "test";
//        ni.hashMD5 = "hash_test";
//
//        IndexResponse response = client.index(i -> i
//                .index("news")
//                .id(ni.getHashMD5())
//                .document(ni)
//        );
//
//        System.out.println("Indexed with version " + response.version());
//        client.index()
//        SearchResponse<NewsInfo> search = client.search(s -> s
//                        .index("news"),
////                        .query(q -> q
////                                .term(t -> t
////                                        .field("hash")
////                                        .value(v -> v.stringValue("news"))
////                                )),
//                NewsInfo.class);

//        for (Hit<NewsInfo> hit: search.hits().hits()) {
//            processNewsInfo(hit.source());
//        }

    }
    private static void processNewsInfo(NewsInfo p) {
        p.print();
    }
}