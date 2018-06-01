
package com.uma.mantenimiento.spider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * 
 * @author vikour
 */

public class Spider {
   
   private final WebDriver DRIVER;
   private final WebDriverWait DRIVER_WAIT;
   private final URL INIT_URL;
   private final int MAX_DEEP;
   private List<URL> visitedURLs;
   private List<URL> brokenURLs;
   
   private final int WAIT_UNITL_SECONDS = 10;

   
   public Spider(WebDriver driver, URL initURL, int max_deep) {
      this.INIT_URL = initURL;
      this.DRIVER = driver;
      this.DRIVER_WAIT = new WebDriverWait(driver, WAIT_UNITL_SECONDS);
      this.MAX_DEEP = max_deep;
      visitedURLs = new ArrayList<>();
      brokenURLs = new ArrayList<>();
   }
   
   public void iniciar() {
      DRIVER.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
      visitarUrl(INIT_URL, 1);
      DRIVER.quit();
   }
   
   private void visitarUrl(URL url, int profundidad) {
      List<URL> urlList;
      
      if (profundidad > MAX_DEEP)
         return;
      else if (visitedURLs.contains(url)) {
         printLine(profundidad - 1);
         System.out.println(url + " (Visitado)");
      }
      else if (existsConnectionNot404(url)) {
         printLine(profundidad - 1);
         System.out.println(url + " (✘)");
         brokenURLs.add(url);
         visitedURLs.add(url);
      }
      else {
         DRIVER.get(url.toString());
         printLine(profundidad - 1);         
         System.out.println(url + "(\u2714)");
         visitedURLs.add(url);
         urlList = buscarLinks(url);

         for (URL innerURL : urlList) {
            visitarUrl(innerURL, profundidad + 1);
         }
      }
      
   }

   private boolean existsConnectionNot404(URL url) {
       String response = getResponseHTTP(url);
      return response != null && getResponseHTTP(url).equals("Not Found");
   }
   
   /**
    * Busca todos los enlaces de la página web en la que se encuentra el driver
    * actualmente. Ingora aquellos que salgan del dominio del server o sean anclas.
    * 
    * Busca un máximo de MAX_LINKS enlaces en la página.
    * 
    * @param initURL Objecto URL que inidica la página inicial.
    * 
    * @return        Lista de enlaces de la página.
    */
   
   private List<URL> buscarLinks(URL initURL) {
      List<WebElement> aList = null;
      Set<URL> domainUrlsSet = new HashSet<>(); // Para evitar duplicados.
      URL url;
      String domain = initURL.getHost();
      int i = 0;
      URL aux;
      
      //aList = DRIVER_WAIT.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("a")));
      aList = DRIVER.findElements(By.tagName("a"));
      
       while (i < aList.size()) {
           try {
               url = new URL(aList.get(i).getAttribute("href"));
               if (url != null && url.getHost().equals(domain) && !url.toString().contains("#"))
                    domainUrlsSet.add(url);

           } catch (StaleElementReferenceException e) {
               System.out.println("Puto javascript");
           } catch (MalformedURLException ex) {
               
          }
           
         i++;
      }
      
      return new ArrayList<>(domainUrlsSet);
   }
   
   /**
    * Esta función devuevle la respuesta que hace el server al intentar conectarse
    * a la URL pasada como argumento.
    * 
    * @param url_string  URL al server que responserá.
    * @return El mensaje de respuesta del server.
    */

   private String getResponseHTTP(URL url) {
      HttpURLConnection connection = null;
      String response = null;
      
      try {
         connection = (HttpURLConnection) url.openConnection();
         response = connection.getResponseMessage();
      } catch (MalformedURLException ex) {
         //System.err.println("MAL FORMADA " + url.toString());
      } catch (IOException ex) {
         //System.err.println("EXCEPTION : " + url.toString());
      }
      finally {
         
         if (connection != null)
            connection.disconnect();
         
      }
      
      return response;
   }

   private void printLine(int n) {
      
      for (int i = 0 ; i < n ; i++) {
         System.out.print("   ");
      }
      
      if (n != 0)
         System.out.print("|-> ");
      
   }
   
   public URL [] getLinkVisited() {
      return (URL []) visitedURLs.toArray(new URL [visitedURLs.size()]);
   }
   
   public URL [] getBrokenLinks() {
      return (URL []) brokenURLs.toArray(new URL [brokenURLs.size()]);
   }
   
   
}
