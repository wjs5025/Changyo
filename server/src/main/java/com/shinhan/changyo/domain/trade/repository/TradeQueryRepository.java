package com.shinhan.changyo.domain.trade.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shinhan.changyo.api.controller.trade.response.DepositOverviewResponse;
import com.shinhan.changyo.api.controller.trade.response.DoneWithdrawalDetailResponse;
import com.shinhan.changyo.api.controller.trade.response.WaitWithdrawalDetailResponse;
import com.shinhan.changyo.api.service.trade.dto.DepositDetailDto;
import com.shinhan.changyo.api.service.trade.dto.MemberAccountDto;
import com.shinhan.changyo.domain.trade.TradeStatus;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.shinhan.changyo.domain.account.QAccount.account;
import static com.shinhan.changyo.domain.member.QMember.member;
import static com.shinhan.changyo.domain.qrcode.QQrCode.qrCode;
import static com.shinhan.changyo.domain.trade.QTrade.trade;
import static com.shinhan.changyo.domain.trade.SizeConstants.PAGE_SIZE;

/**
 * 보증금 거래내역 쿼리 저장소
 *
 * @author 최영환
 */
@Repository
public class TradeQueryRepository {

    private final JPAQueryFactory queryFactory;

    public TradeQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 보증금 반환대기 송금내역 조회
     *
     * @param loginId 조회하려는 회원의 로그인 아이디
     * @return 해당 회원의 전체 보증금 반환대기 송금내역
     */
    public List<WaitWithdrawalDetailResponse> getWaitingWithdrawalTrades(String loginId) {
        List<Long> accountIds = getAccountIdsByLoginId(loginId);

        if (accountIds == null || accountIds.isEmpty()) {
            return new ArrayList<>();
        }

        return queryFactory
                .select(Projections.constructor(WaitWithdrawalDetailResponse.class,
                        trade.id,
                        qrCode.title,
                        member.name,
                        trade.withdrawalAmount,
                        trade.status
                ))
                .from(trade)
                .join(trade.account, account)
                .join(trade.qrCode, qrCode)
                .join(qrCode.account, account)
                .join(account.member, member)
                .where(
                        account.id.in(accountIds),
                        trade.status.eq(TradeStatus.WAIT)
                )
                .orderBy(trade.createdDate.desc())
                .fetch();
    }

    /**
     * 보증금 반환완료 송금내역 개수 조회
     *
     * @param loginId 현재 로그인한 회원의 로그인 아이디
     * @return 보증금 반환완료 송금내역 개수
     */
    public Long getDoneWithdrawalTradesCount(String loginId) {
        List<Long> accountIds = getAccountIdsByLoginId(loginId);

        if (accountIds == null || accountIds.isEmpty()) {
            return 0L;
        }

        return queryFactory
                .select(trade.count())
                .from(trade)
                .join(trade.account, account)
                .join(trade.qrCode, qrCode)
                .join(qrCode.account, account)
                .join(account.member, member)
                .where(
                        account.id.in(accountIds),
                        trade.status.ne(TradeStatus.WAIT)
                )
                .fetchOne();
    }

    /**
     * 보증금 반환완료 송금내역 조회
     *
     * @param loginId     현재 로그인한 회원의 로그인 아이디
     * @param lastTradeId 마지막으로 조회된 거래내역 식별키
     * @return 보증금 반환완료 송금내역
     */
    public List<DoneWithdrawalDetailResponse> getDoneWithdrawalTrades(String loginId, Long lastTradeId) {
        List<Long> accountIds = getAccountIdsByLoginId(loginId);

        if (accountIds == null || accountIds.isEmpty()) {
            return new ArrayList<>();
        }

        return queryFactory
                .select(Projections.constructor(DoneWithdrawalDetailResponse.class,
                        trade.id,
                        qrCode.title,
                        member.name,
                        trade.withdrawalAmount,
                        trade.lastModifiedDate
                ))
                .from(trade)
                .join(trade.account, account)
                .join(trade.qrCode, qrCode)
                .join(qrCode.account, account)
                .join(account.member, member)
                .where(
                        account.id.in(accountIds),
                        trade.status.ne(TradeStatus.WAIT),
                        isLagerThanLastTradeId(lastTradeId)
                )
                .orderBy(trade.createdDate.desc())
                .limit(PAGE_SIZE + 1)
                .fetch();
    }

    private BooleanExpression isLagerThanLastTradeId(Long tradeId) {
        return tradeId == null ? null : trade.id.lt(tradeId);
    }

    /**
     * 보증금 정산관리 목록 개수 조회
     *
     * @param loginId 현재 로그인한 회원 로그인아이디
     * @return 보증금 정산관리 내역 총 개수
     */
    public int getDepositTradesTotalCount(String loginId) {
        List<Long> accountIds = getAccountIdsByLoginId(loginId);

        if (accountIds == null || accountIds.isEmpty()) {
            return 0;
        }

        return Objects.requireNonNull(queryFactory
                .select(qrCode.count())
                .from(qrCode)
                .join(qrCode.account, account)
                .where(account.id.in(accountIds))
                .fetchOne()).intValue();
    }

