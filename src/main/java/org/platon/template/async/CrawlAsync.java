package org.platon.template.async;

import ai.platon.pulsar.common.LinkExtractors;
import ai.platon.pulsar.context.PulsarContexts;
import ai.platon.pulsar.dom.FeaturedDocument;

import java.util.concurrent.CompletableFuture;

class CrawlAsync {

    public static void loadAll() {
        var session = PulsarContexts.createSession();
        LinkExtractors.fromResource("seeds10.txt").stream()
                .map(session::open).map(session::parse)
                .map(FeaturedDocument::guessTitle)
                .forEach(System.out::println);
    }

    public static void loadAllAsync2() {
        var session = PulsarContexts.createSession();

        var futures = LinkExtractors.fromResource("seeds10.txt").stream()
                .map(url -> url + " -i 1d")
                .map(session::loadAsync)
                .map(f -> f.thenApply(session::parse))
                .map(f -> f.thenApply(FeaturedDocument::guessTitle))
                .map(f -> f.thenAccept(System.out::println))
                .toArray(CompletableFuture<?>[]::new);

        CompletableFuture.allOf(futures).join();
    }

    public static void loadAllAsync3() {
        var session = PulsarContexts.createSession();

        var futures = session.loadAllAsync(LinkExtractors.fromResource("seeds10.txt")).stream()
                .map(f -> f.thenApply(session::parse))
                .map(f -> f.thenApply(FeaturedDocument::guessTitle))
                .map(f -> f.thenAccept(System.out::println))
                .toArray(CompletableFuture<?>[]::new);

        CompletableFuture.allOf(futures).join();
    }

    public static void loadAllAsync4() {
        var session = PulsarContexts.createSession();

        var futures = session.loadAllAsync(LinkExtractors.fromResource("seeds10.txt")).stream()
                .map(f -> f.thenApply(session::parse).thenApply(FeaturedDocument::guessTitle).thenAccept(System.out::println))
                .toArray(CompletableFuture<?>[]::new);

        CompletableFuture.allOf(futures).join();
    }

    public static void main(String[] args) {
        loadAll();
        loadAllAsync2();
        loadAllAsync3();
        loadAllAsync4();
    }
}
