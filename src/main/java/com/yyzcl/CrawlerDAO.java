package com.yyzcl;

import java.sql.SQLException;

public interface CrawlerDAO {
    String getNextUrlThenSwitchStatus() throws SQLException;

    void saveUrlIntoDatabase(String url) throws SQLException;

    void insertNewsIntoDatabase(News news) throws SQLException;
}
