package com.shinhan.changyo.api.service.transfer;

import com.shinhan.changyo.api.controller.transfer.response.*;
import com.shinhan.changyo.domain.qrcode.SimpleQrCode;
import com.shinhan.changyo.domain.qrcode.repository.SimpleQrCodeRepository;
import com.shinhan.changyo.domain.transfer.TransferQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

/**
 * 이체정보 조회 서비스
 *
 * @author 최영환
 */
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TransferQueryService {

    private final TransferQueryRepository transferQueryRepository;
    private final SimpleQrCodeRepository simpleQrCodeRepository;

    /**
     * 보증금 이체 정보 조회
     *
     * @param qrCodeId QR 코드 식별키
     * @param loginId  현재 로그인 중인 회원 로그인 아이디
     * @return 보증금 이체 정보
     */
    public TransferInfoResponse getTransferInfo(Long qrCodeId, String loginId) {
        ClientAccountResponse clientAccount = transferQueryRepository.getClientAccount(loginId);
        StoreAccountResponse storeAccount = transferQueryRepository.getStoreAccountByQrCodeId(qrCodeId);

        return TransferInfoResponse.of(storeAccount, clientAccount);
    }

    /**
     * 간편송금 이체 정보 조회
     *
     * @param simpleQrCodeId 간편송금 QR 코드 식별키
     * @param loginId        현재 로그인 중인 회원 로그인 아이디
     * @return 간편송금 이체 정보
     */
    public SimpleTransferInfoResponse getSimpleTransferInfo(Long simpleQrCodeId, String loginId) {
        SimpleQrCode simpleQrCode = simpleQrCodeRepository.findById(simpleQrCodeId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 QR 코드입니다."));
        Duration diff = Duration.between(simpleQrCode.getLastModifiedDate(), LocalDateTime.now());
        log.debug("diff={}", diff.toMinutes());

        if (isExpired(diff.toMinutes())) {
            simpleQrCodeRepository.delete(simpleQrCode);
            throw new IllegalArgumentException("유효시간이 만료되었습니다.");
        }

        ClientAccountResponse clientAccount = transferQueryRepository.getClientAccount(loginId);
        SimpleStoreAccountResponse storeAccount = transferQueryRepository.getStoreAccountBySimpleQrCodeId(simpleQrCodeId);
        return SimpleTransferInfoResponse.of(storeAccount, clientAccount);
    }

    /**
     * 만료여부 확인
     *
     * @param minutes 현재시간과 최종수정시간 차이 (분)
     * @return true: 3분이 지난 경우, false: 3분이 지나지 않은 경우
     */
    private boolean isExpired(long minutes) {
        return minutes > 3;
    }
}
