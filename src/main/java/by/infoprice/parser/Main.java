package by.infoprice.parser;

import by.infoprice.parser.web.WebServer;

public class Main {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");

        WebServer server = new WebServer();
        server.start();

        System.out.println("Откройте в браузере: http://localhost:8080");
        Thread.currentThread().join();
    }
}
