package net.sqlcipher.auth;

public interface SQLCipherOwner {

	public void unlockDatabase (String passcode) throws Exception;
	public void rekeyDatabase (String newpasscode) throws Exception;
	
}
