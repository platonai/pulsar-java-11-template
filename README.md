# README

A template project to use PulsarR as a library.

## Basic Usage

```java
public class BasicUsage {
    public static void main(String[] args) {
        // create a pulsar session
        PulsarSession session = PulsarContexts.createSession();
        // the main url we are playing with
        String url = "https://list.jd.com/list.html?cat=652,12345,12349";
        // load a page, fetch it from the web if it has expired or if it's being fetched for the first time
        WebPage page = session.load(url, "-expires 1d");
        // parse the page content into a Jsoup document
        FeaturedDocument document = session.parse(page, false);
        // do something with the document
        // ...

        // or, load and parse
        FeaturedDocument document2 = session.loadDocument(url, "-expires 1d");
        // do something with the document
        // ...

        // load all pages with links specified by -outLink
        List<WebPage> pages = session.loadOutPages(url, "-expires 1d -itemExpires 7d -outLink a[href~=item]");
        // load, parse and scrape fields
        List<Map<String, String>> fields = session.scrape(url, "-expires 1d", "li[data-sku]",
                Arrays.asList(".p-name em", ".p-price"));
        // load, parse and scrape named fields
        List<Map<String, String>> fields2 = session.scrape(url, "-i 1d", "li[data-sku]",
                Map.of("name", ".p-name em", "price", ".p-price"));
    }
}
```

## Continuous Crawler

```java
public class ContinuousCrawler {

    private static void onParse(WebPage page, FeaturedDocument document) {
        // do something wonderful with the document
        // System.out.println(document.title() + "\t|\t" + document.baseUri());

        // we can extract links in document and then scraping them
        List<Hyperlink> urls = document.selectHyperlinks("a[href~=/dp/]");
        PulsarContexts.create().submitAll(urls);
    }

    public static void main(String[] args) {
        List<Hyperlink> urls = LinkExtractors.fromResource("seeds10.txt")
                .stream()
                .map(seed -> new ParsableHyperlink(seed, ContinuousCrawler::onParse))
                .collect(Collectors.toList());
        PulsarContext context = PulsarContexts.create();
        // feel free to submit millions of urls here
        // ...
        context.submitAll(urls);

        context.await();
    }
}
```

## RPA

```java

public class RPACrawler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PulsarSession session;

    public final Map<String, String> fieldSelectors;

    public RPACrawler() {
        this(PulsarContexts.createSession());
    }

    public RPACrawler(PulsarSession session) {
        this.session = session;

        fieldSelectors = new HashMap<>(Map.of(
                "sku-name", ".sku-name",
                "news", ".news",
                "summary", ".summary"
        ));
    }

    public LoadOptions options(String args) {
        var options = session.options(args, null);
        var be = options.getEvent().getBrowseEvent();

        be.getOnWillComputeFeature().addLast(new WebPageJvmWebDriverEventHandler() {
            @Override
            public Object invoke(WebPage page, JvmWebDriver driver, Continuation<? super Object> continuation) {
                fieldSelectors.values().forEach(selector -> interact(selector, driver));
                return null;
            }
        });

        be.getOnFeatureComputed().addLast(new WebPageJvmWebDriverEventHandler() {
            @Override
            public Object invoke(WebPage page, JvmWebDriver driver, Continuation<? super Object> $completion) {
                logger.info("Feature computed");
                return null;
            }
        });

        return options;
    }

    private void interact(String selector, JvmWebDriver driver) {
        var delayedExecutor = CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS);
        var searchBoxSelector = ".form input[type=text]";

        driver.existsAsync(selector).thenAccept(exists -> {
            if (exists) {
                driver.clickAsync(selector)
                        .thenCompose(ignored -> driver.firstTextAsync(selector))
                        .thenAcceptAsync(text -> driver.typeAsync(searchBoxSelector, text.substring(1, 4)), delayedExecutor)
                        .thenRun(() -> { logger.info("{} clicked", selector); })
                        .join();
            }
        }).join();
    }

    public static void main(String[] argv) {
        var url = "https://item.jd.com/10023632209832.html";
        var args = "-refresh -parse";

        var session = PulsarContexts.createSession();
        var crawler = new RPACrawler(session);

        var fields = session.scrape(url, crawler.options(args), crawler.fieldSelectors);
        System.out.println(fields);
    }
}
```
