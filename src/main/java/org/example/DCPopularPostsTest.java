package org.example;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DCPopularPostsTest {
    public static void main(String[] args) {
        Map<String, JSONObject> postsMap = trackPopularPosts();
        for (String key : postsMap.keySet()) {
            System.out.println(postsMap.get(key));
        }
    }

    public static Map<String, JSONObject> trackPopularPosts() {
        String url = "http://gall.dcinside.com/board/lists/?id=football_new8";
        Map<String, JSONObject> postsMap = new HashMap<>();
        try {
            Document postDocs = Jsoup.connect(url).get();
            Elements posts = postDocs.select(".concept_txtlist li a");

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDate = formatter.format(new Date());

            for (Element post : posts) {
                String postTitle = post.text();
                String postLink = post.attr("href").replace("https", "http"); // 'https'를 'http'로 변경
                Document postDoc = Jsoup.connect(postLink).get();
                Element view = postDoc.selectFirst(".view_content_wrap .gall_count");
                int count = Integer.parseInt(view.text().replaceAll("[^\\d]", "")); // 숫자만 추출

                JSONObject postJSON = new JSONObject();
                postJSON.put("title", postTitle);
                postJSON.put("link", postLink);
                postJSON.put("total_views", count);
                postJSON.put("last_updated", currentDate);

                postsMap.put(postLink, postJSON);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return postsMap;
    }
}
