package org.platon.template.sites.amazon;

import ai.platon.pulsar.context.PulsarContexts;

import java.util.Map;

class AmazonCrawler {

    public static void main(String[] argv) {
        PulsarContexts.createSession().scrapeOutPages(
                "https://www.amazon.com/Best-Sellers/zgbs",
                "-outLink a[href~=/dp/]",
                Map.of("title", "#title", "ratings", "#acrCustomerReviewText")
        );
    }
}
