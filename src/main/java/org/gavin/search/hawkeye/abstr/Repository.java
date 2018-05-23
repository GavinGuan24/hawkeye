package org.gavin.search.hawkeye.abstr;

import com.hankcs.lucene.HanLPIndexAnalyzer;
import org.gavin.search.hawkeye.query.PagingQuery;
import org.gavin.search.hawkeye.result.HittingSearchResult;
import org.gavin.search.hawkeye.result.PagingQueryResult;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * ---------------------------------------------------
 * File:    Repository
 * Package: org.gavin.search.hawkeye
 * Project: hawkeye
 * ---------------------------------------------------
 * Created by gavinguan on 2018/1/19 09:52.
 * Copyright © 2018 gavinguan. All rights reserved.
 */
public abstract class Repository<T> {

    private static final String HighLightLabS = "<b><font color=red>";
    private static final String HighLightLabE = "</font></b>";

    private String directoryPath;
    private Directory directory;
    private Analyzer analyzer;
    private DirectoryReader indexReader;

    private boolean closed;

    // TODO: 私有方法 private methods

    //调用该方法的方法需要 synchronized (this = Repository)
    private IndexWriter getIndexWriter() throws IOException {
        checkClosed();
        IndexWriter indexWriter = customMakeIndexWriter(this.analyzer);
        if (indexWriter == null) {
            //writer: 默认 标准语法解析器, 写模式 -> 新建与追加
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(this.analyzer);
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            //这里只要保证同一时刻只有一个 IndexWriter 就不会 throw LockObtainFailedException
            indexWriter = new IndexWriter(this.directory, indexWriterConfig);
        } else if (!indexWriter.isOpen()) {
            throw new IOException("This Custom-Make IndexWriter Has Been Closed");
        }
        return indexWriter;
    }


    private DirectoryReader getIndexReader() throws IOException {
        checkClosed();
        DirectoryReader directoryReader = customMakeIndexReader();
        if (directoryReader == null) {
            directoryReader = DirectoryReader.open(directory);
        } else if (directoryReader.getRefCount() == 0) {
            throw new IOException("This Custom-Make IndexReader Has Been Closed");
        }
        return directoryReader;
    }

    private Highlighter getHighlighter(Query query) {
        Highlighter highlighter = customMakeHighlighter(query);
        if (highlighter == null) {
            highlighter = new Highlighter(new SimpleHTMLFormatter(HighLightLabS, HighLightLabE), new QueryScorer(query));
        }
        return highlighter;
    }


    private void checkClosed() throws IOException {
        if (this.closed) throw new IOException("This Repository Has Been Closed");
    }


    private void commitAndCloseIndexWriter(IndexWriter indexWriter) throws IOException {
        indexWriter.commit();
        indexWriter.close();
    }

    private SearchResponse search(Query query,
                                  int n,
                                  ScoreDoc after)
            throws IOException {
        checkClosed();
        if (query == null) throw new IOException("Query Mustn't be Null !!!");

        if (this.indexReader == null) {
            this.indexReader = getIndexReader();
        } else {
            DirectoryReader newDirectoryReader = DirectoryReader.openIfChanged(this.indexReader);
            if (newDirectoryReader != null) {
                this.indexReader.close();
                this.indexReader = newDirectoryReader;
            }
        }

        IndexSearcher indexSearcher = new IndexSearcher(this.indexReader);
        TopDocs topDocs = null;
        if (after == null) {
            topDocs = indexSearcher.search(query,n);
        } else {
            topDocs = indexSearcher.searchAfter(after, query, n);
        }

        return new SearchResponse(topDocs, indexSearcher);
    }

    // TODO: 子类方法抽象

    /**
     * 构建 Repository 时, 若没有 directoryPath, 则需要实现 RAMDirectory 定制方法
     * @return
     * @throws IOException
     */
    protected RAMDirectory customMakeRAMDirectory() throws IOException {
        return null;
    }

    /**
     * 构建 Repository 时, FSDirectory 定制方法
     * @param directoryPath
     * @return
     * @throws IOException
     */
    protected abstract FSDirectory customMakeFSDirectory(final String directoryPath) throws IOException;

