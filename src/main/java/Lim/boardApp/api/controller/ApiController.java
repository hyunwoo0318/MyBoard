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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiController {

    private final EmailService emailService;
    private final CustomerService customerService;

    @ApiOperation(value = "이메일 주소", notes = "넘어온 이메일 주소로 인증코드를 보낸다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 이메일 발송 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 이메일주소"),
            @ApiResponse(responseCode = "404", description = "등록되지 않은 이메일 주소")
    })
    @PostMapping(value = "/email-auth", produces = "application/json; charset=utf8")
    public ResponseEntity sendEmail(@RequestBody EmailParam emailParam) {
        System.out.println("sendEmail API called!");
        boolean checkEmailFormRes = emailService.checkEmailForm(emailParam.getEmail());
        if (!checkEmailFormRes) {
            return new ResponseEntity("유효하지 않은 이메일 주소입니다.",HttpStatus.BAD_REQUEST);
        }
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

}
