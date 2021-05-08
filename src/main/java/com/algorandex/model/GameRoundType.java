package com.algorandex.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GameRoundType {
	START(1), FLOP(2), TURN(3), RIVER(4);
	
	private Integer value;
	
	public Integer getValue() {
		return value;
	}
}
