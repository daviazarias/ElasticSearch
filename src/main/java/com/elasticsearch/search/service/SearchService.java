package com.elasticsearch.search.service;

import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.elasticsearch.search.api.model.Result;
import com.elasticsearch.search.api.model.SearchResults;
import com.elasticsearch.search.domain.EsClient;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final EsClient esClient;
    private int pageSize = 10;

    public SearchService(EsClient esClient) {
        this.esClient = esClient;
    }

    public SearchResults submitQuery(String query, Integer page) {
        var resultsResponse = esClient.search(query, page, pageSize);
        HitsMetadata<ObjectNode> hits = resultsResponse.hits();
        List<Hit<ObjectNode>> hits_hits = hits.hits();

        int total_hits = (int) hits.total().value();

        var resultsList = hits_hits.stream().map(h ->
                new Result()
                        .abs(treatContent(h.source().get("content").asText()))
                        .title(h.source().get("title").asText())
                        .url(h.source().get("url").asText())
        ).collect(Collectors.toList());

        return new SearchResults()
                .totalHits(total_hits)
                .numeroPaginas((int) Math.ceil((double) total_hits / pageSize))
                .results(resultsList);
    }

    private String treatContent(String content) {
        content = content.replaceAll("</?(som|math)\\d*>", "");
        content = content.replaceAll("[^A-Za-z\\s]+", "");
        content = content.replaceAll("\\s+", " ");
        content = content.replaceAll("^\\s+", "");
        return content;
    }
}
