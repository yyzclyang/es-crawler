package com.yyzcl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "123456";

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        File projectDir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
        String jdbcUrl = "jdbc:h2:file:" + new File(projectDir, "tmp/news-db").getAbsolutePath();
        Connection connection = DriverManager.getConnection(jdbcUrl, USER_NAME, PASSWORD);


        while (true) {
            LinkedList<String> urlPool = new LinkedList<>(getUrlsFromDatabase(connection, 0));
            Set<String> processedUrls = new HashSet<>(getUrlsFromDatabase(connection, 1));

            if (urlPool.isEmpty()) {
                break;
            }

            String url = urlPool.poll();

            if (processedUrls.contains(url)) {
                continue;
            }

            if (isValidUrl(url)) {
                Document htmlDom = getHtmlDom(url);

                getAnchorHrefAndInsertIntoDatabase(connection, htmlDom);

                storeIntoDatabaseIfItIsNewsPage(htmlDom);

                updateUrlStatusIntoDatabase(connection, url, 1);
            }
        }
    }

    private static void saveUrlIntoDatabase(Connection connection, String url) throws SQLException {
        if (isExistInDatabase(connection, url)) {
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement("insert into Links(url) values (?);")) {
            statement.setString(1, url);
            statement.executeUpdate();
        }
    }

    private static void updateUrlStatusIntoDatabase(Connection connection, String url, int status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("update LINKS set status = ? where url = ?;")) {
            statement.setInt(1, status);
            statement.setString(2, url);
            statement.executeUpdate();
        }
    }

    private static boolean isExistInDatabase(Connection connection, String url) throws SQLException {
        return !getUrlsFromDatabase(connection, url).isEmpty();
    }

    private static List<String> getUrlsFromDatabase(Connection connection, String url) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select id, url, status from Links where url = ?;");) {
            statement.setString(1, url);
            return getUrlsFromDatabase(statement);
        }
    }

    private static List<String> getUrlsFromDatabase(Connection connection, int status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select id, url, status from Links where status = ?;");) {
            statement.setInt(1, status);
            return getUrlsFromDatabase(statement);
        }
    }

    private static List<String> getUrlsFromDatabase(PreparedStatement statement) throws SQLException {
        return getLinksFromDatabase(statement)
                .stream()
                .map(link -> link.url)
                .collect(Collectors.toList());
    }

    private static List<Link> getLinksFromDatabase(PreparedStatement statement) throws SQLException {
        List<Link> links = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                long id = resultSet.getInt(1);
                String url = resultSet.getString(2);
                int status = resultSet.getInt(3);
                links.add(new Link(id, url, status));
            }
        }

        return links;
    }


    private static void getAnchorHrefAndInsertIntoDatabase(Connection connection, Document htmlDom) {
        getAnchorHrefs(htmlDom).filter(Main::isValidUrl).forEach(href -> {
            try {
                saveUrlIntoDatabase(connection, href);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private static Stream<String> getAnchorHrefs(Document htmlDom) {
        return htmlDom.select("a").stream()
                .map(aTag -> aTag.attr("href"));
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

    private static Document getHtmlDom(String url) throws IOException {
        if (url.startsWith("//")) {
            url = "https:" + url;
        }
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1 Edg/92.0.4515.159");

        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String htmlString = EntityUtils.toString(entity);
            return Jsoup.parse(htmlString);
        }
    }

    private static boolean isValidUrl(String url) {
        return url.contains("news.sina.cn");
    }
}
