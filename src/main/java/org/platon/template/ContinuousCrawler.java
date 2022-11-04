package org.platon.template;

import ai.platon.pulsar.common.LinkExtractors;
import ai.platon.pulsar.common.urls.Hyperlink;
import ai.platon.pulsar.context.PulsarContext;
import ai.platon.pulsar.context.PulsarContexts;
import ai.platon.pulsar.crawl.common.url.ParsableHyperlink;
import ai.platon.pulsar.dom.FeaturedDocument;
import ai.platon.pulsar.dom.select.QueriesKt;
import ai.platon.pulsar.persist.WebPage;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.stream.Collectors;

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
