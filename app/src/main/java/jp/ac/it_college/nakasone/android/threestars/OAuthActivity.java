package jp.ac.it_college.nakasone.android.threestars;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;


public class OAuthActivity extends Activity {
    private OAuthAuthorization mOAuth = null;
    private RequestToken mReq = null;
    public static final String CALLBACK_URL
            = "callback://OAuthActivity.threestars.android.nakasone.it-college.ac.jp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);

        new OAuthTask().execute();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        new TokenTask().execute(intent);
    }

    public class OAuthTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Configuration conf = ConfigurationContext.getInstance();

            mOAuth = new OAuthAuthorization(conf);
            mOAuth.setOAuthConsumer(getString(R.string.consumer_key),
                    getString(R.string.consumer_secret));
            try {
                mReq = mOAuth.getOAuthRequestToken(CALLBACK_URL);
            } catch (TwitterException e) {
                Log.e("twitter4j", e.getMessage(), e);
                return null;
            }

            String uri;
            uri = mReq.getAuthorizationURL();
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
            return null;
        }
    }

    public class TokenTask extends AsyncTask<Intent, Void, AccessToken> {

        @Override
        protected AccessToken doInBackground(Intent... intents) {
            Intent intent = intents[0];
            Uri uri = intent.getData();
            AccessToken token = null;
            if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {
                String verifier = uri.getQueryParameter("oauth_verifier");
                try {
                    token = mOAuth.getOAuthAccessToken(mReq, verifier);
                } catch (Exception e) {
                    Log.e("TokenTask", e.getMessage(), e);
                }
            }
            return token;
        }

        @Override
        protected void onPostExecute(AccessToken accessToken) {
            if (accessToken != null) {
                SharedPreferences preferences
                        = getSharedPreferences(TweetActivity.PREF_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(TweetActivity.TOKEN, accessToken.getToken());
                editor.putString(TweetActivity.TOKEN_SECRET, accessToken.getTokenSecret());
                editor.commit();

                Intent sIntent = new Intent(getApplicationContext(), TweetActivity.class);
                startActivity(sIntent);
            }
            finish();
        }
    }
}
