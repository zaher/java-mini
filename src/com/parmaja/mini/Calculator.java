package com.parmaja.mini;

/*
 * 
 * 
 */
import java.text.DecimalFormat;
import java.util.Arrays;

public class Calculator {

	protected enum CalcState {
		csFirst, csValid, csError
	};

	protected boolean bStarted = false;
	protected CalcState calcState = CalcState.csFirst;
	protected String sNumber = "0";
	protected char charSign = ' ';
	
	protected char cCurrentOperator = '=';
	protected char cLastOperator = ' ';
	protected double dLastResult = 0;
	protected double dOperand = 0;
	protected double dMemory = 0;
	protected boolean bHaveMemory = false;
	protected double dDisplayNumber = 0;
	protected boolean bHexShown = false;

	public int iMaxDecimals = 10;
	public int iMaxDigits = 30;
	
	public Calculator() {
		reset();
	}
	
	@Override
	public String toString() {		
		return charSign+sNumber;
	}

	public String getNumber(){
		return sNumber;
	}
	
	public String getSign(){
		return String.valueOf(charSign);
	}
	
	public String repeat(char c, int n) {
		char[] chars = new char[n];
		Arrays.fill(chars, c);
		return chars.toString();
	}

	public String format(double d) {
		DecimalFormat df = new DecimalFormat("#0.#");
		return df.format(d);
	}

	public double getDisplay() {
		return dDisplayNumber;
	}

	public void setDisplay(double d, boolean ShouldKeepZeroes) {
		String s;
		int KeepZeroes;

		dDisplayNumber = d;
		KeepZeroes = 0;
		int p = sNumber.indexOf('.');

		if (ShouldKeepZeroes && p >= 0) {
			int i = sNumber.length() - 1;
			while (i > p) {
				if (sNumber.charAt(i) == '0')
					KeepZeroes++;
				else
					break;
			}
		}

		s = format(d);

		if (KeepZeroes > 0)
			s = s + repeat('0', KeepZeroes);

		//Move the sign to a variable
		if (s.charAt(0) == '-') { 
			s = s.substring(1);
			charSign = '-';
		} else 
			charSign = ' ';		

		if (s.length() > iMaxDigits + 1 + iMaxDecimals)
			error();
		else {
			if (s.endsWith("."))
				s = s.substring(s.length() - 1);
			sNumber = s;
		}
	}

	protected void check() {
		if (calcState == CalcState.csFirst) {
			calcState = CalcState.csValid;
			setDisplay(0, false);
		}
	}

	public boolean process(String key) {

		double r;
		String s;
		boolean result = true;

		key = key.toUpperCase();

		if ((calcState == CalcState.csError) && (key != "C"))
			key = " ";
		r = 0;
		if (bHexShown) {
			r = getDisplay();
			setDisplay(r, false);
			bHexShown = false;
			if (key == "H")
				key = " ";
		}

		if (key == "X^Y")
			key = "^";
		else if (key == "_")
			key = "+/-";

		if (key.length() > 1) {
			r = getDisplay();
			if (key == "ON")
				reset();
			else if (key == "AC")
				clear();
			else if (key == "CR") {
				check();
				setDisplay(0, true);
			} else if (key == "1/X") {
				if (r == 0)
					error();
				else
					setDisplay(1 / r, false);
			} else if (key == "SQRT") {
				if (r < 0)
					error();
				else
					setDisplay(Math.sqrt(r), false);
			} else if (key == "LOG") {
				if (r <= 0)
					error();
				else
					setDisplay(Math.log(r), false);
			} else if (key == "X^2")
				setDisplay(r * r, false);
			else if (key == "+/-") {
				if (charSign == ' ')
					charSign = '-';
				else
					charSign = ' ';
				r = getDisplay();
				setDisplay(-r, true);
			} else if (key == "M+") {
				dMemory = dMemory + r;
				bHaveMemory = true;
			} else if (key == "M-") {
				dMemory = dMemory - r;
				bHaveMemory = true;
			} else if (key == "MR") {
				check();
				setDisplay(dMemory, false);
			} else if (key == "MC") {
				dMemory = 0;
				bHaveMemory = false;
			} else if (key == "DEL") // Delete
			{
				check();
				if (sNumber.length() == 1)
					sNumber = "0";
				else
					sNumber = sNumber.substring(0, sNumber.length() - 2);
				setDisplay(Double.valueOf(sNumber), true);// { !!! }
			}
		} else { // key is one char
			char k = key.charAt(0);

			if (k >= '0' && k <= '9') {
				if (sNumber.length() < iMaxDigits) {
					check();
					if (sNumber == "0")
						sNumber = "";
					sNumber = sNumber + k;
					dDisplayNumber = Double.parseDouble(sNumber);
					// SetDisplay(StrToFloat(Number), True);
				}
			} else if (k == '.') {
				check();
				if (sNumber.indexOf('.') < 0)
					sNumber = sNumber + '.';
			} else if (k == 'H') {
				r = getDisplay();
				sNumber = Long.toHexString(Math.round(r));
				bHexShown = true;
			} else { // finally else '^', '+', '-', '*', '/', '%', '='
				if ((k == '=') && (calcState == CalcState.csFirst)) {
					// for repeat last operator
					calcState = CalcState.csValid;
					r = dLastResult;
					cCurrentOperator = cLastOperator;
				} else
					r = getDisplay();

				if (calcState == CalcState.csValid) {
					bStarted = true;
					if (cCurrentOperator == '=')
						s = " ";
					else
						s = String.valueOf(cCurrentOperator);

					log(s + format(r));

					calcState = CalcState.csFirst;
					cLastOperator = cCurrentOperator;
					dLastResult = r;
					if (k == '%') {
						if (cCurrentOperator == '+'
								|| cCurrentOperator == '-')
							r = dOperand * r / 100;
						else if (cCurrentOperator == '*'
								|| cCurrentOperator == '/')
							r = r / 100;
					}

					else if (cCurrentOperator == '^') {
						if ((dOperand == 0) && (r <= 0))
							error();
						else
							setDisplay(Math.pow(dOperand, r), false);
					} else if (cCurrentOperator == '+')
						setDisplay(dOperand + r, false);
					else if (cCurrentOperator == '-')
						setDisplay(dOperand - r, false);
					else if (cCurrentOperator == '*')
						setDisplay(dOperand * r, false);
					else if (cCurrentOperator == '/') {
						if (r == 0)
							error();
						else
							setDisplay(dOperand / r, false);
					}
				}
				if (k == '=')
					log('=' + sNumber);

				cCurrentOperator = k;
				dOperand = getDisplay();

			}
			result = false;
		}
		refresh();
		return result;
	}

	public void clear() {
		if (bStarted)
			log(repeat('-', iMaxDigits + 1 + iMaxDecimals));
		bStarted = false;
		calcState = CalcState.csFirst;
		sNumber = "0";
		charSign = ' ';
		cCurrentOperator = '=';
	}

	public void reset() {
		clear();
		bHaveMemory = false;
		dMemory = 0;
	}

	protected void error() {
		calcState = CalcState.csError;
		sNumber = "Error";
		charSign = ' ';
		refresh();
	}

	// This methods need to override;
	public void log(String S) { // virtual

	}

	public void refresh() { // virtual

	}
}
