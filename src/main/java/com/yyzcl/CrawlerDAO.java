package com.yyzcl;

import java.sql.SQLException;

public interface CrawlerDAO {
    String getNextUrlThenSwitchStatus() throws SQLException;

    void saveUrlIntoDatabase(String url) throws SQLException;

    void insertNewsIntoDatabase(String url, String title, String content) throws SQLException;
}
