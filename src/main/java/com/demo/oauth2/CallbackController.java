package com.demo.oauth2;

import net.minidev.json.JSONObject;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping
public class CallbackController {

    @Autowired
    private WebClient webClient;
    @GetMapping("/callback")
    public ResponseEntity<String> welcome(@RequestParam String code) throws URISyntaxException, UnsupportedEncodingException {

        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();

        bodyValues.add("client_id", "85129361-94f5-4946-a765-4ca6b61849fc");
        bodyValues.add("client_secret", "uf88Q~GrVUMDgKeC-icdjL5yzLgx70ak.A2LKcb-");
        bodyValues.add("grant_type", "authorization_code");
        bodyValues.add("scope", "https://graph.microsoft.com/User.Read");
        bodyValues.add("code", code);
        bodyValues.add("redirect_uri", "http://localhost:8080/callback");


        JSONObject response = webClient.post()
                .uri(new URI("https://login.microsoftonline.com/6350a69c-bc2a-4586-9c11-317854dfca24/oauth2/v2.0/token"))
                //.header("Authorization", "Bearer MY_SECRET_TOKEN")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromFormData(bodyValues))
                .retrieve()
                .bodyToMono(JSONObject.class)
                .block();

        System.out.println("response  = "+response);
        String access_token = response.get("access_token").toString();
        System.out.println("access_token  = "+access_token);
        String payload = access_token.split("\\.")[1];
        System.out.println("payload  = "+payload);
        String payloadDecoded = new String(Base64.decodeBase64(payload), "UTF-8");
        System.out.println("payloadDecoded  = "+payloadDecoded);

        org.json.JSONObject jsonObject = new org.json.JSONObject(payloadDecoded);
        String emailfromjson = jsonObject.getString("email");
        System.out.println("emailfromjson  = "+emailfromjson);

        JSONObject responseSpec = webClient.get()
                //.uri(builder -> builder.path("https://graph.microsoft.com/v1.0/users").queryParam("$filter", "mail eq '"+emailfromjson+"'").build())
                .uri("https://graph.microsoft.com/v1.0/users?$filter=mail eq '"+emailfromjson+"'")
                .header("Authorization", "Bearer "+access_token)
                .retrieve()
                .bodyToMono(JSONObject.class)
                .block();
        System.out.println("users info  = "+responseSpec);
        System.out.println("users info  = "+responseSpec.get("value"));


        return ResponseEntity.ok("callback = "+code);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(){
        String url = "https://login.microsoftonline.com/6350a69c-bc2a-4586-9c11-317854dfca24/oauth2/v2.0/token" +
                "&client_id=85129361-94f5-4946-a765-4ca6b61849fc" +
                "&scope=2ff814a6-3304-4ab8-85cb-cd0e6f879c1d%2F.default" +
                "&code=0.AWAAnKZQYyq8hkWcETF4VN_KJGGTEoX1lEZJp2VMprYYSfxfADo.AgABAAIAAAD--DLA3VO7QrddgJg7WevrAgDs_wUA9P_cnfMOxfaZgeSAOpgg6KhHSm4tFTIt6Hps8IXXYqQ28N-9j1UyRBIxpRduwgYK_HFMFug0CcffurHGJaYGMFspH5RebuoXG6mzemVu8l64gTmOxLmhN-V6vrSkIFxTO5WSPv1OdbKfJ-zLWoVISmfs862_QtkM6Ikp6w2W2qeXN680Da5MbPeULoCLzmFrUCUlIwkkFmX9supxynCU9NC6D0h7pWdCpFc_BjRONmXD-jtiYZdYXD_kLcnR4m-PBouPn6TWO7rnwBXVB8gjlNDV3nPXMqiBNXPLFS9s24FLU4YFr6PH1tvcUNnLEd_sF_8GrlbpCe_dglHCe3UbDVOgEB_zn6Iq3OaKnDZ8kWWINETWi5Gfjfs8GkEuO0Ck4dAlZfgRPXr01Oi4bi-bIfPjeT8d2QA4YH6qOqp6W-dCPB7ZQYAMxv0hHdD7z0uKO7u7D7RDOb6R41klo6BPl7qF5l52Rk2W5qLBrP500Y2UZuaG85rKnxtOQeIPuABaR-vb4FMCohSSoXuAVhErQ--vqCaPuh3BZMyUBr6oYHRBVmPBFCnQjxlliLOl2a6gphfukGL6ePxRMWzKgDAEJ_4hPm9kVW0pqNvlwg6pTPNhlUdMUGwL_Vp2cZKcloyMG68yMmSkuiOSU2AIPmjv96r49NAiyOjecLhQNT1JTeO6qg5DBGakIbQF8qNDa2_-F09cLHTHsa7-xBE0nM7zd7G8rXEMrFOh0JQnkY7pMQaWJo1Q5j3Ln1DkDyqtfCFerlYFIs3WckegmAMv3wIVjuwMTTfKDjNHAo-yba7i1pszVf__z5Ju5HKduBs340udCuFFWgPXT8uXiedpsiT9IO3MsnNjffDZs_e9qNkohjiTwjBG0UaH-DWNEb6PusB-cq2z8lgeQt6wrOUHtvdAaVSo1Wf8cldtY_jx5xUBx7NMfPdjFYzo5iHcjUDdu9Ujmon28qGWZ17yVumHzxhZUyTMbtiTXG5R0R73xvQULvbABZyyqEKyP60BHSE6BO96C9eEcDwJPv9QOyeiunXmTwm9RlYkJBBCgCYOmPtBiuSq0Fk3RAM86brGUh5j1WOJOTzhqkCnJsQHO_pvq3n_dKP6A0GjABjBF0iVn8LvGj4ssMIjNNsuz_eZCisdKTXtv1u_ifA8cuirRDoQhaWcctlhYOGHiO29gLV1VtDeK7LbgLbcYw" +
                "&redirect_uri=http://localhost:8080/callback2" +
                "&grant_type=authorization_code" +
                "&state=123";
        System.out.print(url);
        String str = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return ResponseEntity.ok("test controller = "+str);
    }

    @GetMapping("/callback2")
    public ResponseEntity<String> callback2(){
        return ResponseEntity.ok("callback2 ");
    }

}
