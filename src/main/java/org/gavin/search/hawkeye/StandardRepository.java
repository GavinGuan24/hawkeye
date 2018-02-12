package org.gavin.search.hawkeye;

import org.gavin.search.hawkeye.abstr.Repository;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;

/**
 * ---------------------------------------------------
 * File:    StandardRepository
 * Package: org.gavin.search.hawkeye
 * Project: hawkeye
 * ---------------------------------------------------
 * Created by gavinguan on 2018/1/19 09:56.
 * Copyright © 2018 gavinguan. All rights reserved.
 */
public abstract class StandardRepository<T> extends Repository<T> {

    @Override
    protected RAMDirectory customMakeRAMDirectory() throws IOException {return super.customMakeRAMDirectory();}

    @Override
    protected FSDirectory customMakeFSDirectory(String directoryPath) throws IOException {return null;}

    @Override
    protected IndexWriter customMakeIndexWriter(Analyzer analyzer) throws IOException {return null;}

    @Override
    protected DirectoryReader customMakeIndexReader() throws IOException {return null;}

    @Override
    protected Highlighter customMakeHighlighter(Query query) {return null;}

    public StandardRepository(String directoryPath) throws IOException {super(directoryPath, null);}

    public StandardRepository(String directoryPath, Analyzer analyzer) throws IOException {super(directoryPath, analyzer);}




}
