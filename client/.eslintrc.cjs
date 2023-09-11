module.exports = {
	root: true,
	env: { browser: true, es2020: true },
	extends: [
		'plugin:@typescript-eslint/recommended',
		'airbnb',
		'airbnb/hooks',
		'airbnb-typescript',
		'plugin:prettier/recommended',
		'prettier',
	],
	ignorePatterns: ['dist', '.eslintrc.cjs', 'vite.config.ts'],
	parserOptions: {
		project: './tsconfig.json',
	},
	parser: '@typescript-eslint/parser',
	plugins: ['@typescript-eslint', 'react', 'prettier'],
	rules: {
		'prettier/prettier': ['error', { endOfLine: 'auto' }],
		'import/prefer-default-export': 'off',
		"react/require-default-props": "off",
		"react-hooks/exhaustive-deps": "off",
		"react/jsx-no-useless-fragment": "off",
		"react/jsx-props-no-spreading": "off",
		"no-restricted-syntax": "off",
		"no-await-in-loop": "off",
		"react/no-array-index-key" : "off"
 	},
};
