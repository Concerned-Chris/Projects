package de.fau.qLStore.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class QuerySimilaritySearch {

//    public qLStoreQuery self;

    public List<qLStoreQuery> similaritySearchResult = new ArrayList<>();

    public final int LIMIT = 9;
    private AtomicInteger offset = new AtomicInteger(0);

//    public QuerySimilaritySearch(qLStoreQuery query){
//        this.self = query;
//    }

    public List<SimilarQueryInfo> getQueryIdentifiersForCurrentPage(AtomicInteger currentPage){
        List<SimilarQueryInfo> result = new ArrayList<>();
        for (int i = currentPage.get() * LIMIT ; i < (currentPage.get() + 1) * LIMIT; i++){
            if(i >= similaritySearchResult.size()) break;
            result.add(new SimilarQueryInfo(similaritySearchResult.get(i).queryIdentifier,
                    similaritySearchResult.get(i).similarityTypes, similaritySearchResult.get(i).similarityRates));
        }
        return result;
    }

    public qLStoreQuery getQLStoreQueryByQueryIdentifier(String queryIdentifier){
        for (qLStoreQuery query : this.similaritySearchResult){
            if(query.queryIdentifier.equals(queryIdentifier)) return query;
        }
        return null;
    }
}
