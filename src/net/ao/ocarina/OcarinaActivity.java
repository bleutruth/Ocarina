package net.ao.ocarina;

import java.util.ArrayList;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
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
		System.out.println("token: " + token);
		final String tokenSecret = pref.getString("tokenSecret", "");
		System.out.println("tokenSecret: " + tokenSecret);

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
			System.out.println("catch OAuthCommunicationException");
		}
		catch(OAuthExpectationFailedException e){
			System.out.println("catch OAuthExpectationFailedException");
		}
		catch(Exception e){
			System.out.println("catch Exception");
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
					System.out.println("catch OAuthExpectationFailedException");
				}
				catch(Exception e){
					System.out.println("catch Exception");
				}

				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();

				final String token = consumer.getToken();
				System.out.println("token: " + token);
				editor.putString("token", token);

				final String tokenSecret = consumer.getTokenSecret();
				System.out.println("tokenSecret: " + tokenSecret);
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
			System.out.println("catch OAuthMessageSignerException");
		}
		catch(OAuthExpectationFailedException e){
			System.out.println("catch OAuthExpectationFailedException");
		}
		catch(OAuthCommunicationException e){
			System.out.println("catch OAuthCommunicationException");
		}
	}

	static final String CALLBACKURL = "myapp://ocarinamainactivity";

	CommonsHttpOAuthConsumer consumer;
	DefaultOAuthProvider provider;

	Handler handler = new Handler();
}
