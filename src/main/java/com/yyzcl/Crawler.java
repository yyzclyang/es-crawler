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

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Crawler {
    private CrawlerDAO dao = new MyBatisCrawlerDAO();

    public Crawler() {
    }

    public void run() {
        try {
            String url;

            while ((url = dao.getNextUrlThenSwitchStatus()) != null) {
                System.out.println(url);
                if (JdbcCrawlerDAO.isValidUrl(url)) {
                    Document htmlDom = getHtmlDom(url);

                    getAnchorHrefAndInsertIntoDatabase(htmlDom);

                    storeIntoDatabaseIfItIsNewsPage(htmlDom, url);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void getAnchorHrefAndInsertIntoDatabase(Document htmlDom) {
        getAnchorHrefs(htmlDom).forEach(href -> {
            try {
                dao.saveUrlIntoDatabase(href);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private Stream<String> getAnchorHrefs(Document htmlDom) {
        return htmlDom.select("a").stream()
                .map(aTag -> aTag.attr("href"));
    }

    private void storeIntoDatabaseIfItIsNewsPage(Document htmlDom, String url) throws SQLException {
        ArrayList<Element> articleTags = htmlDom.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.selectFirst("h1").text();
                String content = articleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
                dao.insertNewsIntoDatabase(url, title, content);
                System.out.println(title);
            }
        }
    }

    private Document getHtmlDom(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1 Edg/92.0.4515.159");

        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String htmlString = EntityUtils.toString(entity);
            return Jsoup.parse(htmlString);
        }
    }
}
