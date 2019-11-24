package com.utils.bloomFilter.filter;

import cn.hutool.core.util.HashUtil;

public class ELFFilter extends AbstractFilter {
	private static final long serialVersionUID = 1L;

	public ELFFilter(long maxValue, int MACHINENUM) {
		super(maxValue, MACHINENUM);
	}
	
	public ELFFilter(long maxValue) {
		super(maxValue);
	}
	
	@Override
	public long hash(String str) {
		return HashUtil.elfHash(str) % size;
	}

}