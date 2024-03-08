package Lim.boardApp.common.crawling;

import Lim.boardApp.common.constant.TextType;
import Lim.boardApp.common.exception.NotFoundException;
import Lim.boardApp.domain.customer.entity.Customer;
import Lim.boardApp.domain.customer.repository.CustomerRepository;
import Lim.boardApp.domain.text.entity.Board;
import Lim.boardApp.domain.text.entity.Text;
import Lim.boardApp.domain.text.repository.board.BoardRepository;
import Lim.boardApp.domain.text.repository.text.TextRepository;

import lombok.RequiredArgsConstructor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrawlingService {

    private final String baseURL = "https://sports.news.naver.com/";
    private final TextRepository textRepository;

    private final BoardRepository boardRepository;
    private final CustomerRepository customerRepository;

    public void crawlingNews(String boardName) throws IOException {
        boardRepository
                .findByName(boardName)
                .orElseThrow(
                        () -> {
                            throw new NotFoundException();
                        });
        String urlName = findUrl(boardName);
        if (urlName == null) {
            throw new NotFoundException();
        }
        String url = baseURL + urlName + "/index";
        Document doc = Jsoup.connect(url).get();

        Elements elements = doc.select("div.home_news > ul.home_news_list > li > a");
        System.out.println("elements" + elements.toString());

        List<String> newsAddrList = new ArrayList<>();

        for (Element element : elements) {
            String href = element.attr("href");
            newsAddrList.add(href);
        }

        List<Text> textList = new ArrayList<>();
        for (String addr : newsAddrList) {
            url = baseURL + addr;
            Document document = Jsoup.connect(url).get();
            String title = document.selectFirst("div.news_headline > h4").text();
            String contentHtml = document.selectFirst("div#newsEndContents").html();
            String content = Jsoup.parse(contentHtml).wholeText();

            // 본문 내용만 발췌
            int idx = content.indexOf("기사제공");
            if (idx != -1) {
                content = content.substring(0, idx);
            }

            if (!textRepository.findByTitle(title).isEmpty()) {
                continue;
            }

            // 3줄 이상의 줄바꿈은 2줄 줄바꿈으로 변경
            content = content.replaceAll("\\n{3,}", "\n\n");

            Customer autoBot = customerRepository.findByName("auto-bot").get();
            Board board = boardRepository.findByName(boardName).get();
            Text text =
                    Text.builder()
                            .customer(autoBot)
                            .title(title)
                            .content(content)
                            .textType(TextType.ARTICLE)
                            .board(board)
                            .build();
            textList.add(text);
        }
        textRepository.saveAll(textList);
    }

    private String findUrl(String boardName) {
        if (boardName.equals(URLName.BASEBALL.inputName)) {
            return URLName.BASEBALL.urlName;
        } else if (boardName.equals(URLName.BASKETBALL.inputName)) {
            return URLName.BASKETBALL.urlName;
        } else if (boardName.equals(URLName.VOLLEYBALL.inputName)) {
            return URLName.VOLLEYBALL.urlName;
        } else if (boardName.equals(URLName.GOLF.inputName)) {
            return URLName.GOLF.urlName;
        } else if (boardName.equals(URLName.SOCCER.inputName)) {
            return URLName.SOCCER.urlName;
        }
        return null;
    }

    private enum URLName {
        SOCCER("축구", "wfootball"),
        BASKETBALL("농구", "basketball"),
        GOLF("골프", "golf"),
        VOLLEYBALL("배구", "volleyball"),
        BASEBALL("야구", "wbaseball"),
        ;

        private String inputName;
        private String urlName;

        URLName(String inputName, String urlName) {
            this.inputName = inputName;
            this.urlName = urlName;
        }
    };
}
