/*
 * @TwitterTester.java	Jul 9, 2015
 *
 * Copyright (c) 2015 Sphata Systems Pvt Ltd. All rights reserved.
 * Sphata Systems Pvt Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */
package com.dijiwise.semaphore;

import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author 	Senthil Raja N
 * @since	Jul 9, 2015
 */
public class TwitterTester {

	private String OAuthConsumerKey = "arW0EywAp2gQn0xn2SNzpGEZ0";
	private String OAuthConsumerSecret = "OG9EcoIpPeltoLR4Ia6h01QWca73GGlySAC42E6s6fwRn8vCC5";
	
	//Sundar
//	private String OAuthAccessTokenValue = "3272870491-xGItj0Qo9eA9n38LpoBNKZSWwXtf5P2Nb8LgcGb";
//	private String OAuthAccessTokenSecret = "QARgY0TVOOuMWSACStNkG5ml5H4LGZj4PDdxus9oj6OfE";
			
	//Diana
	private String OAuthAccessTokenValue = "2855167433-WsyawxaPSXaSz2b5rOJFxMR9Bmudtl6p7YAb07x";
	private String OAuthAccessTokenSecret = "GZwzxbUQKZFeTRRWrJmBvaEIaEQsZX2rMhdhgPH18bY2w";
	
	private List<Status> data = null;
	private List<Status> userData = null;
	private List<Status> favorites = null;
	
	public TwitterTester() {
		
	}
	
	public void init() {
		try {
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true).setOAuthConsumerKey(OAuthConsumerKey)
					.setOAuthConsumerSecret(OAuthConsumerSecret).setOAuthAccessToken(OAuthAccessTokenValue)
					.setOAuthAccessTokenSecret(OAuthAccessTokenSecret);
			Twitter twitter = new TwitterFactory(cb.build()).getInstance();

			data = twitter.getHomeTimeline();
			
			//logger.info("\n\n data "+data.toString());
			userData = twitter.getUserTimeline();
			favorites = twitter.getFavorites();
			String screenName = twitter.getScreenName();
			readHomeTimeline();
			System.out.println();
			System.out.println("---------------------------------------");
			System.out.println();
			readUserTimeline();
			System.out.println();
			System.out.println("---------------------------------------");
			System.out.println();
			readFavoritesTimeline();
			System.out.println(screenName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void readHomeTimeline() {
		String check = "This is what will happen to Wall Street";
		for(Status homeStatus : data) {
			String txt = homeStatus.getText();
			System.out.println("HOME : "+homeStatus.getId()+" : "+txt);
			if(txt.contains(check)){
				System.out.println(homeStatus);
			}
		}
	}
	
	private void readUserTimeline() {
		String check = "This is what will happen to Wall Street";
		for(Status userStatus : userData) {
			String txt = userStatus.getText();
			System.out.println("USER : "+userStatus.getId()+" : "+txt);
			if(txt.contains(check)){
				System.out.println(userStatus);
			}
		}
	}
	
	private void readFavoritesTimeline() {
		String check = "This is what will happen to Wall Street";
		for(Status favoriteStatus : favorites) {
			String txt = favoriteStatus.getText();
			System.out.println(favoriteStatus.isFavorited()+" FAVO : "+favoriteStatus.getId()+" : "+txt);
			if(txt.contains(check)){
				System.out.println("-->> "+favoriteStatus);
			}
		}
	}
	
	public static void main(String[] arg) {
		new TwitterTester().init();
	}
}
