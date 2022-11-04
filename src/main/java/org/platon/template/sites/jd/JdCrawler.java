package org.platon.template.sites.jd;

import ai.platon.pulsar.context.PulsarContexts;

class JdCrawler {

    public static void main(String[] argv) {
        var portalUrl = "https://list.jd.com/list.html?cat=652,12345,12349";
        var args = "-i 1s -ii 5s -ol a[href~=item] -ignoreFailure";
        var session = PulsarContexts.createSession();
        session.loadOutPages(portalUrl, args);
    }
}
