import React, { ChangeEvent, Dispatch, SetStateAction, useState } from 'react';
import Input from '@mui/material/Input';
import { UnderLineInputWrapper } from './style';

interface IUnderLineInputProps<T> {
	type: string;
	value: T;
	setValue: Dispatch<SetStateAction<T>>;
	placeholder: string;
	width: string;
	unitText?: string;
}
function UnderLineInput<T>(props: IUnderLineInputProps<T>) {
	const { type, value, setValue, placeholder, unitText, width } = props;
	const [displayValue, setDisplayValue] = useState(value);

	const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
		setValue(e.target.value as T);
		setDisplayValue(e.target.value as T);
	};

	console.log(type);
	return (
		<UnderLineInputWrapper $width={width}>
			<Input type="" value={displayValue} onChange={handleChange} placeholder={placeholder} /> {unitText}
		</UnderLineInputWrapper>
	);
}

export default UnderLineInput;
