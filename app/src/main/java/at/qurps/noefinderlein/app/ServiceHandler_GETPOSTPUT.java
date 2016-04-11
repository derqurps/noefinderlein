package at.qurps.noefinderlein.app;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class ServiceHandler_GETPOSTPUT {

    static String response = null;
    public final static int GET = 1;
    public final static int POST = 2;
    public final static int PUT = 3;

    public ServiceHandler_GETPOSTPUT() {

    }

    /**
     * Making service call
     * @url - url to make request
     * @method - http request method
     * */
    public String makeServiceCall(String url, int method) {
        return this.makeServiceCall(url, method, null);
    }

    /**
     * Making service call
     * @url - url to make request
     * @method - http request method
     * @params - http request params
     * */
    public String makeServiceCall(String url, int method,
                                  List<NameValuePair> params) {
        return makeServiceCall(url,method,params,"");
    }
    public String makeServiceCall(String url, int method,
                                  List<NameValuePair> params, String putpost) {
        try {
            // http client
            response = null;

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;

            // Checking http request method type
            if (method == POST) {
                HttpPost httpPost = new HttpPost(url);
                // adding post params
                if (params != null) {
                    httpPost.setEntity(new UrlEncodedFormEntity(params));
                }

                httpResponse = httpClient.execute(httpPost);
                httpEntity = httpResponse.getEntity();
                response = EntityUtils.toString(httpEntity);

            } else if (method == GET) {

                // appending params to url
                if (params != null) {
                    String paramString = URLEncodedUtils
                            .format(params, "utf-8");
                    url += "?" + paramString;
                }
                URL urlConn = new URL(url);
                HttpURLConnection urlConnection;
                if(url.startsWith("https:")){
                    urlConnection = (HttpsURLConnection) urlConn.openConnection();
                }else{
                    urlConnection = (HttpURLConnection) urlConn.openConnection();
                }
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);

                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                response = total.toString();
                r.close();

                //HttpGet httpGet = new HttpGet(url);

                //httpResponse = httpClient.execute(httpGet);

            } else if (method == PUT) {
                URL urlConn = null;
                try {
                    urlConn = new URL(url);
                }catch (MalformedURLException exception) {
                    exception.printStackTrace();
                }
                HttpURLConnection httpCon = null;
                DataOutputStream dataOutputStream = null;
                try {
                    if (url.startsWith("https:")) {
                        httpCon = (HttpsURLConnection) urlConn.openConnection();
                    } else {
                        httpCon = (HttpURLConnection) urlConn.openConnection();
                    }
                    httpCon.setRequestMethod("PUT");
                    httpCon.setDoInput(true);
                    httpCon.setDoOutput(true);
                    httpCon.setRequestProperty("Content-Type", "application/json");
                    httpCon.connect();

                    dataOutputStream = new DataOutputStream(httpCon.getOutputStream());
                    byte[] data=putpost.getBytes("UTF-8");
                    dataOutputStream.write(data);

                }catch (IOException exception) {
                    exception.printStackTrace();
                }  finally {
                    if (dataOutputStream != null) {
                        try {
                            dataOutputStream.flush();
                            dataOutputStream.close();
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }finally {
                            BufferedReader br = null;
                            if (200 <= httpCon.getResponseCode() && httpCon.getResponseCode() <= 299) {
                                br = new BufferedReader(new InputStreamReader((httpCon.getInputStream())));
                            } else {
                                br = new BufferedReader(new InputStreamReader((httpCon.getErrorStream())));
                            }
                            StringBuilder sb = new StringBuilder();
                            String output;
                            while ((output = br.readLine()) != null) {
                                sb.append(output);
                            }
                            response = sb.toString();
                            br.close();
                        }
                    }
                    if (httpCon != null) {
                        httpCon.disconnect();
                    }
                }

            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;

    }
}
