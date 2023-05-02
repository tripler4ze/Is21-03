package org.example;

public class Main {

	public static void main(String[] args) {

		HtmlPage page = new HtmlPage("https://simbirsoft.com", "[\s|\t|\n|\r|.|,|!|?|\"|:|;|[|]|(|)]+");
		page.countWords(SortDirection.DESC, true).forEach(System.out::println);
	}

}