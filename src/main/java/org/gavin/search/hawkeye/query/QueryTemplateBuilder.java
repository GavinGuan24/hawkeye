package org.gavin.search.hawkeye.query;

import com.hankcs.hanlp.HanLP;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * ---------------------------------------------------
 * File:    QueryTemplateBuilder
 * Package: org.gavin.search.hawkeye.query
 * Project: hawkeye
 * ---------------------------------------------------
 * Created by gavinguan on 2018/1/25 14:45.
 * Copyright © 2018 gavinguan. All rights reserved.
 */
public abstract class QueryTemplateBuilder {

    public final static Query nullQuery = new BooleanQuery.Builder().build();

    /**
     * 对 <code>mustKey</code> 映射到 Term, 编写对应的检索条件
     * @param mustKey
     * @return
     */
    protected abstract List<Term> parseMustKey(final String mustKey);

    /**
     * 对 <code>filterKey</code> 映射到 Term, 编写对应的检索条件
     * @param filterKey
     * @return
     */
    protected abstract List<Term> parseFilterKey(final String filterKey);

    /**
     * 对 <code>shouldKey</code> 映射到 Term, 编写对应的检索条件
     * @param shouldKey
     * @return
     */
    protected abstract List<Term> parseShouldListKey(final String shouldKey);

    /**
     * 对 <code>mustNotKey</code> 映射到 Term, 编写对应的检索条件
     * @param mustNotKey
     * @return
     */
    protected abstract List<Term> parseMustNotKey(final String mustNotKey);

    /**
     * 模糊查询 (含中文)
     * NOTE: <code>key</code> 会被语法分析器解析为单词, 然后查询
     * @param key
     * @return
     */
    public final Query fuzzyQuery(String key) {
        if (key == null || key.length() == 0) {
            return nullQuery;
        }
        List<String> shouldList = new ArrayList<>(10);
        List<com.hankcs.hanlp.seg.common.Term> termList = HanLP.segment(key);
        for (com.hankcs.hanlp.seg.common.Term hanlpTerm : termList) {
            shouldList.add(hanlpTerm.word);
        }
        return advancedQuery(null, null, shouldList, null);
    }

    /**
     * 高级搜索
     * @param mustList      // 一定要包含的字段,       影响相关度排序
     * @param filterList    // 一定要包含的字段,    不 影响相关度排序
     * @param shouldList    // 可选包含字段,          影响相关度排序
     * @param mustNotList   // 一定不可包含的字段,     影响相关度排序
     * @return
     */
    public final Query advancedQuery(List<String> mustList,
                                     List<String> filterList,
                                     List<String> shouldList,
                                     List<String> mustNotList) {

        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        parseQueryKeys(builder, mustList, BooleanClause.Occur.MUST);
        parseQueryKeys(builder, filterList, BooleanClause.Occur.FILTER);
        parseQueryKeys(builder, shouldList, BooleanClause.Occur.SHOULD);
        parseQueryKeys(builder, mustNotList, BooleanClause.Occur.MUST_NOT);

        return builder.build();
    }

    private void parseQueryKeys(BooleanQuery.Builder builder, List<String> keyList, BooleanClause.Occur occur) {

        if (keyList == null || keyList.size() == 0) return;

        for (String key : keyList) {
            if (key == null || key.length() == 0) continue;

            List<Term> termList = null;
            if (occur.equals(BooleanClause.Occur.MUST)) {
                termList = parseMustKey(key);
            }
            if (occur.equals(BooleanClause.Occur.FILTER)) {
                termList = parseFilterKey(key);
            }
            if (occur.equals(BooleanClause.Occur.SHOULD)) {
                termList = parseShouldListKey(key);
            }
            if (occur.equals(BooleanClause.Occur.MUST_NOT)) {
                termList = parseMustNotKey(key);
            }
            if (termList == null || termList.size() == 0) return;

            for (Term term : termList) {
                builder.add(new TermQuery(term), occur);
            }
        }
    }

}
