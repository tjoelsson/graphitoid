/*
 * Copyright 2012 Tomas Joelsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tnc.android.graphite.controllers;


import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import com.tnc.android.graphite.GraphiteApp;


public class SettingsController extends Controller
  implements OnSharedPreferenceChangeListener
{
  SSLSocketFactory defaultSSLFactory;
  HostnameVerifier defaultVerifier;
  SharedPreferences prefs;

  public static final int MESSAGE_UPDATE_HTTP_USER=1;
  public static final int MESSAGE_UPDATE_HTTP_PASS=2;
  public static final int MESSAGE_VIEW_READY=3;
  
  public SettingsController()
  {
    defaultSSLFactory=HttpsURLConnection.getDefaultSSLSocketFactory();
    defaultVerifier=HttpsURLConnection.getDefaultHostnameVerifier();
    prefs=PreferenceManager.getDefaultSharedPreferences(
      GraphiteApp.getContext());
    prefs.registerOnSharedPreferenceChangeListener(this);
  }

  public boolean handleMessage(int what, Object data)
  {
    switch(what)
    {
      case MESSAGE_VIEW_READY:
        setPrefs();
        break;
    }
    return false;
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
    String key)
  {
    setPrefs();
  }

  private void setPrefs()
  {
    boolean dontVerify=prefs.getBoolean("https_verify", false);
    if(dontVerify)
    {
      trustAllCertificates();
      HttpsURLConnection.setDefaultHostnameVerifier(DO_NOT_VERIFY);
    }
    else
    {
      HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLFactory);
      HttpsURLConnection.setDefaultHostnameVerifier(defaultVerifier);
    }
    
    String httpUsername = prefs.getString("http_user", "");
    String httpPassword = prefs.getString("http_pass", "");
    
    // TODO: Use a single message to trigger update. Read all preferences
    // from activity class instead of sending strings. Use UI thread.
    notifyOutboxHandlers(MESSAGE_UPDATE_HTTP_USER, 0, 0, httpUsername);
    notifyOutboxHandlers(MESSAGE_UPDATE_HTTP_PASS, 0, 0, httpPassword);
    
    if(!httpUsername.equals("") || !httpPassword.equals(""))
    {
      setHttpAuthDetails(httpUsername, httpPassword);
    }
  }

  private void setHttpAuthDetails(String user, String pass)
  {
    final String username = user;
    final String password = pass;
    Authenticator.setDefault(new Authenticator()
    {
      protected PasswordAuthentication getPasswordAuthentication()
      {
        return new PasswordAuthentication(username,
          password.toCharArray());
      } 
    });
  }
  
  //always dontVerify the host - dont check for certificate
  final HostnameVerifier DO_NOT_VERIFY=new HostnameVerifier()
  {
    public boolean verify(String hostname, SSLSession session)
    {
      return true;
    }
  };

  /**
   * Trust every server - dont check for any certificate
   */
  private void trustAllCertificates()
  {
    // Create a trust manager that does not validate certificate chains
    TrustManager[] trustAllCerts=new TrustManager[] {new X509TrustManager() {
      public java.security.cert.X509Certificate[] getAcceptedIssuers()
      {
        return new java.security.cert.X509Certificate[] {};
      }

      public void checkClientTrusted(X509Certificate[] chain,
        String authType) throws CertificateException
      {
      }

      public void checkServerTrusted(X509Certificate[] chain,
        String authType) throws CertificateException
      {
      }
    }};

    // Install the all-trusting trust manager
    try
    {
      SSLContext sc=SSLContext.getInstance("TLS");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection
        .setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
}
