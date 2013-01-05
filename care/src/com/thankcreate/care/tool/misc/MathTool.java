package com.thankcreate.care.tool.misc;

public class MathTool {
	
	public static int getMaxValue(int[] values)
	{
		if(values == null || values.length < 0)
			return -1;
		int max = values[0];
		for(int i = 0; i < values.length; i++)
		{
			if(values[i] > max)
				max = values[i];
		}
		return max;
	}
	
	public static String getMaxLable(String[] labels, int[] values)
	{
		if(values == null || values.length < 0 || values.length != labels.length)
			return "你妹的出BUG了啊~~";
		int max = values[0];
		String label = labels[0];
		for(int i = 0; i < values.length; i++)
		{
			if(values[i] > max)
			{
				max = values[i];
				label = labels[i];
			}
		}
		return label;
	}
}
