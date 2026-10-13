package rdhxb.raportservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rdhxb.raportservice.service.TestService;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

    private final TestService service;

//    should create a rabbitmq post pending and waitng for consumer
    @PostMapping("/send")
    public void testSend(@RequestBody String testMess){
        service.sendTestMess(testMess);
    }

}
