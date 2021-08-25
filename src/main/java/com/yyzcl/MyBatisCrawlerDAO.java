package com.yyzcl;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class MyBatisCrawlerDAO implements CrawlerDAO {
    private SqlSessionFactory sqlSessionFactory;

    public MyBatisCrawlerDAO() {
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNextUrlThenSwitchStatus() throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String url = session.selectOne("com.yyzcl.CrawlerMapper.getNextUrl");
            if (url != null) {
                session.update("com.yyzcl.CrawlerMapper.switchLinkStatus", url);
            }
            return url;
        }
    }

    @Override
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
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.yyzcl.CrawlerMapper.insertLink", new Link(url));
        }
    }

    private boolean isExistInDatabase(String url) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = session.selectOne("com.yyzcl.CrawlerMapper.countUrl", url);
            return count != 0;
        }
    }

    @Override
    public void insertNewsIntoDatabase(String url, String title, String content) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.yyzcl.CrawlerMapper.insertNews", new News(url, title, content));
        }
    }

    public static boolean isValidUrl(String url) {
        return url.contains("news.sina.cn") && !url.contains("passport.sina.cn");
    }
}
