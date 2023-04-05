package Lim.boardApp.service;

import Lim.boardApp.ObjectValue.KakaoConst;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class OauthService {

    public String getKakaoToken(String code, String usage){

        String accessToken = "";
        String refreshToken = "";
        String redirectURL = "";

        if(usage == "login"){
            redirectURL = KakaoConst.REDIRECT_URL_LOGIN;
        }else{
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
}
