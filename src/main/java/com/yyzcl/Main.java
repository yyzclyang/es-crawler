package com.yyzcl;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        LinkedList<String> linkPool = new LinkedList<>();
        Set<String> processedLinks = new HashSet<>();

        linkPool.add("https://news.sina.cn/");

        while (!linkPool.isEmpty()) {
            String link = linkPool.poll();

            if (processedLinks.contains(link)) {
                continue;
            }

            if (isValidLink(link)) {
                processedLinks.add(link);

                Document htmlDom = getHtmlDomByLink(link);

                addHtmlAnchorHrefToLikPool(linkPool, htmlDom);

                storeIntoDatabaseIfItIsNewsPage(htmlDom);
            }
        }
    }

    private static void addHtmlAnchorHrefToLikPool(LinkedList<String> linkPool, Document htmlDom) {
        htmlDom.select("a").stream()
                .map(aTag -> aTag.attr("href"))
                .filter(Main::isValidLink)
                .forEach(linkPool::add);
    }

    private static void storeIntoDatabaseIfItIsNewsPage(Document htmlDom) {
        ArrayList<Element> articleTags = htmlDom.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                Elements h1Tags = articleTag.select("h1");
                if (!h1Tags.isEmpty()) {
                    for (Element h1Tag : h1Tags) {
                        System.out.println(h1Tag.text());
                    }
                }
            }
        }
    }

    private static Document getHtmlDomByLink(String link) throws IOException {
        if (link.startsWith("//")) {
            link = "https:" + link;
        }
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1 Edg/92.0.4515.159");

        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String htmlString = EntityUtils.toString(entity);
            return Jsoup.parse(htmlString);
        }
    }

    private static boolean isValidLink(String link) {
        return link.contains("news.sina.cn");
    }
}
