package com.elasticsearch.search.domain;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.BoundaryScanner;
import co.elastic.clients.elasticsearch.core.search.Highlight;
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
import java.util.Arrays;
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

    private Query createSimpleQuery(String query){

        List<String> searchedWords = Arrays.stream(query.split("\\s+")).toList();

        List<Query> queriesList = searchedWords
                .stream().map(word ->
                        Query.of(q -> q
                                .multiMatch(mm -> mm
                                    .fields("content^1.0", "title^3.0")
                                    .query(word).queryName(word)
                                )
                        )
                ).toList();

        return Query.of(q -> q.bool(b -> b.should(queriesList)));
    }

    private Query createQueryWithMandatoryPhrases(List<String> mandatoryPhrases, String originalQuery){
        List<Query> mustQueriesList = mandatoryPhrases
                .stream()
                .map(phrase ->
                        Query.of(sq ->
                                sq.matchPhrase(mp ->
                                        mp.query(phrase).field("content")
                                )
                        )
                ).toList();

        String filteredQuery = originalQuery.replace("\"", "");

        Query shouldQuery = Query.of(q -> q
                .multiMatch(mmq -> mmq
                        .query(filteredQuery)
                        .fields("content^1.0", "title^3.0")
                        .queryName(filteredQuery)
                )
        );

        return BoolQuery.of(b -> b
                .must(mustQueriesList)
                .should(shouldQuery)
        )._toQuery();
    }

    public SearchResponse search(String query, Integer page, Integer pageSize) {
        int from = ((page != null ? page : 1) - 1) * pageSize;

        Query matchQuery;

        List<String> mustEntries = extractPatterns(query, "\"(.+?)\"");

        matchQuery = (mustEntries.isEmpty())
                ? createSimpleQuery(query)
                : createQueryWithMandatoryPhrases(mustEntries,query);

        Highlight highlight = Highlight.of(h -> h
                .fields("content", hf -> hf
                        .numberOfFragments(0)
                )
        );

        SearchResponse<ObjectNode> response;
        try {
            response = elasticsearchClient.search(s -> s
                    .index("wikipedia")
                    .from(from)
                    .size(pageSize)
                    .query(matchQuery)
                    .highlight(highlight), ObjectNode.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response;
    }
}
