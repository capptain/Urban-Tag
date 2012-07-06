package reach;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import models.data.CampaignData;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import crypto.HMacSha1;

public class ReachWrapper
{
  private static String apiKey = "0d2ef4edb1174bd3829c66a73d455533";
  private static String appid = "urb789348";
  private static String kind = "data-push";

  public static long createDataPush(CampaignData data) throws Exception
  {
    HttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost(ReachUrls.CREATE);
    Gson gson = new GsonBuilder().create();

    try
    {
      String json = gson.toJson(data, CampaignData.class);
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
      nameValuePairs.add(new BasicNameValuePair("appid", appid));
      nameValuePairs.add(new BasicNameValuePair("kind", kind));
      nameValuePairs.add(new BasicNameValuePair("key", HMacSha1.asHexDigest(apiKey, json)));
      nameValuePairs.add(new BasicNameValuePair("data", json));
      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

      HttpResponse response = client.execute(post);

      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity()
        .getContent()));
      String content = "";
      String line = "";
      while ((line = rd.readLine()) != null)
      {
        System.out.println(line);
        content += line;
      }

      try
      {
        Long campaignId = Long.parseLong(content);
        if (ReachWrapper.activateDataPush(campaignId))
        {
          return campaignId;
        }
        else
        {
          throw new Exception("Campaign activation failed");
        }
      }
      catch (NumberFormatException e)
      {
        throw new Exception(content);
      }
    }
    catch (IOException e)
    {
      throw (e);
    }
  }

  public static boolean activateDataPush(long id) throws Exception
  {
    HttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost(ReachUrls.ACTIVATE);
    try
    {
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
      nameValuePairs.add(new BasicNameValuePair("appid", appid));
      nameValuePairs.add(new BasicNameValuePair("kind", kind));
      nameValuePairs.add(new BasicNameValuePair("key", HMacSha1.asHexDigest(apiKey, id + "")));
      nameValuePairs.add(new BasicNameValuePair("id", id + ""));
      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

      HttpResponse response = client.execute(post);

      return (response.getStatusLine().getStatusCode() == 200);
    }
    catch (IOException e)
    {
      throw (e);
    }
  }

  public static boolean suspendDataPush(long id) throws Exception
  {
    HttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost(ReachUrls.SUSPEND);
    try
    {
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
      nameValuePairs.add(new BasicNameValuePair("appid", appid));
      nameValuePairs.add(new BasicNameValuePair("kind", kind));
      nameValuePairs.add(new BasicNameValuePair("key", HMacSha1.asHexDigest(apiKey, id + "")));
      nameValuePairs.add(new BasicNameValuePair("id", id + ""));
      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

      HttpResponse response = client.execute(post);

      return (response.getStatusLine().getStatusCode() == 200);
    }
    catch (IOException e)
    {
      throw (e);
    }
  }

  public static boolean finishDataPush(long id) throws Exception
  {
    HttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost(ReachUrls.FINISH);
    try
    {
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
      nameValuePairs.add(new BasicNameValuePair("appid", appid));
      nameValuePairs.add(new BasicNameValuePair("kind", kind));
      nameValuePairs.add(new BasicNameValuePair("key", HMacSha1.asHexDigest(apiKey, id + "")));
      nameValuePairs.add(new BasicNameValuePair("id", id + ""));
      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

      HttpResponse response = client.execute(post);

      return (response.getStatusLine().getStatusCode() == 200);
    }
    catch (IOException e)
    {
      throw (e);
    }
  }

  public static boolean deleteDataPush(long id) throws Exception
  {
    HttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost(ReachUrls.DESTROY);
    try
    {
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
      nameValuePairs.add(new BasicNameValuePair("appid", appid));
      nameValuePairs.add(new BasicNameValuePair("kind", kind));
      nameValuePairs.add(new BasicNameValuePair("key", HMacSha1.asHexDigest(apiKey, id + "")));
      nameValuePairs.add(new BasicNameValuePair("id", id + ""));
      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

      HttpResponse response = client.execute(post);

      return (response.getStatusLine().getStatusCode() == 200);
    }
    catch (IOException e)
    {
      throw (e);
    }
  }
}