    /**
     * 보증금 정산관리 조회
     *
     * @param loginId      로그인한 회원의 로그인 아이디
     * @param lastQrCodeId 마지막으로 조회된 QR 코드 식별키
     * @return 해당 회원의 보증금 입금내역 목록
     */
    public List<DepositOverviewResponse> getDepositTradeOverviews(String loginId, Long lastQrCodeId) {
        List<Long> accountIds = getAccountIdsByLoginId(loginId);

        if (accountIds == null || accountIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> qrCodeIds = getQrCodeIds(accountIds);

        if (qrCodeIds == null || qrCodeIds.isEmpty()) {
            return new ArrayList<>();
        }

        return queryFactory
                .select(Projections.constructor(DepositOverviewResponse.class,
                        qrCode.qrCodeId,
                        qrCode.title,
                        trade.depositAmount,
                        trade.depositAmount.sum(),
                        trade.countDistinct().intValue()
                ))
                .from(trade)
                .join(trade.qrCode, qrCode)
                .where(
                        qrCode.qrCodeId.in(qrCodeIds),
                        trade.status.eq(TradeStatus.WAIT),
                        isLagerThanLastQrCodeId(lastQrCodeId)
                )
                .groupBy(qrCode.qrCodeId, trade.createdDate)
                .orderBy(trade.createdDate.desc())
                .limit(PAGE_SIZE + 1)
                .fetch();
    }

    private BooleanExpression isLagerThanLastQrCodeId(Long qrCodeId) {
        return qrCodeId == null ? null : qrCode.qrCodeId.lt(qrCodeId);
    }

    /**
     * 계좌 식별키 목록 조회
     *
     * @param loginId 조회할 회원 로그인 아이디
     * @return 해당 회원이 가진 계좌 식별키 목록
     */
    private List<Long> getAccountIdsByLoginId(String loginId) {
        return queryFactory.select(account.id)
                .from(account)
                .join(account.member, member)
                .where(member.loginId.eq(loginId))
                .fetch();
    }

    /**
     * QR 코드 식별키 목록 조회
     *
     * @param accountIds 계좌 식별키 목록
     * @return 계좌들에 해당하는 QR 코드 식별키 목록
     */
    private List<Long> getQrCodeIds(List<Long> accountIds) {
        return queryFactory
                .select(qrCode.qrCodeId)
                .from(qrCode)
                .join(qrCode.account, account)
                .where(account.id.in(accountIds))
                .fetch();
    }

    /**
     * 보증금 정산관리 반환대기 목록개수 조회
     *
     * @param qrCodeId QR 코드 식별키
     * @return 보증금 정산대기 반환완료 목록개수
     */
    public int getWaitDepositCountByQrCodeId(Long qrCodeId) {
        return Objects.requireNonNull(queryFactory
                .select(trade.count())
                .from(trade)
                .join(trade.account, account)
                .join(account.member, member)
                .where(
                        trade.qrCode.qrCodeId.eq(qrCodeId),
                        trade.status.eq(TradeStatus.WAIT)
                )
                .fetchOne()).intValue();
    }

    /**
     * 보증금 정산관리 반환완료 목록개수 조회
     *
     * @param qrCodeId QR 코드 식별키
     * @return 보증금 정산관리 반환완료 목록개수
     */
    public int getDoneDepositCountByQrCodeId(Long qrCodeId) {
        return Objects.requireNonNull(queryFactory
                .select(trade.count())
                .from(trade)
                .join(trade.account, account)
                .join(account.member, member)
                .where(
                        trade.qrCode.qrCodeId.eq(qrCodeId),
                        trade.status.ne(TradeStatus.WAIT)
                )
                .fetchOne()).intValue();
    }

    /**
     * 보증금 정산관리 상세조회
     *
     * @param qrCodeId    QR 코드 식별키
     * @param lastTradeId 마지막으로 조회된 QR 코드 식별키
     * @return 보증금 정산관리 상세 내역
     */
    public List<DepositDetailDto> getDepositDetails(Long qrCodeId, Long lastTradeId) {
        return queryFactory
                .select(Projections.constructor(DepositDetailDto.class,
                        trade.id,
                        trade.status,
                        account.member.name,
                        trade.createdDate
                ))
                .from(trade)
                .join(trade.account, account)
                .join(account.member, member)
                .where(
                        trade.qrCode.qrCodeId.eq(qrCodeId),
                        isLagerThanLastTradeId(lastTradeId)
                )
                .orderBy(trade.createdDate.desc())
                .limit(PAGE_SIZE * 2 + 1)
                .fetch();
    }

    /**
     * QR 코드 식별키가 일치하는 모든 거래내역의 금액 총합 조회
     *
     * @param qrCodeId QR 코드 식별키
     * @return 거래내역 금액 총합
     */
    public int getTotalAmountByQrCodeId(Long qrCodeId) {
        return Objects.requireNonNull(queryFactory
                .select(trade.depositAmount.sum())
                .from(trade)
                .where(trade.qrCode.qrCodeId.eq(qrCodeId))
                .fetchOne());
    }

    /**
     * 입금자 (QR 코드 소유 회원) 계좌 정보 조회
     *
     * @param tradeId 보증금 거래내역 식별키
     * @return 입금자 (QR 코드 소유 회원) 계좌 정보
     */
    public MemberAccountDto getDepositAccount(Long tradeId) {
        return queryFactory
                .select(Projections.constructor(MemberAccountDto.class,
                        account.id,
                        account.accountNumber,
                        member.name
                ))
                .from(trade)
                .join(trade.qrCode, qrCode)
                .join(qrCode.account, account)
                .join(account.member, member)
                .where(trade.id.eq(tradeId))
                .fetchOne();
    }

    /**
     * 송금자 계좌 정보 조회
     *
     * @param tradeId 보증금 거래내역 식별키
     * @return 송금자 계좌 정보
     */
    public MemberAccountDto getWithdrawalAccount(Long tradeId) {
        return queryFactory
                .select(Projections.constructor(MemberAccountDto.class,
                        account.id,
                        account.accountNumber,
                        member.name
                ))
                .from(trade)
                .join(trade.account, account)
                .join(account.member, member)
                .where(trade.id.eq(tradeId))
                .fetchOne();
    }
}
