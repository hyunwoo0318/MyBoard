package Lim.boardApp.api.controller;

import Lim.boardApp.api.dto.EmailParam;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.service.CustomerService;
import Lim.boardApp.service.EmailService;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.util.HashMap;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiController {

    private final EmailService emailService;
    private final CustomerService customerService;

    @ApiOperation(value = "이메일 주소", notes = "넘어온 이메일 주소로 인증코드를 보낸다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 이메일 발송 성공"),
            @ApiResponse(responseCode = "404", description = "등록되지 않은 이메일 주소")
    })
    @PostMapping(value = "/email-auth", produces = "application/json; charset=utf8")
    public ResponseEntity sendEmail(@RequestBody EmailParam emailParam) {

        Customer customer = customerService.findCustomerByEmail(emailParam.getEmail());
        if (customer == null) {
            return new ResponseEntity("등록되지 않은 이메일 주소입니다.", HttpStatus.NOT_FOUND);
        }

        try {
            emailService.sendEmailAuth(emailParam.getEmail());
        } catch (MessagingException e) {
            return new ResponseEntity("메일 발송 실패", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @ApiOperation(value="아이디", notes = "아이디 중복 체크")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중복되지 않은 아이디"),
            @ApiResponse(responseCode = "400", description = "중복된 아이디")
    })
    @PostMapping(value = "/dup-id", produces = "application/json; charset=utf8")
    public ResponseEntity checkDupLoginId(@RequestBody HashMap<String,String> map) {
        String loginId = map.get("loginId");
        boolean dupLoginIdRes = customerService.dupLoginId(loginId);
        if(dupLoginIdRes){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }else {
            return new ResponseEntity(HttpStatus.OK);
        }
    }

}
