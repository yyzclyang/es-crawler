package com.yyzcl;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockDataGenerator {
    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mockData(sqlSessionFactory, 12_000);
    }

    public static void mockData(SqlSessionFactory sqlSessionFactory, int targetCount) {

        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<News> currentNews = session.selectList("com.yyzcl.MockMapper.selectNews");

            int count = targetCount - currentNews.size();
            Random random = new Random();
            try {
                while (count-- > 0) {
                    int index = random.nextInt(currentNews.size());
                    News newsToBeInsert = new News(currentNews.get(index));

                    Instant time = newsToBeInsert.getCreatedAt();
                    Instant newTime = time.minusSeconds(random.nextInt(3600 * 24 * 365));
                    newsToBeInsert.setCreatedAt(newTime);
                    newsToBeInsert.setUpdatedAt(newTime);

                    session.insert("com.yyzcl.MockMapper.insertNews", newsToBeInsert);

                    System.out.println("LEFT: " + count);

                    if (count % 2000 == 0) {
                        session.flushStatements();
                    }
                }
                session.commit();
            } catch (Exception e) {
                session.rollback();
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

    }
}
