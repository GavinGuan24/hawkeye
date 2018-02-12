package org.gavin.search.result;

import org.apache.lucene.search.ScoreDoc;

import java.util.List;

/**
 * ---------------------------------------------------
 * File:    HittingSearchResult
 * Package: org.gavin.search.result
 * Project: hawkeye
 * ---------------------------------------------------
 * Created by gavinguan on 2018/1/22 16:25.
 * Copyright © 2018 gavinguan. All rights reserved.
 */
public class HittingSearchResult<T> {

    private long totalHits;
    private List<T> topList;
    private ScoreDoc after;

    public HittingSearchResult(long totalHits, List<T> topList, ScoreDoc after) {
        this.totalHits = totalHits;
        this.topList = topList;
        this.after = after;
    }

    public long getTotalHits() {
        return totalHits;
    }

    public List<T> getTopList() {
        return topList;
    }

    public ScoreDoc getAfter() {
        return after;
    }

}
