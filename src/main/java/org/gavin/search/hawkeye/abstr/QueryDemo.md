```java
//    /**
//     * 对特定项搜索
//     *  按词条搜索—TermQuery
//     *TermQuery是最简单、也是最常用的Query。TermQuery可以理解成为“词条搜索”，
//     * 在搜索引擎中最基本的搜索就是在索引中搜索某一词条，而TermQuery就是用来完成这项工作的。
//     * 在Lucene中词条是最基本的搜索单位，从本质上来讲一个词条其实就是一个名/值对。
//     * 只不过这个“名”是字段名，而“值”则表示字段中所包含的某个关键字。
//     * @throws Exception
//     */
//    @Test
//    public void testTermQuery()throws Exception{
//        String searchField="contents";
//        String q="xxxxxxxxx$";
//        Term t=new Term(searchField,q);
//        Query query=new TermQuery(t);
//        TopDocs hits=is.search(query, 10);
//        System.out.println("匹配 '"+q+"'，总共查询到"+hits.totalHits+"个文档");
//        for(ScoreDoc scoreDoc:hits.scoreDocs){
//            Document doc=is.doc(scoreDoc.doc);
//            System.out.println(doc.get("fullPath"));
//        }
//    }
//
//    /**
//     * “多条件查询”搜索—BooleanQuery
//     * BooleanQuery也是实际开发过程中经常使用的一种Query。
//     * 它其实是一个组合的Query，在使用时可以把各种Query对象添加进去并标明它们之间的逻辑关系。
//     * 在本节中所讨论的所有查询类型都可以使用BooleanQuery综合起来。
//     * BooleanQuery本身来讲是一个布尔子句的容器，它提供了专门的API方法往其中添加子句，
//     * 并标明它们之间的关系，以下代码为BooleanQuery提供的用于添加子句的API接口：
//     * @throws Exception
//     */
//    @Test
//    public void testBooleanQuery()throws Exception{
//        String searchField="contents";
//        String q1="xxxxxxxxx";
//        String q2="oooooooooooooooo";
//        Query query1=new TermQuery(new Term(searchField,q1));
//        Query query2=new TermQuery(new Term(searchField,q2));
//        BooleanQuery.Builder  builder=new BooleanQuery.Builder();
//        //  1．MUST和MUST：取得连个查询子句的交集。
//        //  2．MUST和MUST_NOT：表示查询结果中不能包含MUST_NOT所对应得查询子句的检索结果。
//        // 3．SHOULD与MUST_NOT：连用时，功能同MUST和MUST_NOT。
//        // 4．SHOULD与MUST连用时，结果为MUST子句的检索结果,但是SHOULD可影响排序。
//        // 5．SHOULD与SHOULD：表示“或”关系，最终检索结果为所有检索子句的并集。
//        // 6．MUST_NOT和MUST_NOT：无意义，检索无结果。
//        builder.add(query1, BooleanClause.Occur.MUST);
//        builder.add(query2, BooleanClause.Occur.MUST);
//        BooleanQuery  booleanQuery=builder.build();
//        TopDocs hits=is.search(booleanQuery, 10);
//        System.out.println("匹配 "+q1 +"And"+q2+"，总共查询到"+hits.totalHits+"个文档");
//        for(ScoreDoc scoreDoc:hits.scoreDocs){
//            Document doc=is.doc(scoreDoc.doc);
//            System.out.println(doc.get("fullPath"));
//        }
//    }
//
//    /**
//     * TermRangeQuery 范围查询
//     *TermRangeQuery是用于字符串范围查询的，既然涉及到范围必然需要字符串比较大小，
//     * 字符串比较大小其实比较的是ASC码值，即ASC码范围查询。
//     * 一般对于英文来说，进行ASC码范围查询还有那么一点意义，
//     * 中文汉字进行ASC码值比较没什么太大意义，所以这个TermRangeQuery了解就行，
//     * 用途不太大，一般数字范围查询NumericRangeQuery用的比较多一点，
//     * 比如价格，年龄，金额，数量等等都涉及到数字，数字范围查询需求也很普遍。
//     * @throws Exception
//     */
//    @Test
//    public void testTermRangeQuery()throws Exception{
//        String searchField="contents";
//        String q="1000001----1000002";
//        String lowerTermString = "1000001";
//        String upperTermString = "1000003";
//        /**
//         * field  字段
//         * lowerterm -范围的下端的文字
//         *upperterm -范围的上限内的文本
//         *includelower -如果真的lowerterm被纳入范围。
//         *includeupper -如果真的upperterm被纳入范围。
//         *https://yq.aliyun.com/articles/45353
//         */
//        Query query=new TermRangeQuery(searchField,new BytesRef(lowerTermString),new BytesRef(upperTermString),true,true);
//        TopDocs hits=is.search(query, 10);
//        System.out.println("匹配 '"+q+"'，总共查询到"+hits.totalHits+"个文档");
//        for(ScoreDoc scoreDoc:hits.scoreDocs){
//            Document doc=is.doc(scoreDoc.doc);
//            System.out.println(doc.get("fullPath"));
//        }
//    }
//
//
//    /**
//     * PrefixQuery  PrefixQuery用于匹配其索引开始以指定的字符串的文档。就是文档中存在xxx%
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testPrefixQuery()throws Exception{
//        String searchField="contents";
//        String q="1license";
//        Term t=new Term(searchField,q);
//        Query query=new PrefixQuery(t);
//        TopDocs hits=is.search(query, 10);
//        System.out.println("匹配 '"+q+"'，总共查询到"+hits.totalHits+"个文档");
//
//        for(ScoreDoc scoreDoc:hits.scoreDocs){
//            Document doc=is.doc(scoreDoc.doc);
//            System.out.println(doc.get("fullPath"));
//        }
//    }
//
//    /**
//     *  所谓PhraseQuery，就是通过短语来检索，比如我想查“big car”这个短语，
//     *  那么如果待匹配的document的指定项里包含了"big car"这个短语，
//     *  这个document就算匹配成功。可如果待匹配的句子里包含的是“big black car”，
//     *  那么就无法匹配成功了，如果也想让这个匹配，就需要设定slop，
//     *  先给出slop的概念：slop是指两个项的位置之间允许的最大间隔距离
//     * @throws Exception
//     */
//    @Test
//    public void testPhraseQuery()throws Exception{
//        String searchField="contents";
//        String q1="xxxx";
//        String q2="bbb";
//        Term t1=new Term(searchField,q1);
//        Term t2=new Term(searchField,q2);
//        PhraseQuery.Builder builder=new PhraseQuery.Builder();
//        builder.add(t1);
//        builder.add(t2);
//        builder.setSlop(0);
//        PhraseQuery query=builder.build();
//        TopDocs hits=is.search(query, 10);
//        System.out.println("匹配 '"+q1+q2+"之间的几个字段"+"，总共查询到"+hits.totalHits+"个文档");
//
//        for(ScoreDoc scoreDoc:hits.scoreDocs){
//            Document doc=is.doc(scoreDoc.doc);
//            System.out.println(doc.get("fullPath"));
//        }
//    }
//
//
//    /**
//     * 相近词语的搜索—FuzzyQuery
//     * FuzzyQuery是一种模糊查询，它可以简单地识别两个相近的词语。
//     * @throws Exception
//     */
//    @Test
//    public void testFuzzyQuery()throws Exception{
//        String searchField="contents";
//        String q="ljlxx";
//        Term t=new Term(searchField,q);
//        Query query=new FuzzyQuery(t);
//        TopDocs hits=is.search(query, 10);
//        System.out.println("匹配 '"+q+"'，总共查询到"+hits.totalHits+"个文档");
//
//        for(ScoreDoc scoreDoc:hits.scoreDocs){
//            Document doc=is.doc(scoreDoc.doc);
//            System.out.println(doc.get("fullPath"));
//        }
//    }
//
//    /**
//     * 使用通配符搜索—WildcardQuery
//     * Lucene也提供了通配符的查询，这就是WildcardQuery。
//     * 通配符“?”代表1个字符，而“*”则代表0至多个字符。
//     * @throws Exception
//     */
//    @Test
//    public void testWildcardQuery()throws Exception{
//        String searchField="contents";
//        String q="bb??qq";
//        Term t=new Term(searchField,q);
//        Query query=new WildcardQuery(t);
//        TopDocs hits=is.search(query, 10);
//        System.out.println("匹配 '"+q+"'，总共查询到"+hits.totalHits+"个文档");
//
//        for(ScoreDoc scoreDoc:hits.scoreDocs){
//            Document doc=is.doc(scoreDoc.doc);
//            System.out.println(doc.get("fullPath"));
//        }
//    }
//
//    /**
//     * 解析查询表达式
//     * QueryParser实际上就是一个解析用户输入的工具，可以通过扫描用户输入的字符串，生成Query对象，以下是一个代码示例：
//     * @throws Exception
//     */
//    @Test
//    public void testQueryParser()throws Exception{
//        Analyzer analyzer=new StandardAnalyzer(); // 标准分词器
//        String searchField="contents";
//        String q="xxxxxxxxx$";
//        //指定搜索字段和分析器
//        QueryParser parser=new QueryParser(searchField, analyzer);
//        //用户输入内容
//        Query query=parser.parse(q);
//        TopDocs hits=is.search(query, 100);
//        System.out.println("匹配 "+q+"查询到"+hits.totalHits+"个记录");
//        for(ScoreDoc scoreDoc:hits.scoreDocs){
//            Document doc=is.doc(scoreDoc.doc);
//            System.out.println(doc.get("fullPath"));
//        }
//    }
//
//    @Override
//    public Long case0() throws IOException {
//
//        //测试 Object
//        String idStrings = "8a9b0aa15e3c6816015e50d14af9031b,8a9b0aa15d978917015d9d022d6501c5,8a9b0aa15d978917015d9d1a512001e6,8a9b0aa15bb379d6015be6cb730b66a6,8a9b0aa15bb379d6015be6f3b5c66725,8a9b0aa15d211597015d2b2ff3df0685,8a9b0aa15bb379d6015bc86e1dbf1184,8a9b0aa15b7b405e015b8034eb7809a6,8a9b0aa15bb379d6015c0b90d7ae25de,8a9b0ac45d9d167e015d9d2501e20002,8a9b0aa15d211597015d2b21b74c0669,8a9b0aa15b7b405e015b80238aea0973,8a9b0aa15d978917015d9cf8acab01be,8a9b0aa15b7b405e015b807c64fa0a92,8a9b0aa15d211597015d2b70d3f706ae,8a9b0aa15bb379d6015c2edaaac668f7,8a9b0aa15c88365b015d078dc4c735fb,8a9b0aa15c88365b015d07e688eb3638,8a9b0aa15d978917015d9d09582701c7,8a9b0ac45a225624015a26f1392f002c,8a9b0aa15bb379d6015c2ea3ab8668e2,8a9b0aa15a6f4aec015a7e8eba2b00b3,8a9b0aa15b1991e4015b1d6eaf220e8c,8a9b0aa15c88365b015ce31694150ba3,8a9b0aa15b859de4015b85f6c5fe0099,8a9b0aa15bb379d6015c0af7bf9d2587,8a9b0aa15b7b405e015b7f2172fc03f2,8a9b0aa15af51b36015af5249e64006a,8a9b0aa15b859de4015b85cf225e0036,8a9b0aa15c343617015c5cc2683d3c35,8a9b0aa15c88365b015d07cc75cc361c,8a9b0ac45a225624015a26d642110023,8a9b0aa15c88365b015d07db48e4362a,8a9b0ac45a225624015a271b52f00048,8a9b0ac45a225624015a26a68490000f,8a9b0aa15c88365b015d079c453935fe,8a9b0aa15b1991e4015b1d783a900ec6,8a9b0aa15d211597015d2b66375406aa,8a9b0aa15c88365b015ce8883fdf0f11,8a9b0aa15b859de4015b85e94e460089,8a9b0aa15c88365b015ce8a9624b0f1f,8a9b0aa15c343617015c7703bdb85da3,8a9b0aa15b1991e4015b1d5533d90d5f,8a9b0aa15a6f4aec015a7e94693d00c0,8a9b0aa15b1991e4015b1d8b6c9f0ec9,8a9b0aa15b1991e4015b1d5efb580dd0,8a9b0aa15bb379d6015c0b6feb3825c5,8a9b0aa15d211597015d2b78e9e806b1,8a9b0aa15c88365b015ce843501d0ede,8a9b0aa15c343617015c5d467aff3d0d,8a9b0aa15b7b405e015b80a2871f0ad1,8a9b0aa15c88365b015ce2e6648a0b9a,8a9b0aa15d211597015d2b53bc7f06a4,8a9b0ac45a225624015a26f655c90033,8a9b0aa15c88365b015ce910c29f0f51,8a9b0aa15c343617015c772287b95e18,8a9b0ac45a225624015a268568540002,8a9b0aa15b859de4015b8606083a00c5,8a9b0aa15c88365b015ce31d6b600ba8,8a9b0aa15b1991e4015b1d2ebba10ced,8a9b0ac45a225624015a26b2ac7f0019,8a9b0aa15d978917015d9d121d7201c9,8a9b0aa15c343617015c76f29aa35da0,8a9b0aa15b1991e4015b1d4f887d0d27,8a9b0aa15c88365b015d07e352573634,8a9b0aa15bb379d6015bc87c31b71189,8a9b0aa15b7b405e015b8070a0890a86,8a9b0aa15c88365b015ce2fc1cbb0b9e,8a9b0aa15c88365b015d07b84391360b,8a9b0aa15d211597015d2b5e905e06a7,8a9b0aa15d211597015d2b37c0070687,8a9b0aa15bb379d6015bc86221431171,8a9b0aa15af5929e015b100e756f2f4a,8a9b0aa15d211597015d2b7fc75206b4,8a9b0ac45a225624015a270ad966003a,8a9b0aa15b7b405e015b80610f970a74,8a9b0ac45a225624015a270eee26003d,8a9b0aa15b7b405e015b8014c4630950,8a9b0ac45a6f4bd7015a7ea719fa0090,8a9b0aa15c88365b015ce283329a0acd,8a9b0aa15d211597015d2b89c88006b6,8a9b0aa15c88365b015ce86bdb660f08,8a9b0aa15bb379d6015c2e128248687a,8a9b0aa15bb379d6015be79e562f681a,8a9b0aa15d211597015d2b4871d70698,8a9b0ac45aa6f5f2015aa709e659004d,8a9b0aa15c343617015c5d2d161b3d0a,8a9b0aa15c88365b015ce26ce8ee04f0,8a9b0aa15b7b405e015b804e4b830a30,8a9b0aa15b7b405e015b7ff9d14804ec";
//        String[] _idStrings = idStrings.split(",");
//        List<String> idList = new ArrayList<>(100);
//        for (String temp : _idStrings) {
//            idList.add(temp);
//        }
//
//        MMTeachingPlanExample teachingPlanExample = new MMTeachingPlanExample();
//        teachingPlanExample.createCriteria()
//                .andIdIn(idList);
//
//        List<MMTeachingPlan> planList = teachingPlanMapper.selectByExampleWithBLOBs(teachingPlanExample);
//
//        logger.info("测试教案总量: " + planList.size());
//        int indexNum = 0;
//
//        --------------------------------------------------------------
//
//        long startTime = new Date().getTime();
//
//        //安检测试文件夹路径
//        final Path folderPath = Paths.get("indexTest");
//        if (!Files.exists(folderPath)) {
//            Files.createDirectory(folderPath);
//        }
//
//        //以folderPath 为 index字典库
//        FSDirectory directory = FSDirectory.open(folderPath);
//
//        //创建 语法解析器 与 index写手配置
//        Analyzer analyzer = new StandardAnalyzer();
//        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
//        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
//        //创建 index写手
//        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
//
//
//        for (MMTeachingPlan teachingPlan: planList) {
//            //新建一条 index文档: 可以类比为DB中, 一个表中 一条数据
//            Document document = new Document();
//
//            document.add(new StringField("id", teachingPlan.getId(), Field.Store.YES));
//            document.add(new TextField("hardReading", teachingPlan.getHardReading()+".test", Field.Store.YES));
//            document.add(new TextField("teachGoalExplain", teachingPlan.getTeachGoalExplain(), Field.Store.YES));
//            document.add(new TextField("guidance", teachingPlan.getGuidance(), Field.Store.YES));
//            document.add(new TextField("process", teachingPlan.getProcess(), Field.Store.YES));
//
//            indexWriter.addDocument(document);
//
//            logger.info("已完成"+ ++indexNum);
//        }
//        indexWriter.close();
//
//        long endTime = new Date().getTime();
//
//        return endTime - startTime;
//    }
//
//    @Override
//    public void case1(String queryString) throws Exception {
//
//        //安检测试文件夹路径
//        final Path folderPath = Paths.get("indexTest");
//        if (!Files.exists(folderPath)) {
//            Files.createDirectory(folderPath);
//        }
//
//        //以folderPath 为 index字典库
//        Directory directory = FSDirectory.open(folderPath);
//
//        Analyzer analyzer = new StandardAnalyzer();
//
//        IndexReader indexReader = DirectoryReader.open(directory);
//
//        System.out.println("你要的:"+indexReader.getRefCount());
//
//        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
//
//        //单条件
//        QueryParser queryParser = new QueryParser("hardReading",analyzer);
//        Query query = queryParser.parse(queryString);
//
//        TopDocs topDocs = null;
//
//        if (after == null) {
//            topDocs = indexSearcher.search(query,10);
//        } else {
//            topDocs = indexSearcher.searchAfter(after, query,10);
//        }
//
//        long conut = topDocs.totalHits;
//        System.out.println("检索总条数："+conut);
//        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
//        for (ScoreDoc scoreDoc : scoreDocs) {
//            Document document = indexSearcher.doc(scoreDoc.doc);
//            logger.info("相关度:"+scoreDoc.score+"->index:"+scoreDoc.doc);
//        }
//
//        after = scoreDocs[topDocs.scoreDocs.length - 1];
//
//        after = new ScoreDoc(80, 0.11f, 0);
//
//        System.out.println("after:"+after);
//    }

//https://github.com/hankcs/hanlp-lucene-plugin

```