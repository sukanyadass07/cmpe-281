package sjsu.cmpe281.requestAndResource;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestResourceStorage {
	
	public static HashMap<Integer, ResourceRequest> requestDetails = new HashMap<Integer, ResourceRequest>();

	public void addRequestsToHashMap(int requestID, ResourceRequest resourceRequest)
	{
		requestDetails.put(requestID, resourceRequest);
	}
	
	public ArrayList<Integer> getRequestsFromHashMap (){
		
		ArrayList<Integer> requestIDs = new ArrayList<Integer>();
		 
		 for(int requestID : requestDetails.keySet())
		 {
			 requestIDs.add(requestID) ;
		 }	 
		 return requestIDs;	
	}
	
	public ArrayList<ResourceRequest> getResourcesFromHashMap()
	{
		ArrayList<ResourceRequest> resourceRequestList = new ArrayList<ResourceRequest>();
		
		for(int requestID : requestDetails.keySet())
		{
			resourceRequestList.add(requestDetails.get(requestID));
		}
		
		return resourceRequestList;
	}

}
