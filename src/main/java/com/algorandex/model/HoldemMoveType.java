package com.algorandex.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HoldemMoveType {
	CHECK(1), BET(2), FOLD(3);
	
	private Integer value;
	
	public Integer getValue() {
		return value;
	}
}
