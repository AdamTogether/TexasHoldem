package com.algorandex.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TicTacToe {
	X(1), O(2);
	
	private Integer value;
	
//	private TicTacToe(Integer value) {
//		this.value = value;
//	}
	
	public Integer getValue() {
		return value;
	}
}
