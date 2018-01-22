package com.uvt.miniprojet.providers.twi;

/**
 *  This class is used to create an adapter of the tweets, and fill the listview
 */

import java.util.ArrayList;

import com.uvt.miniprojet.Config;
import com.uvt.miniprojet.HolderActivity;
import com.uvt.miniprojet.R;
import com.uvt.miniprojet.util.Helper;
import com.uvt.miniprojet.util.MediaActivity;
import com.uvt.miniprojet.util.WebHelper;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class TweetAdapter  extends ArrayAdapter<Tweet> {

	private Context context;
	private ArrayList<Tweet> tweets;

	public TweetAdapter(Context context, int viewResourceId, ArrayList<Tweet> tweets) {
		super(context, viewResourceId, tweets);
		this.context = context;
		this.tweets = tweets;
	}
	
	@Override
	public View getView(int posicao, View view, ViewGroup parent){
		if (view == null) {
			view = LayoutInflater.from(context).inflate(R.layout.fragment_tweets_row, parent, false);
		}

		final Tweet tweet = tweets.get(posicao);
		
		if (tweet != null) {

			TextView name = (TextView) view.findViewById(R.id.name);
			TextView username = (TextView) view.findViewById(R.id.username);
			ImageView imagem = (ImageView) view.findViewById(R.id.profile_image);
			ImageView photo = (ImageView) view.findViewById(R.id.photo);
			TextView message = (TextView) view.findViewById(R.id.message);
			TextView retweetCount = (TextView) view.findViewById(R.id.retweet_count);
			TextView date = (TextView) view.findViewById(R.id.date);

			name.setText(tweet.getname());
			username.setText("@" + tweet.getusername());
			date.setText(tweet.getData());
			message.setText(Html.fromHtml(tweet.getmessage()));
		    message.setTextSize(TypedValue.COMPLEX_UNIT_SP, WebHelper.getTextViewFontSize(context));
			retweetCount.setText(Helper.formatValue(tweet.getRetweetCount()));

			Picasso.with(context).load(tweet.geturlProfileImage()).into(imagem);
			
			if (tweet.getImageUrl() != null){
				photo.setVisibility(View.VISIBLE);

				Picasso.with(context).load(tweet.getImageUrl()).placeholder(R.drawable.placeholder).into(photo);

				photo.setOnClickListener(new View.OnClickListener() {
	                public void onClick(View arg0) {

		                Intent commentIntent = new Intent(context, MediaActivity.class);
		                commentIntent.putExtra(MediaActivity.TYPE, MediaActivity.TYPE_IMG);
		                commentIntent.putExtra(MediaActivity.URL, tweet.getImageUrl());
		                context.startActivity(commentIntent);
	                }
	            });
			} else {
				photo.setImageDrawable(null);
				photo.setVisibility(View.GONE);
			}
			
			view.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

        			Intent sendIntent = new Intent();
        			sendIntent.setAction(Intent.ACTION_SEND);
        			String link = ("http://twitter.com/" + tweet.getusername() + "/status/" + tweet.getTweetId());
        			// this is the text that will be shared
        			sendIntent.putExtra(Intent.EXTRA_TEXT, link);
        			sendIntent.putExtra(Intent.EXTRA_SUBJECT, tweet.getusername()
        					+ context.getResources().getString('0'));
        			
        			sendIntent.setType("text/plain");
        			context.startActivity(Intent.createChooser(sendIntent, context.getResources()
        					.getString(R.string.share_header)));
                }
            });
			
			view.findViewById(R.id.open).setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                	String link = ("http://twitter.com/" + tweet.getusername() + "/status/" + tweet.getTweetId());
					HolderActivity.startWebViewActivity(context, link, Config.OPEN_EXPLICIT_EXTERNAL, false, null);

				}
            });
			
			
		}

		return view;
	}
}

