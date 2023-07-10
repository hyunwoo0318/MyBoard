package Lim.boardApp.service;

import Lim.boardApp.domain.Board;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;
import Lim.boardApp.repository.BoardRepository;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.repository.text.TextRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CrawlingService {

    private final TextRepository textRepository;
    private final CustomerRepository customerRepository;
    private final BoardRepository boardRepository;

    private final String baseURL = "https://sports.news.naver.com/";
    private String category;

    public void crawlingNews() throws IOException {
        String url = baseURL + "basketball/index";
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

            if (!textRepository.findByTitle(title).isEmpty()) {
                continue;
            }

            Customer autoBot = customerRepository.findByName("auto-bot").get();
            Board board = boardRepository.findByName("basketball").get();
            Text text = new Text(content, title, null, autoBot, board);
            textList.add(text);
        }
        textRepository.saveAll(textList);
    }

    //TODO : 원하는 종류의 기사를 크롤링 하게함(해외축구, 국내축구, 농구 등등 네이버 기준에 맞춰서)
    //TODO : 기사게시판, 사진게시판, 글 게시판을 구별함(크게 각각을 만들고 그 안에서 축구,농구 등등으로 구별하기)
    //TODO : /news를 POST로 변경하고 버튼 만들기
    //TODO : 페이징이 세로로 세워지는 문제 해결
    //TODO ; 사진 게시판 생성
    //TODO : remember-me 기능 구현
    //TODO : 글을 관리자도 작성가능하게 하여 관리자의 글은 설정시 항시 상단 고정

    //TODO : 관리자 페이지 구현(유저 관리, 관리자는 모든 글,댓글 삭제 가능) -> 관리자의 기능 생각해보기

    //TODO : Docker로 컨테이너화를 거쳐서 배포
    //TODO : 각각의 기능에 대해서 문서화를 진행
}
