package de.fau.qLStore.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class QuerySearch {

    private String queryType;
    private String source;
    private Boolean organic;
    private Boolean timeout;

    private final int LIMIT = 9;
    private AtomicInteger offset = new AtomicInteger(0);

    private List<qLStoreQuery> searchResult;

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getOrganic() {
        return organic;
    }

    public void setOrganic(Boolean organic) {
        this.organic = organic;
    }

    public Boolean getTimeout() {
        return timeout;
    }

    public void setTimeout(Boolean timeout) {
        this.timeout = timeout;
    }

    public int getLIMIT() {
        return LIMIT;
    }

    public AtomicInteger getOffset() {
        return offset;
    }

    public void setOffset(AtomicInteger offset) {
        this.offset = offset;
    }

    public List<qLStoreQuery> getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(List<qLStoreQuery> searchResult) {
        this.searchResult = searchResult;
    }

    public List<String> getQueryIdentifiersForCurrentPage(AtomicInteger currentPage){
        List<String> result = new ArrayList<>();
        for (int i = currentPage.get() * LIMIT ; i < (currentPage.get() + 1) * LIMIT; i++){
            if(i >= searchResult.size()) break;
            result.add(searchResult.get(i).queryIdentifier);
        }
        return result;
    }

    public qLStoreQuery getQLStoreQueryByQueryIdentifier(String queryIdentifier){
        for (qLStoreQuery query : this.searchResult){
            if(query.queryIdentifier.equals(queryIdentifier)) return query;
        }
        return null;
    }




}
