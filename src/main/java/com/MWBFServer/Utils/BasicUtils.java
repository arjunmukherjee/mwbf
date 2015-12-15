package com.MWBFServer.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONObject;

import com.MWBFServer.Datasource.CacheManager;
import com.MWBFServer.Datasource.SimpleCache;

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
	 * Extracts a specific field from a JSON object.
	 * @param _field
	 * @param _obj
	 * @return
	 */
	public static String extractFieldFromJson(String _field, JSONObject _obj)
	{
		return _obj.optString(_field).trim();
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
	
	/**
	 * Return a deep copy of the arrayList (using JOS)
	 * @param _collectionToCopy
	 * @return
	 */
	public static List<?> copyCollection(List<?> _collectionToCopy)
	{
		Object obj = null;
        try 
        {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(_collectionToCopy);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            obj = in.readObject();
        }
        catch(IOException e) 
        {
            e.printStackTrace();
        }
        catch(ClassNotFoundException cnfe) 
        {
            cnfe.printStackTrace();
        }
        return (List<?>) obj;
	}

}
