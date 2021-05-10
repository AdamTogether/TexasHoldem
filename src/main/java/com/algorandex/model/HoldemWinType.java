package com.algorandex.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HoldemWinType {
	FIVE_OF_A_KIND(0), ROYAL_FLUSH(1), STRAIGHT_FLUSH(2), FOUR_OF_A_KIND(3), FULL_HOUSE(4), FLUSH(5),
	STRAIGHT(6), THREE_OF_A_KIND(7), TWO_PAIR(8), PAIR(9), HIGH_CARD(10);
	
	private Integer value;
	
	public Integer getValue() {
		return value;
	}
}
