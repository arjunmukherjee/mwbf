package com.MWBFServer.Utils;

import javax.ws.rs.core.Response;

/**
 * Wrapper class to contain basic utility methods.
 * @author arjunmuk
 *
 */
public final class BasicUtils 
{
	/**
	 * Private constructor. This class is not to be instantiated.
	 */
	private BasicUtils()
	{
        throw new IllegalStateException( "Do not instantiate this class." );
    }
	
	/**
	 * Construct a JSON formatted string to indicate success or failure of the operation 
	 * along with a message to send back to the called.
	 * @param _success
	 * @param _message
	 * @return
	 */
	public static String constructReturnString(String _success, String _message)
	{
		return "{\"success\":" + _success + ",\"message\":\"" + _message + "\"}";
	}
	
	/**
	 * Build an HttpResponse string to be sent back to the requester.
	 * @param _responseString
	 * @return
	 */
	public static Response buildResponse(String _responseString)
	{
		Response rb = Response.ok(_responseString).build();
		return rb;
	}
	
	/**
	 * Round off a double value.
	 * 
	 * @param value
	 * @param places
	 * @return
	 */
	public static double round(double value, int places) 
	{
	    if (places < 0) 
	    	throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}

}
