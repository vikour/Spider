package com.uma.mantenimiento.spider;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.net.MalformedURLException;
import java.net.URL;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;


public class SpiderDemo {
   
   // Configuración del driver.
   private static final String URL_CHROME_DRIVER = "C:\\Users\\Vikour\\Downloads\\chromedriver.exe";
   private static final String CHROME_DRIVER_NAME = "webdriver.chrome.driver";
   
   // Configuración de la App
   private static final String INIT_URL = "http://cau.sci.uma.es/noticias/noticia.php?id=38";
   
   public static void main(String[] args) throws MalformedURLException {
      Spider spider;
      WebDriver chromeDriver;
      
      // Iniciación del driver.
      System.setProperty(CHROME_DRIVER_NAME, URL_CHROME_DRIVER);
      chromeDriver = new ChromeDriver();
      
      spider = new Spider(chromeDriver, new URL(INIT_URL), 10);
      spider.iniciar();
      
      System.out.println("\nFIN DE BÚSQUEDA");
      URL [] visitedLinks = spider.getLinkVisited();
      URL [] brokenLinks = spider.getBrokenLinks();
      URL [] loginLinks = spider.getBrokenLogin();
      System.out.println("Se han encontrado " + brokenLinks.length + " links rotos de " + visitedLinks.length + " visitados.");
      
      if (brokenLinks.length > 0) {
         System.out.println("Los links rotos son : ");
         for (URL broken : brokenLinks)
            System.out.println(" - " + broken);
      }
      
      if(loginLinks.length>0){
          System.out.println("Los login rotos son : ");
          for(URL log : loginLinks)
              System.out.println(" - " + log);
      }
      
   }
   
}
