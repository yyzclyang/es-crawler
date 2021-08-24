package com.yyzcl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JdbcCrawlerDAO implements CrawlerDAO {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "123456";
    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDAO() {
        try {
            File projectDir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
            String jdbcUrl = "jdbc:h2:file:" + new File(projectDir, "tmp/news-db").getAbsolutePath();
            connection = DriverManager.getConnection(jdbcUrl, USER_NAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNextUrlThenSwitchStatus() throws SQLException {
        String url = getNextUrl();
        if (url != null) {
            updateUrlStatusIntoDatabase(url);
        }
        return url;
    }

    public String getNextUrl() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select url from Links where status = 0 limit 1;"); ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    public void updateUrlStatusIntoDatabase(String url) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("update LINKS set status = 1, updated_at = now() where url = ?;")) {
            statement.setString(1, url);
            statement.executeUpdate();
        }
    }

    public boolean isExistInDatabase(String url) throws SQLException {
        return !getUrlsFromDatabase(url).isEmpty();
    }

    public void saveUrlIntoDatabase(String url) throws SQLException {
        if (!isValidUrl(url)) {
            return;
        }
        if (url.startsWith("//")) {
            url = "https:" + url;
        }
        if (url.toLowerCase().startsWith("javascript")) {
            return;
        }
        if (isExistInDatabase(url)) {
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement("insert into Links(url) values (?);")) {
            statement.setString(1, url);
            statement.executeUpdate();
        }
    }

    public List<String> getUrlsFromDatabase(String url) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select id, url, status from Links where url = ?;")) {
            statement.setString(1, url);
            return getUrlsFromDatabase(statement);
        }
    }

    public List<String> getUrlsFromDatabase(PreparedStatement statement) throws SQLException {
        return getLinksFromDatabase(statement)
                .stream()
                .map(link -> link.url)
                .collect(Collectors.toList());
    }

    public List<Link> getLinksFromDatabase(PreparedStatement statement) throws SQLException {
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

    public void insertNewsIntoDatabase(News news) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into News(url, title, content) values (?, ?, ?);")) {
            statement.setString(1, news.getUrl());
            statement.setString(2, news.getTitle());
            statement.setString(3, news.getContent());
            statement.executeUpdate();
        }
    }

    public static boolean isValidUrl(String url) {
        return url.contains("news.sina.cn") && !url.contains("passport.sina.cn");
    }
}
