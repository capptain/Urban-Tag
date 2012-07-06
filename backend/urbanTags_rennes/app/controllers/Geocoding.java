package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import play.mvc.Controller;

import com.ning.http.util.UTF8UrlEncoder;

public class Geocoding extends Controller
{

  public static void geocode(String query)
  {
    String baseUrl = "http://nominatim.openstreetmap.org/search";
    try
    {
      URI uri = new URI(baseUrl + "?q=" + UTF8UrlEncoder.encode(query)
        + "&format=json&polygon=0&addressdetails=0");
      HttpGet get = new HttpGet();
      get.setURI(uri);
      HttpResponse httpResponse;

      /* Create a http client manager. */
      ThreadSafeClientConnManager httpClientManager = new ThreadSafeClientConnManager(
        SchemeRegistryFactory.createDefault());

      /* Create and initialize HTTP parameters. */
      HttpParams params = new BasicHttpParams();
      HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
      HttpClient httpClient = new DefaultHttpClient(httpClientManager, params);
      httpResponse = httpClient.execute(get);

      InputStream is = httpResponse.getEntity().getContent();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));

      String content = "";
      String line = reader.readLine();
      while (line != null)
      {
        content += line;
        line = reader.readLine();
      }

      renderJSON(content);
    }
    catch (URISyntaxException e)
    {
      e.printStackTrace();
      error();
    }
    catch (ClientProtocolException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      error();
    }
  }
}
