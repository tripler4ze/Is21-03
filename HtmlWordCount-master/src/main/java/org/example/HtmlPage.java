package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum SortDirection {
	ASC, DESC
}

@SuppressWarnings("unused")
public class HtmlPage {

	private final String url;

	private final String splitRegexp;

	private final int bufferLineCapacity;

	private final int bufferPosition;

	private final Map<String, Integer> wordCountMap;

	public HtmlPage(String url, String splitRegexp) {

		this(url, splitRegexp, 1_000_000);
	}

	public HtmlPage(String url, String splitRegexp, int bufferLineCapacity) {

		if (url == null || url.isEmpty() || url.isBlank()) {
			throw new IllegalArgumentException("URL could be null or empty or null");
		}

		if (splitRegexp == null || splitRegexp.isEmpty()) {
			throw new IllegalArgumentException("Split RegExp could not be null or empty");
		}

		this.url = url;
		this.splitRegexp = splitRegexp;
		this.bufferLineCapacity = bufferLineCapacity;
		this.bufferPosition = 0;
		this.wordCountMap = new HashMap<>();
	}

	public String getUrl() {

		return url;
	}

	public String getSplitters() {

		return splitRegexp;
	}

	public int getBufferLineCapacity() {

		return bufferLineCapacity;
	}

	public List<Map.Entry<String, Integer>> countWords(SortDirection sortDirection, boolean forceRefresh) {

		final SortDirection sort = Objects.requireNonNullElse(sortDirection, SortDirection.ASC);

		if (this.wordCountMap.isEmpty() || forceRefresh) {

			String chunk = getHtmlChunk();

			Jsoup.parse(chunk).childNodes().forEach(this::dfs);


		}

		return this.wordCountMap.entrySet().stream().sorted((o1, o2) -> {
			if (SortDirection.ASC.equals(sort)) {
				return o1.getValue() - o2.getValue();
			} else if (SortDirection.DESC.equals(sort)) {
				return o2.getValue() - o1.getValue();
			} else {
				return o1.getKey().compareTo(o2.getKey());
			}
		}).collect(Collectors.toList());
	}

	private String getHtmlChunk() {

		StringBuilder result = new StringBuilder();
		try {
			URL url = new URL(this.url);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			reader.lines().skip(bufferPosition).limit(bufferLineCapacity).forEach(line -> result.append(line).append("\n"));
			reader.close();
		} catch (IOException e) {
			System.out.printf("Не удалось загрузить страницу %s", this.url);
		}
		return result.toString();
	}

	private void dfs(Node node) {

		if (node instanceof TextNode) {
			Stream.of(((TextNode) node).getWholeText().split(this.splitRegexp)).forEach(word -> {
				if (!(word.isEmpty() || word.isBlank())) {
					if (wordCountMap.containsKey(word)) {
						wordCountMap.put(word, wordCountMap.get(word) + 1);
					} else {
						wordCountMap.put(word, 1);
					}
				}
			});
		} else if (node.childNodeSize() != 0) {
			node.childNodes().forEach(this::dfs);
		}
	}

}



