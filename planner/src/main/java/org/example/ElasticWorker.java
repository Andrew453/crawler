package org.example;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;

public class ElasticWorker {
    private static ElasticsearchClient elasticClient;

    ElasticWorker() {
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200)).build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        elasticClient = new ElasticsearchClient(transport);
    }

    public static Boolean storeNewsInfo(NewsInfo newsInfo) throws IOException {
        try {
            IndexResponse response = elasticClient.index(i -> i
                    .index("news")
                    .id(newsInfo.getHashMD5())
                    .document(newsInfo)
            );

            System.out.println("Indexed with id " + response.id());
        } catch (IOException e) {
            System.out.println("ERROR storing news info: " + e.getMessage());
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    public static NewsInfo searchNewsInfo() throws IOException {
        try {
            SearchResponse<NewsInfo> search = elasticClient.search(s -> s
                            .index("news"),
                    NewsInfo.class);
        } catch (IOException e) {
            System.out.println("ERROR searching news by id: " + e.getMessage());
            return null;
        }
        return null;
    }
}
