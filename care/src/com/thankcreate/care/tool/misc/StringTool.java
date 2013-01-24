package com.thankcreate.care.tool.misc;

import android.R.string;
import net.sourceforge.pinyin4j.PinyinHelper;  
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;  
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;  
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;  
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;  

public class StringTool {
	
	public static Boolean isNullOrEmpty(String input)
	{
		return (input == null || input.equalsIgnoreCase(""));
	}
	
	public static String converterToFirstSpell(String chines) {
		String pinyinName = "";
		char[] nameChar = chines.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		for (int i = 0; i < nameChar.length; i++) {
			if (nameChar[i] > 128) {
				try {
					pinyinName += PinyinHelper.toHanyuPinyinStringArray(
							nameChar[i], defaultFormat)[0].charAt(0);
					return pinyinName;
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			} else {
				pinyinName += nameChar[i];
			}
		}
		return pinyinName;
	}
	
	public static int getSig(String input)
	{
		if(input == null)
			return 0;
		int herSig = 0;
		for(int i = 0; i < input.length(); i++)
		{
			char cIter = input.charAt(i);
			herSig += (int)cIter;
		}
		return herSig;
	}
	
	public static String getFileName(String input)
	{
		if(input == null)
			return "";
		String res = "";
		try {
			res = input.substring(input.lastIndexOf("/") + 1);
		} catch (Exception e) {
			res = "";
		}
		return res;
	}
	
	
}
