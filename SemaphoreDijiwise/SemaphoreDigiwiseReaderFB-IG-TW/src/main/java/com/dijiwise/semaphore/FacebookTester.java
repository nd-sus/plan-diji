/*
 * @FacebookTester.java	Jul 10, 2015
 *
 * Copyright (c) 2015 Sphata Systems Pvt Ltd. All rights reserved.
 * Sphata Systems Pvt Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */
package com.dijiwise.semaphore;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.Post;
import com.restfb.types.User;

/**
 * @author Senthil Raja N
 * @since Jul 10, 2015
 */
public class FacebookTester {

	private String accessToken = "CAAKjwF5gBZBoBAJnSJZC8CbvUV6YjLSB94jYrKROK65r7E5MWzEgm3MZCZCfaLQOrqRbhs1lDZAxucgv5G3kbxZA11zze3DGnnTMXSmpb4TG7G27HIZCFIQMH7qtqygDzLonN1RQYtIDtGYkDRy8aQwMQx8GG0K2hBYRtl5qscE2N6lqX9KIPrq7tMfx2ePnCrHZA2q7orK5j90kpZAU96ZAzz";
//	private String accessToken = "CAAKjwF5gBZBoBAGbYXnBpuZCDS8bUoue6cDn808JzUJTlLr5AZC8PS5Md9LLkzAZAaIZBEslsSdueZAjHkn6fbwGgA1VxJiKDkuBN9VEZC5VHmh0AvG5yzNWyDxIrtRZBCZCqEwzyPZBqJoG8ZAz91HZBgEyQfEbWm9mv4PWEhDMgDTa5GZCHYRLhZBfvSECe0LDejQpv6EBchZCUoYiIcZAtboQ5KS2";
	private DefaultFacebookClient facebookClient;

	public FacebookTester() {
		facebookClient = new DefaultFacebookClient(accessToken, Version.VERSION_2_3);
	}

	public void init() {
		List<Post> posts = FilteredPosts();
		readFilteredPosts(posts);
//		List<Post> feedPosts = FilteredFeedPosts();
	}
	
	private void readFilteredPosts(List<Post> posts) {
		for(Post post : posts) {
			System.out.println(post.getMessage());
		}
	}

	private ArrayList<Post> FilteredPosts() {
		Date daysAgo = new Date(currentTimeMillis() - 1000L * 60L * 60L * 24L * 12	);

		Connection<Post> filteredFeed = facebookClient.fetchConnection("me/feed", Post.class, Parameter.with("since", daysAgo),
				Parameter.with("limit", 100), Parameter.with("since", daysAgo));
		out.println("***********beforehomefeed**********************************");

		return new ArrayList<Post>(filteredFeed.getData());
	}

	private ArrayList<Post> FilteredFeedPosts() {
		Date daysAgo = new Date(currentTimeMillis() - 1000L * 60L * 60L * 24L * 10);

		Connection<Post> filteredFeed = facebookClient.fetchConnection("me/feed", Post.class, Parameter.with("since", daysAgo),
				Parameter.with("limit", 100));
		out.println("***********beforehomefeed**********************************");

		return new ArrayList<Post>(filteredFeed.getData());
	}

	public static void main(String[] arg) {
		new FacebookTester().init();
	}
}
