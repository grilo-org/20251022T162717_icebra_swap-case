package com.ceg.swap.service;

import com.ceg.swap.dto.ContributorDTO;
import com.ceg.swap.dto.IssueDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GitHubClient {

    private final RestTemplate rest = new RestTemplate();

    public List<IssueDTO> fetchIssues(String user, String repo) {
        List<IssueDTO> result = new ArrayList<>();
        int page = 1;

        while (true) {
            var uri = UriComponentsBuilder
                    .fromHttpUrl("https://api.github.com/repos/" + user + "/" + repo + "/issues")
                    .queryParam("state", "all")
                    .queryParam("per_page", 100)
                    .queryParam("page", page)
                    .build()
                    .toUri();

            ResponseEntity<List> resp =
                    rest.exchange(uri, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), List.class);

            var body = resp.getBody();
            if (body == null || body.isEmpty()) break;

            for (Object obj : body) {
                Map<String, Object> map = (Map<String, Object>) obj;

                String title = String.valueOf(map.getOrDefault("title", ""));
                Map<String, Object> userMap = (Map<String, Object>) map.get("user");
                String author = userMap != null
                        ? String.valueOf(userMap.getOrDefault("login", ""))
                        : "";

                Object labelsObj = map.get("labels");
                List<String> lbls = Collections.emptyList();

                if (labelsObj instanceof List<?>) {
                    lbls = ((List<?>) labelsObj).stream()
                            .filter(Map.class::isInstance)
                            .map(m -> {
                                Map<String, Object> labelMap = (Map<String, Object>) m;
                                Object name = labelMap.get("name");
                                return name != null ? name.toString() : "";
                            })
                            .collect(Collectors.toList());
                }

                result.add(IssueDTO.builder()
                        .title(title)
                        .author(author)
                        .labels(lbls)
                        .build());
            }

            if (body.size() < 100) break;
            page++;
        }
        return result;
    }

    public List<ContributorDTO> fetchContributors(String user, String repo) {
        List<ContributorDTO> result = new ArrayList<>();
        int page = 1;

        while (true) {
            var uri = UriComponentsBuilder
                    .fromHttpUrl("https://api.github.com/repos/" + user + "/" + repo + "/contributors")
                    .queryParam("per_page", 100)
                    .queryParam("page", page)
                    .build()
                    .toUri();

            ResponseEntity<List> resp =
                    rest.exchange(uri, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), List.class);

            var body = resp.getBody();
            if (body == null || body.isEmpty()) break;

            for (Object obj : body) {
                Map<String, Object> m = (Map<String, Object>) obj;
                String login = String.valueOf(m.getOrDefault("login", ""));
                Integer contributions = ((Number) m.getOrDefault("contributions", 0)).intValue();

                result.add(ContributorDTO.builder()
                        .name(login)
                        .user(login)
                        .qtdCommits(contributions)
                        .build());
            }

            if (body.size() < 100) break;
            page++;
        }
        return result;
    }
}