    /**
     * 构建 Repository 时, IndexWriter 定制方法
     * @param analyzer
     * @return
     * @throws IOException
     */
    protected abstract IndexWriter customMakeIndexWriter(final Analyzer analyzer) throws IOException;

    /**
     * 构建 Repository 时, IndexReader 定制方法
     * @return
     * @throws IOException
     */
    protected abstract DirectoryReader customMakeIndexReader() throws IOException;

    /**
     * 定制 荧光笔 for 高亮模式
     * @param query
     * @return
     */
    protected abstract Highlighter customMakeHighlighter(Query query);

    /**
     * 定制 用户数据 -> 内部文档 的 实现
     * @param source
     * @param target
     */
    protected abstract void parseObject2InternalDocument(final T source, final Document target);

    /**
     * 定制 内部文档 -> 用户数据 的 实现
     * @param source
     * @return
     */
    protected abstract T parseInternalDocument2Object(final Document source);

    /**
     * 定制 内部文档(高亮模式) -> 用户数据 的 实现
     * @param source
     * @param highlighter
     * @param analyzer
     * @return
     */
    protected abstract T parseInternalDocument2ObjectWithHighlighter(final Document source, final Highlighter highlighter, final Analyzer analyzer);

    //TODO: Repository 公开方法

    /**
     * 构建一个仓库 Repository
     * @param directoryPath @nullable: When directoryPath is null, directory will be created as RAMRepository.
     * @param analyzer @nullable: Default is HanLPIndexAnalyzer.
     * @throws IOException
     */
    protected Repository(String directoryPath, Analyzer analyzer) throws IOException {
        this.closed = false;
        //区分 FSDirectory 与 RAMDirectory, 抽象自定义方法
        if (directoryPath == null || directoryPath.length() == 0) {
            RAMDirectory ramDirectory = customMakeRAMDirectory();
            if (ramDirectory == null) {
                throw new IOException("There is no available directory");
            } else {
                this.directory = ramDirectory;
            }
        } else {
            final Path folderPath = Paths.get(directoryPath);
            if (!Files.exists(folderPath)) {
                Files.createDirectory(folderPath);
            }
            this.directoryPath = folderPath.toFile().getAbsolutePath();
            FSDirectory fsDirectory = customMakeFSDirectory(this.directoryPath);
            if (fsDirectory != null) {
                this.directory = fsDirectory;
            } else {
                this.directory = FSDirectory.open(folderPath);
            }
        }
        //为仓库配置 writer reader 的 解析器, 配置 公用reader
        if (analyzer == null) {
//            analyzer = new StandardAnalyzer();
            analyzer = new HanLPIndexAnalyzer();
        }
        this.analyzer = analyzer;
    }

    /**
     * 如果是 FSDirectory 的 Repository, 返回仓库绝对路径
     * @return
     */
    public final String getFSRepositoryPath() {
        return this.directoryPath;
    }

    /**
     * 是否是 RAMDirectory 的 Repository
     * @return
     */
    public final boolean isRAMRepository() {
        return this.directoryPath == null;
    }

    /**
     * 关闭仓库 Repository
     * @throws IOException
     */
    public final void close() throws IOException {
        checkClosed();
        if (this.indexReader != null) this.indexReader.close();
        this.directory.close();
        this.closed = true;
    }

    /**
     * 返回 Repository 是否被关闭
     * @return
     */
    public final boolean closed() {
        return this.closed;
    }


    //TODO Repository.Document 方法 - 增, 删, 改, 查

    /**
     * 加 Document 至 Repository
     * @param document
     * @throws IOException
     */
    public synchronized final void addDocument(T document) throws IOException {
        checkClosed();
        if (document != null) {
            Document target = new Document();
            parseObject2InternalDocument(document, target);
            if (target.getFields().size() == 0) throw new IOException("Method (parseObject2InternalDocument) Must add Fields to Document");
            IndexWriter indexWriter = getIndexWriter();
            indexWriter.addDocument(target);
            commitAndCloseIndexWriter(indexWriter);
        } else {
            throw new IOException("Param \"document\" is Null !!!");
        }
    }

