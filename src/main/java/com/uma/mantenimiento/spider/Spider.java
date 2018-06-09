
package com.uma.mantenimiento.spider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
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
   private final int TIME_OUT_SEC = 30;
   private SortedSet<URL> visitedURLs;
   private List<URL> brokenURLs;
   private List<URL> loginURLs;
   
   private final int WAIT_UNITL_SECONDS = 10;

   
   public Spider(WebDriver driver, URL initURL, int max_deep) {
      this.INIT_URL = initURL;
      this.DRIVER = driver;
      this.DRIVER_WAIT = new WebDriverWait(driver, WAIT_UNITL_SECONDS);
      this.MAX_DEEP = max_deep;
      visitedURLs = new TreeSet<>(new UrlStringComparator());
      brokenURLs = new ArrayList<>();
      loginURLs = new ArrayList<>();
   }
   
   public void iniciar() {
      DRIVER.manage().timeouts().pageLoadTimeout(TIME_OUT_SEC, TimeUnit.SECONDS);
      visitarUrl(INIT_URL, 1);
      DRIVER.quit();
   }
   
   private void visitarUrl(URL url, int profundidad) {
      List<URL> urlList;
      
      // Si hemos alcanzado la profundidad máxima, salimos.
      if (profundidad > MAX_DEEP)
         return;
      // Si la URL ya ha sido visitada, lo notificamos y salimos.
      else if (visitedURLs.contains(url)) {
         printLine(profundidad - 1);
         System.out.println(url + " (Visitado)");
      }
      // Si no hay conexión, indicamos que esta roto y anotamos la url como visitada
      // y como rota.
      else if (existsConnection404(url)) {
         printLine(profundidad - 1);
         System.out.println(url + " (!ROTO!)");
         brokenURLs.add(url);
         visitedURLs.add(url);
      }
      // Si no, es que hay conexión HTTP y no ha sido visitado la URL, así que 
      // procedemos a realziar búsqueda y prueba de logins.
      else {
     
           try {
               System.out.print("Trying get url " + url + " ... ");
               DRIVER.get(url.toString());
               System.out.println("OK!");
               printLine(profundidad - 1);
               System.out.print(DRIVER.getTitle() + ", URL = " + url);
               
               // Puede que el recurso no esté y lo indique la página.
               if (DRIVER.getTitle().toLowerCase().contains("error 404")) {
                   brokenURLs.add(url);
                   System.out.println(" (ROTO!)");
                   visitedURLs.add(url);
               }
               // Si no, esta se ha conseguido conexión
               else {
                   System.out.print(" (OK!");
                   visitedURLs.add(url);

                   if (DRIVER.getTitle().equals("iDUMA - Servicio de Identidad de la Universidad de Málaga")) {
                       comprobarLogin(url);
                   }
                   // No tiene sentido buscar más enlaces si estamos al nivel máximo de profundidad...
                   if (profundidad < MAX_DEEP) {
                       urlList = buscarLinks(url);
                       System.out.println(", " + urlList.size() + " links encontrados)");

                       for (URL innerURL : urlList) {
                           visitarUrl(innerURL, profundidad + 1);
                       }

                   } else // Completamos el parétesis de antes del IF.
                       System.out.println(")");
                   
               }

           } catch (TimeoutException ex) {
               // Timeout, no se ha podido obtener, pero había conexión HTTP.
               // Porque el método existsHTTP lo ha conseguido, pero el driver no.
               // Esto puede ser por Javascript. Que haya una venta modal o un popup.
               System.out.println("!TIME OUT! " + url);
           }
       }

   }

    private void comprobarLogin(URL url) {
        
        try {
            DRIVER.findElement(By.name("adAS_username")).sendKeys("foo@uma.es");
            DRIVER.findElement(By.name("adAS_password")).sendKeys("foopassword");
            DRIVER.findElement(By.name("adAS_submit")).click();
            if(!DRIVER.getTitle().equals("iDUMA - Servicio de Identidad de la Universidad de Málaga"))
                loginURLs.add(url);
        }
        catch (Exception ex) {
            // Error inesperado
        }
    }

   private boolean existsConnection404(URL url) {
       String response = getResponseHTTP(url);
       
      return (response == null) || (response != null && response.equals("Not Found"));
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
      String domain = buildDomainBase(initURL);
      
      int i = 0;
      aList = DRIVER.findElements(By.tagName("a"));
      
      String domainRegrex = "[\\S]*(?=\\.)[\\.]?" + domain.replace(".", "\\.") + "[\\S]*$";
      
       while (i < aList.size()) {
           try {
               url = new URL(aList.get(i).getAttribute("href"));
               if (url != null && url.toString().matches(domainRegrex) && !url.toString().contains("#"))
                    domainUrlsSet.add(url);

           } catch (StaleElementReferenceException e) {
               
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
          System.out.print("Trying HTTP connection " + url + " ... ");
         connection = (HttpURLConnection) url.openConnection();
         connection.setConnectTimeout(TIME_OUT_SEC*1000);
         response = connection.getResponseMessage();
          System.out.println("OK!");
      } catch (MalformedURLException ex) {
         System.err.println("MAL FORMADA " + ex.getMessage());
      } catch (IOException ex) {
         System.err.println("EXCEPTION : " + ex.getMessage());
         response = null;
      }
      catch (Exception ex) {
          // Error ineesperado.
          System.out.println("FAIL!");
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

    private String buildDomainBase(URL initURL) {
        String[] splitted = initURL.getHost().split("[.]");
        StringBuilder sb;
        
        
        if (splitted.length > 2) {
            sb = new StringBuilder();
            sb.append(splitted[splitted.length - 2])
                    .append(".").append(splitted[splitted.length - 1]);
        } else {
            sb = new StringBuilder(initURL.getHost());
        }
        
        return sb.toString();
    }
   
   public URL[] getBrokenLogin(){
       return (URL[]) loginURLs.toArray(new URL[loginURLs.size()]);
   }
   
   private class UrlStringComparator implements Comparator<URL> {

        @Override
        public int compare(URL o1, URL o2) {
            return o1.toString().compareTo(o2.toString());
        }
       
   }
}
