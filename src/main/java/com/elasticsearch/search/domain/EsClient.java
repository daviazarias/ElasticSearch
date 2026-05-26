package com.elasticsearch.search.domain;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.altindag.ssl.SSLFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class EsClient {
    private ElasticsearchClient elasticsearchClient;

    public EsClient() {
        createConnection();
    }

    private void createConnection() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        String USER = "elastic";
        String PWD = "user123";
        credentialsProvider.setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials(USER, PWD));

        SSLFactory sslFactory = SSLFactory.builder()
            .withUnsafeTrustMaterial()
            .withUnsafeHostnameVerifier()
            .build();

        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "https"))
            .setHttpClientConfigCallback((HttpAsyncClientBuilder httpClientBuilder) -> httpClientBuilder
                .setDefaultCredentialsProvider(credentialsProvider)
                .setSSLContext(sslFactory.getSslContext())
                .setSSLHostnameVerifier(sslFactory.getHostnameVerifier())
            ).build();

        ElasticsearchTransport transport = new RestClientTransport(
            restClient,
            new JacksonJsonpMapper()
        );

        elasticsearchClient = new co.elastic.clients.elasticsearch.ElasticsearchClient(transport);
    }

    private List<String> extractPatterns(String input, String regex) {
        return Pattern.compile(regex)
                .matcher(input)
                .results()
                .map(mr -> mr.group(1))
                .collect(Collectors.toList());
    }

    public Query createQueryWithMandatoryPhrases(List<String> mandatoryPhrases, String originalQuery){
        List<Query> mustQueriesList = mandatoryPhrases
                .stream()
                .map(phrase ->
                        Query.of(sq ->
                                sq.matchPhrase(mp ->
                                        mp.query(phrase).field("content"))))
                .toList();

        String filteredQuery = originalQuery.replace("\"", "");

        return BoolQuery.of(b -> b
                .must(mustQueriesList)
                .should(sq ->
                        sq.term(t -> t.field("content").value(filteredQuery)))
        )._toQuery();
    }

    public SearchResponse search(String query, Integer page, Integer pageSize) {
        int from = ((page != null ? page : 1) - 1) * pageSize;

        Query matchQuery;

        List<String> mustEntries = extractPatterns(query, "\"(.+?)\"");

        matchQuery = (mustEntries.isEmpty())
                ? MatchQuery.of(q-> q.field("content").query(query))._toQuery()
                : createQueryWithMandatoryPhrases(mustEntries,query);

        SearchResponse<ObjectNode> response;
        try {
            response = elasticsearchClient.search(s -> s
                .index("wikipedia").from(from).size(pageSize)
                .query(matchQuery), ObjectNode.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response;
    }
}
