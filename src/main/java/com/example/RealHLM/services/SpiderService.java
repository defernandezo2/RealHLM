package com.example.RealHLM.services;

import com.example.RealHLM.entities.WebPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.micrometer.common.util.StringUtils.isBlank;

@Service
public class SpiderService {

    @Autowired
    private SearchService searchService;

    public void indexWebPages() {
        List<WebPage> linksToIndex= searchService.getLinksToIndex();
        linksToIndex.stream().parallel().forEach(webPage -> {
            try{
                indexWebPage(webPage);
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
        });

    }

    private void indexWebPage(WebPage webPage) throws Exception {
        String url = webPage.getUrl();
        String content = getWebContent(url);
        if (isBlank(content))
        {
            return;
        }

        indexSaveWebPage(webPage, content);
        String domain = getDomain(url);
        saveLinks(domain,content);
    }

    private String getDomain(String url) {
        String[] aux = url.split("/");
        return aux[0] + "//" + aux[2];
    }

    private void saveLinks(String domain,String content) {
        List<String> links = getLinks(domain,content);
        links.stream()
                .filter(link -> !searchService.exist(link))
                .map(link -> new WebPage(link))
                .forEach(webPage -> searchService.save(webPage));

    }

    private void indexSaveWebPage(WebPage webPage, String content) {
        String title = getTitle(content);
        String description = getDescription(content);
        webPage.setDescription(description);
        webPage.setTitle(title);
        searchService.save(webPage);
    }

    public List<String> getLinks(String domain,String content) {
        List<String> results = new ArrayList<>();
        String[] splitHref = content.split("href=\"");
        List<String> arraySplit = Arrays.asList(splitHref);
        arraySplit.forEach(partA -> {
            String[] aux = partA.split("\"");
            results.add(aux[0]);
        });
        return cleanLinks(domain,results);
    }

    private List<String> cleanLinks(String domain, List<String> links) {
        String[] excludeExtensions = new String[]{"css", "js", "json", "jpg", "png", "woff2"};
        List<String> result = links.stream()
                .filter(link -> Arrays.stream(excludeExtensions).noneMatch(link::endsWith))
                .map(link -> link.startsWith("/") ? domain + link : link)
                .collect(Collectors.toList());
        Set<String> foo= new HashSet<String>(result);

        return new ArrayList<String>(foo);



    }

    public String getTitle(String content) {
        String[] aux = content.split("<title>");
        String[] aux2 = aux[1].split("</title>");
        return aux2[0];
    }

    public String getDescription(String content) {
        String[] aux = content.split("<meta name=\"description\" content=\"");
        String[] aux2 = aux[1].split("\">");
        return aux2[0];
    }

    private String getWebContent(String link) {
        try {
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String encoding = conn.getContentEncoding();

            InputStream inputStream = conn.getInputStream();
            Stream<String> lines = new BufferedReader(new InputStreamReader(inputStream)).lines();
            System.out.println("END");
            return lines.collect(Collectors.joining());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
