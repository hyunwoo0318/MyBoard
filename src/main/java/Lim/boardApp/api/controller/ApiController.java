package Lim.boardApp.api.controller;

import Lim.boardApp.api.dto.EmailParam;
import Lim.boardApp.common.email.EmailService;
import Lim.boardApp.domain.customer.entity.Customer;
import Lim.boardApp.domain.customer.service.CustomerService;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

import javax.mail.MessagingException;

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
    public ResponseEntity<String> sendEmail(@RequestBody EmailParam emailParam) {

        Customer customer = customerService.findCustomerByEmail(emailParam.getEmail());
        if (customer == null) {
            return new ResponseEntity<String>("등록되지 않은 이메일 주소입니다.", HttpStatus.NOT_FOUND);
        }

        try {
            emailService.sendEmailAuth(emailParam.getEmail());
        } catch (MessagingException e) {
            return new ResponseEntity<String>("메일 발송 실패", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @ApiOperation(value="아이디", notes = "아이디 중복 체크")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중복되지 않은 아이디"),
            @ApiResponse(responseCode = "400", description = "중복된 아이디")
    })
    @PostMapping(value = "/dup-id", produces = "application/json; charset=utf8")
    public ResponseEntity<Void> checkDupLoginId(@RequestBody HashMap<String,String> map) {
        String loginId = map.get("loginId");
        boolean dupLoginIdRes = customerService.dupLoginId(loginId);
        if(dupLoginIdRes){
            return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
        }else {
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
    }

}
