package com.sap.sia.wechat.util;

import java.util.HashMap;
import java.util.Map;

public class MaterialMapping {
	// String for car type
	private static final String PORSCHE = "PORSCHE";
	private static final String AUDI = "AUDI";
	private static final String BMW = "BMW";
	private static final String FORD = "FORD";
	private static final String CHEVY = "CHEVY";
	
	//String for car color
	private static final String RED = "RED";
	private static final String YELLOW = "YEL";
	private static final String SLIVER = "SLV";
	private static final String WHITE = "WHT";
	private static final String BLACK = "BLK";
	private static final String GOLD = "GLD";
	private static final String ORANGE = "ORG";
	
	private Map<Integer, String> typeMap = new HashMap<Integer, String>();
	private Map<Integer, String> colorMap = new HashMap<Integer, String>();
	
	
	public MaterialMapping(){
		//set car type
		typeMap.put(1, AUDI);
		typeMap.put(2, BMW);
		typeMap.put(3, PORSCHE);
		typeMap.put(4, FORD);
		typeMap.put(5, CHEVY);
		//set car color
		colorMap.put(1, WHITE);
		colorMap.put(2, ORANGE);
		colorMap.put(3, YELLOW);
		colorMap.put(4, BLACK);
		colorMap.put(5, RED);
		colorMap.put(6, GOLD);
		colorMap.put(7, SLIVER);
	}

	public Map<Integer, String> getTypeMap() {
		return typeMap;
	}

	public Map<Integer, String> getColorMap() {
		return colorMap;
	}
}
