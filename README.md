# hawkeye
hawkeye is a simple implementation of lucene framework.

该项目已视为我巩固java编程的学习项目, 已停止维护.

### 简介
**hawkeye** 是一个 **lucene 7.x.x** 框架的简单实现, 你可以仅直接实现**StandardRepository**的转译功能就可以在你的代码中使用ta了.

**注:** 暂时没有实现 **RAMRepository** 的功能, 请期待未来版本
- StandardRepository 仅需要少量代码就可以上手使用
- Repository 需要更多的自定义


### 如何上手 ???

### 1. 你的VO

```java
public class TestDocument {

    private String id;
    private String name;
    private String intro;

    public TestDocument() {}

    public TestDocument(String id, String name, String intro) {
        this.id = id;
        this.name = name;
        this.intro = intro;
    }

    @Override
    public String toString() {
        return "{\n\"id\":"+id+",\n\t\"name\":"+name+",\n\t"+"\"intro\":"+intro+"\n"+"}";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }
}
```

### 2. 继承StandardRepository, 实现转译

```java
public class TestRepository extends StandardRepository<TestDocument> {

    public static final String IDKey = "ID";
    public static final String NameKey = "Name";
    public static final String IntroKey = "Intro";

    public TestRepository(String directoryPath) throws IOException {
        super(directoryPath);
    }

    @Override
    protected void parseObject2InternalDocument(TestDocument testDocument, Document document) {
        document.add(new StringField(IDKey, testDocument.getId(), Field.Store.YES));
        document.add(new StringField(NameKey, testDocument.getName(), Field.Store.YES));
        document.add(new TextField(IntroKey, testDocument.getIntro(), Field.Store.YES));
    }

    @Override
    protected TestDocument parseInternalDocument2Object(Document document) {
        return new TestDocument(
                document.get(IDKey),
                document.get(NameKey),
                document.get(IntroKey)
        );
    }

    @Override
    protected TestDocument parseInternalDocument2ObjectWithHighlighter(Document document, Highlighter highlighter, Analyzer analyzer) {
        TestDocument testDocument = new TestDocument(
                document.get(IDKey),
                document.get(NameKey),
                document.get(IntroKey)
        );
        try {
            testDocument.setIntro(highlighter.getBestFragment(analyzer, IntroKey, document.get(IntroKey)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return testDocument;
    }
}
```

### 3. 存入与查询

```java
public class App {
    public static void main(String[] args) {
        
        //最简单的实现
        try {
            TestRepository testRepository = new TestRepository("/your/repository/path");
            testRepository.addDocument(new TestDocument("ID_0", "中国要如何变得强大起来?", "少年强则中国强, 教育是国之基础."));
            HittingSearchResult<TestDocument> result = testRepository.hittingSearch(new TermQuery(new Term(TestRepository.IDKey, "ID_0")), 10, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        
        
        
        //TODO: 详细一些的例子
        
        //1. 将一个文件夹路径作为索引位置
        try{
            TestRepository testRepository = new TestRepository("/your/repository/path");
        } catch (Exception e) {}
        
        
        
        //2.1 较少量写操作时
        TestDocument testDocument0 = new TestDocument("ID_0", "中国要如何变得强大起来?", "少年强则中国强, 教育是国之基础.");
        try {
            testRepository.addDocument(testDocument0);
        } catch (Exception e) {}
        
        //2.2 大量写操作时
        TestDocument testDocument1 = new TestDocument("ID_1", "中国要如何变得强大起来?", "少年强则中国强, 教育是国之基础.");
        TestDocument testDocument2 = new TestDocument("ID_2", "中国要如何变得强大起来?", "少年强则中国强, 教育是国之基础.");
        List<TestDocument> documentList = new ArrayList<>();
        documentList.add(testDocument1);
        documentList.add(testDocument2);
        
        try {
            //lucene官方表示内部写入效率极高, 所以内部只允许一个IndexWriter
            //即 class Repository 的对象方法 addDocument(s) 多线程调用是没有实际意义的
            //虽然我这里的实现是允许线程调用, 但内部是有锁的 synchronized(testRepository) 
            testRepository.addDocuments(documentList);
        } catch (Exception e) {}
        
        //3.1 创建一个query, 按照官方文档来写即可
        TermQuery termQuery = new TermQuery(new Term(TestRepository.IDKey, "ID_0"));
        //3.2 如果嫌麻烦, 可使用 QueryTemplateBuilder 创建一个复合的查询条件
        QueryTemplateBuilder queryTemplateBuilder = new QueryTemplateBuilder() {
            @Override
            protected List<Term> parseMustKey(String s) {
                return null; 
            }
            @Override
            protected List<Term> parseFilterKey(String s) {
                return null;
            }

            @Override
            protected List<Term> parseShouldListKey(String s) {
                List<Term> termList = new ArrayList<>();
                termList.add(new Term(TestRepository.IDKey, s));
                termList.add(new Term(TestRepository.NameKey, s));
                termList.add(new Term(TestRepository.IntroKey, s));
                return termList;
            }

            @Override
            protected List<Term> parseMustNotKey(String s) {
                return null;
            }
        };
        
        //4.1 分页式查询
        PagingQuery pagingQuery = new PagingQuery(queryTemplateBuilder.fuzzyQuery("意志"), /*pageIndex*/ 1, /*pageSize*/ 10, /*highlight)*/ true);
        PagingQueryResult<TestDocument> result = testRepository.pagingSearch(pagingQuery);
        //4.2 命中式查询
        //最高相关度, 前10条
        HittingSearchResult<TestDocument> result0 = testRepository.hittingSearch(termQuery, 10, null);
        //最高相关度, 上次查询内容之后的 10 条
        HittingSearchResult<TestDocument> result1 = testRepository.hittingSearch(termQuery, 10, result0.getAfter());
        //高亮查询
        HittingSearchResult<TestDocument> result3 = testRepository.hittingSearch(termQuery, 10, result0.getAfter(), true);
        
        
        
    }

}
```

**更多的使用, 请引用源码后, 在你的IDE中查看吧**


