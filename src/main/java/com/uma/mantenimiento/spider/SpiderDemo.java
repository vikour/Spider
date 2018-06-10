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
import org.openqa.selenium.firefox.FirefoxDriver;


public class SpiderDemo {
   
   // Configuración del driver.
   private static final String URL_CHROME_DRIVER = "C:\\Users\\Vikour\\Downloads\\chromedriver.exe";
   private static final String CHROME_DRIVER_NAME = "webdriver.chrome.driver";
   private static final String FIREFOX_DRIVER_NAME = "webdriver.gecko.driver";
   
   // Configuración de la App
   private static final String INIT_URL = "http://jabega.uma.es";
   
   public static void main(String[] args) throws MalformedURLException {
      Spider spider;
      WebDriver driver = null;
      String url;
      int profundidad;
      
      if (args.length < 4)
           printHelp();
      else {
          url = args[0];
          profundidad = Integer.parseInt(args[1]);
          
          if (args[2].equals("chrome")) {
              System.setProperty(CHROME_DRIVER_NAME, args[3]);
              driver = new ChromeDriver();
          }
          else if (args[2].equals("firefox")) {
              System.setProperty(FIREFOX_DRIVER_NAME, args[3]);
              driver = new FirefoxDriver();
          }
          if (driver != null) {
              spider = new Spider(driver, new URL(url), profundidad);
              spider.iniciar();
              printResult(spider);
          }
          else 
              printHelp();
          
      }
      
   }

    private static void printHelp() {
        System.out.println("java -jar Spider.jar <url> <profundidad> <driver> <path_to_webdriver>\n"+
                "\t driver : chrome || firefox");
    }

    private static void printResult(Spider spider) {
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
