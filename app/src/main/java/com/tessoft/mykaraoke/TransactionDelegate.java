package com.tessoft.mykaraoke;

public interface TransactionDelegate {

	public void doPostTransaction(int requestCode, Object result);
}
