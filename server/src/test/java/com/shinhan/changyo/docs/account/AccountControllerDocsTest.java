package com.shinhan.changyo.docs.account;

import com.shinhan.changyo.api.controller.account.AccountController;
import com.shinhan.changyo.api.controller.account.request.CreateAccountRequest;
import com.shinhan.changyo.api.controller.account.response.AccountDetailResponse;
import com.shinhan.changyo.api.controller.account.response.AccountResponse;
import com.shinhan.changyo.api.service.account.AccountQueryService;
import com.shinhan.changyo.api.service.account.AccountService;
import com.shinhan.changyo.api.service.account.dto.CreateAccountDto;
import com.shinhan.changyo.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AccountControllerDocsTest extends RestDocsSupport {

    private final AccountService accountService = mock(AccountService.class);
    private final AccountQueryService accountQueryService = mock(AccountQueryService.class);

    @Override
    protected Object initController() {
        return new AccountController(accountService, accountQueryService);
    }

    @DisplayName("계좌 등록 API")
    @Test
    void createAccount() throws Exception {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .memberId(1L)
                .customerName("김싸피")
                .bankCode("088")
                .accountNumber("110184999999")
                .productName("예금")
                .title("싸피월급통장")
                .mainAccount(true)
                .build();

        Long accountId = 1L;

        given(accountService.createAccount(any(CreateAccountDto.class)))
                .willReturn(accountId);

        mockMvc.perform(
                        post("/account")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("create-account",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("memberId").type(JsonFieldType.NUMBER)
                                        .description("회원 식별키"),
                                fieldWithPath("customerName").type(JsonFieldType.STRING)
                                        .description("고객명"),
                                fieldWithPath("bankCode").type(JsonFieldType.STRING)
                                        .description("은행코드"),
                                fieldWithPath("accountNumber").type(JsonFieldType.STRING)
                                        .description("계좌번호"),
                                fieldWithPath("productName").type(JsonFieldType.STRING)
                                        .description("상품명"),
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .description("별칭"),
                                fieldWithPath("mainAccount").type(JsonFieldType.BOOLEAN)
                                        .description("주계좌여부")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER)
                                        .description("코드"),
                                fieldWithPath("status").type(JsonFieldType.STRING)
                                        .description("상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("메시지"),
                                fieldWithPath("data").type(JsonFieldType.NUMBER)
                                        .description("계좌 식별키")
                        )
                ));
    }

    @DisplayName("회원별 계좌 전체 조회 API")
    @Test
    void getAccounts() throws Exception {
        Long memberId = 1L;

        AccountDetailResponse account1 = AccountDetailResponse.builder()
                .accountNumber("110184999999")
                .balance(200501)
                .bankCode("088")
                .mainAccount(true)
                .build();

        AccountDetailResponse account2 = AccountDetailResponse.builder()
                .accountNumber("110185999999")
                .balance(0)
                .bankCode("088")
                .mainAccount(false)
                .build();

        List<AccountDetailResponse> accounts = List.of(account1, account2);

        AccountResponse response = AccountResponse.builder()
                .accountSize(accounts.size())
                .accountDetailResponses(accounts)
                .build();

        given(accountQueryService.getAccounts(anyLong()))
                .willReturn(response);

        mockMvc.perform(
                        get("/account")
                                .param("memberId", String.valueOf(memberId))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("search-account",
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("memberId")
                                        .description("회원 식별키")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER)
                                        .description("코드"),
                                fieldWithPath("status").type(JsonFieldType.STRING)
                                        .description("상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("메시지"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.accountSize").type(JsonFieldType.NUMBER)
                                        .description("전체 계좌 개수"),
                                fieldWithPath("data.accountDetailResponses").type(JsonFieldType.ARRAY)
                                        .description("계좌 정보 데이터"),
                                fieldWithPath("data.accountDetailResponses[].accountNumber").type(JsonFieldType.STRING)
                                        .description("계좌번호"),
                                fieldWithPath("data.accountDetailResponses[].balance").type(JsonFieldType.NUMBER)
                                        .description("잔액"),
                                fieldWithPath("data.accountDetailResponses[].bankCode").type(JsonFieldType.STRING)
                                        .description("은행코드"),
                                fieldWithPath("data.accountDetailResponses[].mainAccount").type(JsonFieldType.BOOLEAN)
                                        .description("주계좌여부")
                        )
                ));
    }
}
