/**
 * 
 */
package mktgbi.util;

/**
 * @author yingliu
 *
 */
public class IllegalConfigException extends RuntimeException {

	/**
	 * Recommended by Java SDK document.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public IllegalConfigException() {
		super();
	}

	/**
	 * @param message
	 */
	public IllegalConfigException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IllegalConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public IllegalConfigException(Throwable cause) {
		super(cause);
	}

}
