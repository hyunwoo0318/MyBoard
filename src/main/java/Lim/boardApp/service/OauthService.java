package Lim.boardApp.service;

import Lim.boardApp.ObjectValue.KakaoConst;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.repository.CustomerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class OauthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User>  {

    private final CustomerRepository customerRepository;

    public String getKakaoToken(String code){

        String accessToken = "";
        String refreshToken = "";
        String redirectURL = "";

        {
            redirectURL = KakaoConst.REDIRECT_URL_REG;
        }

        try{
            URL url = new URL(KakaoConst.REQ_URL_TOKEN);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            //OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
            connection.setDoOutput(true);

            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("grant_type=authorization_code");
            stringBuilder.append("&client_id=" + KakaoConst.KEY);
            stringBuilder.append("&redirect_uri=" + redirectURL);
            stringBuilder.append("&code=" + code);
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.flush();

            int httpCode = connection.getResponseCode();
            if(httpCode == 200){
                System.out.println("success!");
            }else{
                throw new RuntimeException("responseCode Error!" + httpCode);
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = "";
            String body = "";

            while ((line = bufferedReader.readLine()) != null) {
                body += line;
            }
            System.out.println(body);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(body);

            accessToken = jsonNode.get("access_token").toString();
            refreshToken = jsonNode.get("refresh_token").toString();

            bufferedReader.close();
            bufferedWriter.close();

        }catch (Exception e){
            e.printStackTrace();
        }
        return accessToken;
    }

    public Long getUserID(String token) {
        Long id=-1L;
        try{
            URL url = new URL(KakaoConst.REQ_URL_INFO);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("responseCode Error!" + responseCode);
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = "";
            String body = "";

            while ((line = bufferedReader.readLine()) != null) {
                body += line;
            }

            System.out.println(body);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(body);

            id = jsonNode.get("id").asLong();

            bufferedReader.close();

        } catch (Exception e){
            e.printStackTrace();
        }
        return id;
    }

    @Override
    public Customer loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        Long kakaoId = oAuth2User.getAttribute("id");
        Customer customer = customerRepository.findByKakaoId(kakaoId).orElseThrow();
        return customer;
    }
}