    /**
     * 批量添加 Document 至 Repository
     * @param documents
     * @throws IOException
     */
    public synchronized final void addDocuments(List<T> documents) throws IOException {
        checkClosed();
        if (documents != null && documents.size() > 0) {
            List<Document> documentList = new ArrayList<>(documents.size());
            for (T document : documents) {
                Document target = new Document();
                parseObject2InternalDocument(document, target);
                if (target.getFields().size() == 0) throw new IOException("Method (parseObject2InternalDocument) Must add Fields to Document");
                documentList.add(target);
            }
            IndexWriter indexWriter = getIndexWriter();
            indexWriter.addDocuments(documentList);
            commitAndCloseIndexWriter(indexWriter);
        } else {
            throw new IOException("Param \"documents\" is Null !!!");
        }
    }

    /**
     * 清除 Repository 中 所有 Document
     * @param forced 强制清除仓库目录下所有索引文件: 当 <code>forced</code> 为 true, 强制关闭当前仓库的indexReader, 然后清除仓库目录下所有文件(不含文件夹)
     * @throws IOException
     */
    public final void cleanAllDocuments(boolean forced) throws IOException {
        checkClosed();
        if (!forced) {
            IndexWriter indexWriter = getIndexWriter();
            indexWriter.deleteAll();
            commitAndCloseIndexWriter(indexWriter);
        } else {
            if (this.indexReader != null) {
                this.indexReader.close();
                this.indexReader = null;
            }
            for (Object temp : Files.list(Paths.get(this.directoryPath)).toArray()) {
                Path path = (Path) temp;
                File file = path.toFile();
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }

    /**
     * 删除 Repository 中 符合条件的 Documents
     * @param terms
     * @throws IOException
     */
    public final void deleteDocuments(Term...terms) throws IOException {
        checkClosed();
        if (terms == null || terms.length == 0) throw new IOException("Term List is unavailable");
        for (Term tempTerm : terms) {
            if (tempTerm == null) throw new IOException("Term is unavailable");
        }
        IndexWriter indexWriter = getIndexWriter();
        indexWriter.deleteDocuments(terms);
        commitAndCloseIndexWriter(indexWriter);
    }

    /**
     * 删除 Repository 中 符合条件的 Documents
     * @param queries
     * @throws IOException
     */
    public final void deleteDocuments(Query...queries) throws IOException {
        checkClosed();
        if (queries == null || queries.length == 0) throw new IOException("Query List is unavailable");
        for (Query tempQuery : queries) {
            if (tempQuery == null) throw new IOException("Query List is unavailable");
        }
        IndexWriter indexWriter = getIndexWriter();
        indexWriter.deleteDocuments(queries);
        commitAndCloseIndexWriter(indexWriter);
    }

    /**
     * 检出 前 <code>n</code>条数据(按相关度 由高至低 排序)
     * NOTE: 如果 <code>after</code> 为空, 查询为 Top 模式, 否则为 paging 模式
     * NOTE: <code>after</code> 数据如果是伪造的, 将会 抛出异常 或 视 <code>after</code> 为null值
     * NOTE: 该方法的 paging 模式, 适用于 --> 亿数量级数据
     * NOTE: 默认没有高亮处理
     * @param query not null: 查询语句
     * @param n     not null: 本次查询最多 <code>n<code/> 条数据
     * @param after nullable: 传入某次查询后, 最后一个数据, 即可在这个数据之后继续查询最多 <code>n<code/> 条数据
     * @return HittingSearchResult: 如果 HittingSearchResult.after 为null, 说明本次查询的参数 <code>after</code> 已经是最后一条数据
     * @throws IOException
     */
    public final HittingSearchResult<T> hittingSearch(Query query, int n, ScoreDoc after)
            throws IOException {
        return hittingSearch(query, n, after, false);
    }

    /**
     * 检出 前 <code>n</code>条数据(按相关度 由高至低 排序)
     * NOTE: 如果 <code>after</code> 为空, 查询为 Top 模式, 否则为 paging 模式
     * NOTE: <code>after</code> 数据如果是伪造的, 将会 抛出异常 或 视 <code>after</code> 为null值
     * NOTE: 该方法的 paging 模式, 适用于 --> 亿数量级数据
     * @param query not null: 查询语句
     * @param n     not null: 本次查询最多 <code>n<code/> 条数据
     * @param after nullable: 传入某次查询后, 最后一个数据, 即可在这个数据之后继续查询最多 <code>n<code/> 条数据
     * @param highlight not null: 是否高亮关键字词
     * @return HittingSearchResult: 如果 HittingSearchResult.after 为null, 说明本次查询的参数 <code>after</code> 已经是最后一条数据
     * @throws IOException
     */
    public final HittingSearchResult<T> hittingSearch(Query query, int n, ScoreDoc after, boolean highlight)
            throws IOException {

        SearchResponse searchResponse = null;
        try {
            searchResponse = search(query, n, after);
        } catch (IndexNotFoundException e) {
            e.printStackTrace();
            //从未建立过索引, 直接返回 0 命中
            return new HittingSearchResult<>(0, new ArrayList<>(), null);
        }


        TopDocs topDocs = searchResponse.getTopDocs();
        IndexSearcher indexSearcher = searchResponse.getIndexSearcher();

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        if (scoreDocs.length > 0) {
            List<T> hitsList = new ArrayList<>(n);
            for (ScoreDoc scoreDoc : scoreDocs) {
                Document document = indexSearcher.doc(scoreDoc.doc);
                //相关度: scoreDoc.score
                T data = null;
                if (highlight) {
                    data = parseInternalDocument2ObjectWithHighlighter(document, getHighlighter(query), this.analyzer);
                } else {
                    data = parseInternalDocument2Object(document);
                }
                if (data == null) throw new IOException("The result of \"parseInternalDocument2Object(WithHighlighter)\" Mustn't be Null");
                hitsList.add(data);
            }

            ScoreDoc newAfter = scoreDocs[scoreDocs.length - 1];

            return new HittingSearchResult<>(topDocs.totalHits, hitsList, newAfter);
        } else {
            return new HittingSearchResult<>(topDocs.totalHits, new ArrayList<>(), null);
        }
    }

    /**
     * 分页查询, 实现方式: 使用 top 模式 查询, 内部进行分页操作
     * NOTE: 该方法适用于 千万数量级数据 及以下
     * NOTE: 该方法内部限制最多 hits 9kw 条数据
     * @param pagingQuery 该参数 包含 1.查询条件 org.apache.lucene.search.Query| 2.分页数据
     * @return
     * @throws IOException
     */
    public final PagingQueryResult<T> pagingSearch(PagingQuery pagingQuery) throws IOException {
        SearchResponse searchResponse = null;
        try {
            searchResponse = search(pagingQuery.getCoreQuery(), pagingQuery.getHittingMax(), null);
        } catch (IndexNotFoundException e) {
            e.printStackTrace();
            //从未建立过索引, 直接返回 0 命中
            return new PagingQueryResult<>(0, new ArrayList<>(), pagingQuery.getPageIndex(), pagingQuery.getPageSize());
        }

        IndexSearcher indexSearcher = searchResponse.getIndexSearcher();
        TopDocs topDocs = searchResponse.getTopDocs();
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        //使用 top 模式 查询时
        //pagingQuery.getHittingMax()是当次查询结果数量上限, 如果命中结果数超过该值, 限制到HittingMax
        int totalHits = (int) ((topDocs.totalHits > pagingQuery.getHittingMax())?pagingQuery.getHittingMax():topDocs.totalHits);

        if (pagingQuery.getPageOffset() < totalHits) {
            List<T> pagingData = new ArrayList<>(pagingQuery.getPageSize());

            int estimateMax = pagingQuery.getPageOffset() + pagingQuery.getPageSize();
            int realMax = (estimateMax > totalHits)?totalHits:estimateMax;

            for (int index = pagingQuery.getPageOffset(); index < realMax; index++) {
                ScoreDoc scoreDoc = scoreDocs[index];
                Document document = indexSearcher.doc(scoreDoc.doc);
                //相关度: scoreDoc.score
                T data = null;
                if (pagingQuery.getHighlight()) {
                    data = parseInternalDocument2ObjectWithHighlighter(document, getHighlighter(pagingQuery.getCoreQuery()), this.analyzer);
                } else {
                    data = parseInternalDocument2Object(document);
                }
                if (data == null) throw new IOException("The result of \"parseInternalDocument2Object(WithHighlighter)\" Mustn't be Null");
                pagingData.add(data);
            }

            return new PagingQueryResult<>(totalHits, pagingData, pagingQuery.getPageIndex(), pagingQuery.getPageSize());
        } else {
            return new PagingQueryResult<>(totalHits, new ArrayList<>(), pagingQuery.getPageIndex(), pagingQuery.getPageSize());
        }
    }

    /**
     * 默认高亮文本缩略方法: 将 由默认高亮笔处理后的 文本 进行缩略, 仅保留部分上下文
     * @param source 由默认高亮笔处理后的文本
     * @return
     */
    public static String standardHighLightOptimizer(String source) {
        if (source == null || source.length() == 0) return "";
        StringBuilder targetSB = new StringBuilder("");
        //非空开头上文字数上限
        int startLenMx = 6;
        int contextLenMx = 5;
        int endLenMx = 6;

        String[] splitList0 = source.split(HighLightLabS);
        int count = splitList0.length;
        String temp = null;
        for (int i = 0; i < count; i++) {
            String item = splitList0[i];

            String str0 = null;
            if (!item.contains(HighLightLabE)) {
                //文本开头
                str0 = (item.length() > startLenMx)?("..." + item.substring(item.length() - startLenMx, item.length())):(item);
            } else {
                String key = item.substring(0, item.indexOf(HighLightLabE));
                String tail = item.substring(item.indexOf(HighLightLabE) + HighLightLabE.length());
                //缓存非空, 经行合并
                if (temp != null) {
                    key = temp + key;
                    temp = null;
                }

                if (i != count - 1) {
                    //片段中
                    if (!tail.equals("")) {
                        //片段含上下文
                        if (tail.length() > 2*contextLenMx) {
                            str0 = HighLightLabS + key + HighLightLabE + tail.substring(0, contextLenMx) + "..." + tail.substring(tail.length() - contextLenMx);
                        } else {
                            str0 = HighLightLabS + key + HighLightLabE + tail;
                        }
                    } else {
                        //需要下次进行合并操作
                        temp = key;
                        continue;
                    }
                } else {
                    //结尾
                    if (tail.length() > endLenMx) {
                        str0 = HighLightLabS + key + HighLightLabE + tail.substring(0, endLenMx) + "...";
                    } else {
                        str0 = HighLightLabS + key + HighLightLabE + tail;
                    }
                }
            }
            targetSB.append(str0);
        }
        return targetSB.toString();
    }


    // TODO: ------------------- 未完成 或 未完善 的 方法 --------------------

    /**
     * 将 RAMRepository 的 Documents 复制给 FSRepository, 无合并操作
     * @param target
     * @throws IOException
     */

    private final void flushAllDocuments2Repository(Repository target) throws IOException {
        if (this.equals(target)) return;
        checkClosed();target.checkClosed();
        if (!(isRAMRepository() && !target.isRAMRepository())) {
            throw new IOException("Self must be a RAM Repository, and target must be a FS Repository");
        }
        IndexWriter indexWriter = getIndexWriter();
        indexWriter.addIndexes(target.directory);
        commitAndCloseIndexWriter(indexWriter);
    }

}

class SearchResponse {

    private TopDocs topDocs;
    private IndexSearcher indexSearcher;

    SearchResponse(TopDocs topDocs, IndexSearcher indexSearcher) {
        this.topDocs = topDocs;
        this.indexSearcher = indexSearcher;
    }

    TopDocs getTopDocs() {
        return topDocs;
    }

    IndexSearcher getIndexSearcher() {
        return indexSearcher;
    }
}


