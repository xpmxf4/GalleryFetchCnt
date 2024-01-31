package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DCPopularPostsTracker {
    private static final String JSON_FILE = "dc_popular_posts2.json";
    private static Map<String, JSONObject> postsMap = new HashMap<>();
    private static final String URL = "http://gall.dcinside.com/board/lists/?id=football_new8";

    private static final long THIRTY_MINUTES = 1000 * 60 * 30; // 30분

    public static void main(String[] args) {
        Timer timer = new Timer();
        TimerTask halfHourlyTask = new TimerTask() {
            @Override
            public void run() {
                trackPopularPosts();
            }
        };

        // 첫 실행을 30분 후로 설정하고, 그 후 30분마다 반복 실행
        timer.scheduleAtFixedRate(halfHourlyTask, THIRTY_MINUTES, THIRTY_MINUTES);
    }

    public static void trackPopularPosts() {
        try {
            Elements posts = fetchPopularPosts();
            String currentDate = getCurrentDate();

            for (Element post : posts) {
                processPost(post, currentDate);
            }

            writeJSONToFile();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static Elements fetchPopularPosts() throws IOException {
        Document postDocs = Jsoup.connect(URL).get();
        return postDocs.select(".concept_txtlist li a");
    }

    private static String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(new Date());
    }

    private static void processPost(Element post, String currentDate) throws IOException {
        String postTitle = post.text();
        String postLink = post.attr("href").replace("https", "http");
        int count = fetchPostViewCount(postLink);

        JSONObject postJSON = postsMap.getOrDefault(postLink, new JSONObject());
        postJSON.put("title", postTitle);
        postJSON.put("link", postLink);

        // 조회수 기록을 배열로 관리
        JSONArray viewCounts = postJSON.optJSONArray("total_views");
        if (viewCounts == null) {
            viewCounts = new JSONArray();
        }
        viewCounts.put(new JSONObject().put("count", count).put("time", currentDate));
        postJSON.put("total_views", viewCounts);

        postsMap.put(postLink, postJSON);
    }

    private static int fetchPostViewCount(String postLink) throws IOException {
        Document postDoc = Jsoup.connect(postLink).get();
        Element view = postDoc.selectFirst(".view_content_wrap .gall_count");
        try {
            return Integer.parseInt(view.text().replaceAll("[^\\d]", ""));
        } catch (Exception e) { // 도중에 게시물을 지워버리는 경우가 존재함.
            return 0;
        }
    }

    private static void writeJSONToFile() {
        try (FileWriter file = new FileWriter(JSON_FILE)) {
            JSONArray postsList = new JSONArray();
            postsMap.values().forEach(postsList::put);
            file.write(postsList.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
