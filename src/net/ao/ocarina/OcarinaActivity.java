package net.ao.ocarina;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Xml;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import static java.lang.System.out;

public class OcarinaActivity extends Activity {

	public

	@Override
	void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		consumer = new CommonsHttpOAuthConsumer(TwitterData.CONSUMER_KEY, TwitterData.CONSUMER_SECRET);
		provider = new DefaultOAuthProvider(TwitterData.URL_REQUEST, TwitterData.URL_ACCESS, TwitterData.URL_AUTHORIZATION);

		SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
		final String token = pref.getString("token", "");
		out.println("token: " + token);
		final String tokenSecret = pref.getString("tokenSecret", "");
		out.println("tokenSecret: " + tokenSecret);

		//	îFèÿçœÇ›ÅH
		if(token != "" && tokenSecret != ""){
			consumer.setTokenWithSecret(token, tokenSecret);
			updateTimeline();
			return;
		}

		try{
//			final String url = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
			final String url = provider.retrieveRequestToken(consumer, CALLBACKURL);

			handler.post(
				new Runnable(){ public void run(){
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				}}
			);
		}
		catch(OAuthCommunicationException e){
			out.println("catch OAuthCommunicationException");
		}
		catch(OAuthExpectationFailedException e){
			out.println("catch OAuthExpectationFailedException");
		}
		catch(Exception e){
			out.println("catch Exception");
		}
	}

	protected

	@Override
	void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		final Uri uri = intent.getData();

		if((uri == null) || !uri.toString().startsWith(CALLBACKURL)){
			return;
		}

		Thread thread = new Thread(
			new Runnable(){ public void run(){
				try{
					final String verifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);
					provider.retrieveAccessToken(consumer, verifier);
				}
				catch(OAuthExpectationFailedException e){
					out.println("catch OAuthExpectationFailedException");
				}
				catch(Exception e){
					out.println("catch Exception");
				}

				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();

				final String token = consumer.getToken();
				out.println("token: " + token);
				editor.putString("token", token);

				final String tokenSecret = consumer.getTokenSecret();
				out.println("tokenSecret: " + tokenSecret);
				editor.putString("tokenSecret", tokenSecret);

				editor.commit();
			}}
		);
		thread.start();
	}

	private

	void updateTimeline()
	{
		final ArrayList<Tweet> tlarray = new ArrayList<Tweet>();

		DefaultHttpClient http = new DefaultHttpClient();
		HttpGet http_get = new HttpGet("http://twitter.com/statuses/friends_timeline.xml");

		try{
			consumer.sign(http_get);
		}
		catch(OAuthMessageSignerException e){
			out.println("catch OAuthMessageSignerException");
		}
		catch(OAuthExpectationFailedException e){
			out.println("catch OAuthExpectationFailedException");
		}
		catch(OAuthCommunicationException e){
			out.println("catch OAuthCommunicationException");
		}

		HttpResponse execute = null;
		try{
			execute = http.execute(http_get);
		}
		catch(ClientProtocolException e){
			out.println("catch ClientProtocolException");
		}
		catch(IOException e){
			out.println("catch IOException");
		}

		InputStream in = null;
		try{
			in = execute.getEntity().getContent();
		}
		catch(IOException e){
			out.println("catch IOException");
		}

		XmlPullParser parser = Xml.newPullParser();
		try{
			parser.setInput(new InputStreamReader(in));
		}
		catch(XmlPullParserException e){
			out.println("catch XmlPullParserException");
		}

		int type = 0;
		try{
			type = parser.getEventType();
		}
		catch(XmlPullParserException e){
			out.println("catch XmlPullParserException");
		}

		while(type != XmlPullParser.END_DOCUMENT){

			if(type == XmlPullParser.START_DOCUMENT){
				out.println("Start document");
			}
			else if(type == XmlPullParser.START_TAG){
				out.println("Start tag: " + parser.getName());
			}
			else if(type == XmlPullParser.END_TAG){
				out.println("End tag: " + parser.getName());
			}
			else if(type == XmlPullParser.TEXT){
				out.println("Text: " + parser.getText());
			}

			try{
				type = parser.next();
			}
			catch(XmlPullParserException e){
				out.println("catch XmlPullParserException");
			}
			catch(IOException e){
				out.println("catch IOException");
			}
		}
		out.println("End document");

		try{
			in.close();
		}
		catch(IOException e){
			out.println("catch IOException");
		}
	}

	static final String CALLBACKURL = "myapp://ocarinamainactivity";

	CommonsHttpOAuthConsumer consumer;
	DefaultOAuthProvider provider;

	Handler handler = new Handler();
}
