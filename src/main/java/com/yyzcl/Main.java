package com.yyzcl;

public class Main {
    public static void main(String[] args) {
        CrawlerDAO dao = new MyBatisCrawlerDAO();

        for (int i = 0; i < 10; i++) {
            new Crawler(dao).start();
        }
    }
}
