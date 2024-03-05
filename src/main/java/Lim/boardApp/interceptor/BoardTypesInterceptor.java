package Lim.boardApp.interceptor;

import Lim.boardApp.domain.Board;
import Lim.boardApp.repository.BoardRepository;
import io.swagger.models.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;


public class BoardTypesInterceptor implements HandlerInterceptor {

    @Autowired private BoardRepository boardRepository;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        if(modelAndView != null){
            String viewName = modelAndView.getViewName();
            if(viewName.startsWith(UrlBasedViewResolver.REDIRECT_URL_PREFIX)){
                return;
            }

            List<Board> boardList = boardRepository.findAll();
            if (!boardList.isEmpty()) {
                List<String> boardNameList = boardList.stream().map(m -> m.getName()).collect(Collectors.toList());
                modelAndView.getModel().put("boardNameList", boardNameList);
            }
        }

    }
}
