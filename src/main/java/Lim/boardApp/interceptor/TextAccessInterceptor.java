package Lim.boardApp.interceptor;

import Lim.boardApp.ObjectValue.SessionConst;
import Lim.boardApp.domain.Text;
import Lim.boardApp.repository.TextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

public class TextAccessInterceptor implements HandlerInterceptor {

    @Autowired TextRepository textRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        HttpSession session = request.getSession(false);

        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Optional<Text> textOp = textRepository.findById(Long.parseLong(pathVariables.get("id")));
        if(textOp.isEmpty()){
            response.sendRedirect("/board?redirectURL=" + requestURI);
            return false;
        }else if(textOp.get().getCustomer().getId() != session.getAttribute(SessionConst.LOGIN_CUSTOMER)){
            response.sendRedirect("/board?redirectURL=" + requestURI);
            return false;
        }else{
            return true;
        }
    }
}
