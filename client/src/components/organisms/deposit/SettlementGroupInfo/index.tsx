import React from 'react';
import { ISettlementGroup } from 'types/deposit';
import { formatMoney } from 'utils/common/formatMoney';
import { SettlementGroupInfoContainer } from './style';

function SettlementGroupInfo({ settlementGroup }: { settlementGroup: ISettlementGroup }) {
	const formattedMoneyUnit = formatMoney(settlementGroup.moneyUnit);
	const formattedTotalMoney = formatMoney(settlementGroup.cntBeforeReturn * settlementGroup.moneyUnit);

	return (
		<SettlementGroupInfoContainer>
			<div className="title">
				<span>{settlementGroup.title}</span> 건
			</div>
			<div className="money-unit">
				입금단위 <span>{formattedMoneyUnit}원</span>
			</div>
			<div className="before-return-total">
				반환 전 합계 <span>{formattedTotalMoney}원</span>
			</div>
		</SettlementGroupInfoContainer>
	);
}

export default SettlementGroupInfo;
