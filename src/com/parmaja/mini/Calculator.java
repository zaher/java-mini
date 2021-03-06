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

	protected String cOperator = "=";
	protected String cLastOperator = " ";
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
		return charSign + sNumber;
	}

	public String getNumber() {
		return sNumber;
	}

	public String getSign() {
		return String.valueOf(charSign);
	}

	public String repeat(char c, int n) {
		char[] chars = new char[n];
		Arrays.fill(chars, c);
		return String.valueOf(chars);
	}

	public String format(double d) {
		DecimalFormat df = new DecimalFormat("#0.#");
		return df.format(d);
	}

	public double getDisplay() {
		return dDisplayNumber;
	}

	public void setDisplay(double value, boolean bKeepZeroes) {

		dDisplayNumber = value;
		int iZeroes = 0;
		int p = sNumber.indexOf('.');

		if (bKeepZeroes && p >= 0) {
			int i = sNumber.length() - 1;
			while (i > p) {
				if (sNumber.charAt(i) == '0')
					iZeroes++;
				else
					break;
			}
		}

		String s = format(value);

		if (iZeroes > 0)
			s = s + repeat('0', iZeroes);

		// Move the sign to a variable
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

	protected boolean check(boolean initZero) {
		if (calcState == CalcState.csFirst) {
			calcState = CalcState.csValid;
			dDisplayNumber = 0;
			if (initZero) 
				sNumber = "0";
			else
				sNumber = "";				
			return true;
		}
		return false;
	}

	public void process(String key) {

		String s;

		key = key.toUpperCase();

		if ((calcState == CalcState.csError) && (!key.equals("CR")))
			key = " ";
		double r = 0;
		if (bHexShown) {
			r = getDisplay();
			setDisplay(r, false);
			bHexShown = false;
			if (key.equals("H"))
				key = " ";
		}

		if (key.equals("X^Y"))
			key = "^";
		else if (key.equals("_"))
			key = "+/-";

		r = getDisplay();
		if (key.equals("ON"))
			reset();
		else if (key.equals("AC"))
			clear();
		else if (key.equals("CR")) {
			if (!check(true)) 
				setDisplay(0, true);
			calcState = CalcState.csFirst;
		} else if (key.equals("1/X")) {
			if (r == 0)
				error();
			else
				setDisplay(1 / r, false);
		} else if (key.equals("SQRT")) {
			if (r < 0)
				error();
			else
				setDisplay(Math.sqrt(r), false);
		} else if (key.equals("LOG")) {
			if (r <= 0)
				error();
			else
				setDisplay(Math.log(r), false);
		} else if (key.equals("X^2"))
			setDisplay(r * r, false);
		else if (key.equals("+/-")) {
			if (charSign == ' ')
				charSign = '-';
			else
				charSign = ' ';
			r = getDisplay();
			setDisplay(-r, true);
		} else if (key.equals("M+")) {
			dMemory = dMemory + r;
			bHaveMemory = true;
		} else if (key.equals("M-")) {
			dMemory = dMemory - r;
			bHaveMemory = true;
		} else if (key.equals("MR")) {
			check(false);
			setDisplay(dMemory, false);
		} else if (key.equals("MC")) {
			dMemory = 0;
			bHaveMemory = false;
		} else if (key.equals("DEL")) // Delete
		{
			check(true);
			if (sNumber.length() == 1)
				sNumber = "0";
			else
				sNumber = sNumber.substring(0, sNumber.length() - 2);
			setDisplay(Double.valueOf(sNumber), true);// { !!! }
		}

		else if (key.equals("00")) {
			if (sNumber.length() < iMaxDigits - 1) {
				check(false);
				if (!sNumber.equals("0")) {
					sNumber = sNumber + key;
					dDisplayNumber = Double.parseDouble(sNumber);
				}
			}
		}
		else if (key.equals("0")) {
			if (sNumber.length() < iMaxDigits) {
				check(false);
				if (!sNumber.equals("0")) {
					sNumber = sNumber + key;
					dDisplayNumber = Double.parseDouble(sNumber);
				}
			}
		}
		else if (key.compareTo("1") >= 0 && (key.compareTo("9") <= 0)) {
			if (sNumber.length() < iMaxDigits) {
				check(false);
				sNumber = sNumber + key;
				dDisplayNumber = Double.parseDouble(sNumber);
			}
		} else if (key.equals(".")) {
			check(true);
			if (sNumber.indexOf('.') < 0)
				sNumber = sNumber + '.';
		} else if (key.equals("H")) {
			r = getDisplay();
			sNumber = Long.toHexString(Math.round(r));
			bHexShown = true;
		} else { // finally else '^', '+', '-', '*', '/', '%', '='
			if (key.equals("=") && (calcState == CalcState.csFirst)) {
				// for repeat last operator
				calcState = CalcState.csValid;
				r = dLastResult;
				cOperator = cLastOperator;
			} else
				r = getDisplay();

			if (calcState == CalcState.csValid) {
				bStarted = true;
				if (cOperator.equals("="))
					s = " ";
				else
					s = String.valueOf(cOperator);

				log(s + format(r));

				calcState = CalcState.csFirst;
				cLastOperator = cOperator;
				dLastResult = r;
				if (key.equals("%")) {
					if (cOperator.equals("+")
							|| cOperator.equals("-"))
						r = dOperand * r / 100;
					else if (cOperator.equals("*")
							|| cOperator.equals("/"))
						r = r / 100;
				}

				else if (cOperator.equals("^")) {
					if ((dOperand == 0) && (r <= 0))
						error();
					else
						setDisplay(Math.pow(dOperand, r), false);
				} else if (cOperator.equals("+"))
					setDisplay(dOperand + r, false);
				else if (cOperator.equals("-"))
					setDisplay(dOperand - r, false);
				else if (cOperator.equals("*"))
					setDisplay(dOperand * r, false);
				else if (cOperator.equals("/")) {
					if (r == 0)
						error();
					else
						setDisplay(dOperand / r, false);
				}
			}
			if (key.equals("="))
				log('=' + sNumber);

			cOperator = key;
			dOperand = getDisplay();

		}

		refresh();
	}

	public void clear() {
		if (bStarted)
			log(repeat('-', iMaxDigits + 1 + iMaxDecimals));
		bStarted = false;
		calcState = CalcState.csFirst;
		sNumber = "0";
		charSign = ' ';
		cOperator = "=";
		refresh();
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
