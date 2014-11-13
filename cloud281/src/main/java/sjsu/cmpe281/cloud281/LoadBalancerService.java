package sjsu.cmpe281.cloud281;

import static spark.Spark.get;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import freemarker.template.Configuration;
import freemarker.template.Template;
import sjsu.cmpe281.requestAndResource.RequestResourceStorage;
import sjsu.cmpe281.requestAndResource.ResourceRequest;
import sjsu.cmpe281.requestAndResource.ResourceStorage;
import sjsu.cmpe281.requestAndResource.Resources;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

public class LoadBalancerService {

    public static void main(String[] args) throws IOException{
    	
    	
    	ResourceStorage resourceStorage = new ResourceStorage();
		RequestResourceStorage requestResourceStorage = new RequestResourceStorage();
    	Random rand = new Random();
    	int count = 6;
    	
    	// Populating the Back-end Resource Store 
    	for(int i=0 ;i < count/2; i++){
    	
    		String resourceName = "EC2_"+ i;
        	Resources resources = new Resources();
    		resources.setResourceName(resourceName);
    		resources.setCpu_units(rand.nextInt(10));
    		resources.setMemory(rand.nextInt(100));
    		resources.setStorage(rand.nextInt(200));
    		resources.setFullAllocation(false);
    		resources.setPartialAllocation(false);
    		
    		resourceStorage.addResourceToHashMap(resourceName, resources);   		
    	}
    	
    	// Web Service Generating 1000 Requests with REQUEST-ID
        get("/request", (request, response) -> {
        	
        	/*Configuration cfg = new Configuration();
        	try
        	{
        		cfg.setDirectoryForTemplateLoading(new File("templates"));
        		Template template = cfg.getTemplate("requests.ftl"); 
        		Map<String, Object> input = new HashMap<String, Object>();   
        	}
        	catch(Exception ex)
        	{
        		
        	}*/
        	
    		int requestID;
    		String heading = "********************Generated 1000 RequestIDs are as follows**********************";
    		String template = heading + System.lineSeparator();
    		
        	for(int i=0; i< count; i++)
        	{
        		requestID = rand.nextInt(Integer.MAX_VALUE);
        		ResourceRequest resourceRequest =new ResourceRequest();
        		resourceRequest.setRequestId(requestID);
        		resourceRequest.setMemory(rand.nextInt(100));
        		resourceRequest.setCpu_units(rand.nextInt(10));
        		resourceRequest.setStorage(rand.nextInt(200));
        		resourceRequest.setAllocated(false);
        		
        		requestResourceStorage.addRequestsToHashMap(requestID, resourceRequest);			

        	}
        	
            return template + requestResourceStorage.getRequestsFromHashMap();
        });

        
        
        // Resource Allocation is done by Ant Colony Algorithm
        get("/antColonyAllocation", (request, response) -> {
        	
        	ArrayList<Resources> resourceList = new ArrayList<Resources>(resourceStorage.getResourcesFromHashMap());
        	ArrayList<ResourceRequest> requestList = new ArrayList<ResourceRequest>(requestResourceStorage.getResourcesFromHashMap());
        	ArrayList<String> resourceNames = new ArrayList<String> ();
       	
        	resourceList = resourceStorage.getResourcesFromHashMap();
        	requestList = requestResourceStorage.getResourcesFromHashMap();
        	
        	for(int j=0; j< resourceList.size(); j++ )
        	{
        		String requestName = resourceList.get(j).getResourceName(); 
        		int cpu = resourceList.get(j).getCpu_units();
        		int memory = resourceList.get(j).getMemory();
        		int storage = resourceList.get(j).getStorage();
        		System.out.println("**********************************************************");
        		System.out.println("### Resources Before Allocation ###");
        		System.out.println("Resource Name: "+requestName);
        		System.out.println("Resource CPU units: "+cpu);
        		System.out.println("Resource Memory units: "+memory);
        		System.out.println("Resource Storage units: "+storage);
        		System.out.println("Full Allocation Flag: " + resourceList.get(j).isFullAllocation());
        		System.out.println("Partial Allocation Flag: " + resourceList.get(j).isPartialAllocation());
        		System.out.println("**********************************************************");
        	}
        	System.out.println("#####################################################################################################");
        	for(int j=0; j<requestList.size(); j++ )
        	{
        		int requestID = requestList.get(j).getRequestId();
        		int cpu = requestList.get(j).getCpu_units();
        		int memory = requestList.get(j).getMemory();
        		int storage = requestList.get(j).getStorage();
        		System.out.println("**********************************************************");
        		System.out.println("Request ID: "+requestID);
        		System.out.println("Request CPU units: "+cpu);
        		System.out.println("Request Memory units: "+memory);
        		System.out.println("Request Storage units: "+storage);
        		System.out.println("Request Is Allocated? " +requestList.get(j).isAllocated());
        		System.out.println("**********************************************************");
        	}
        	
        	
        	for (int i= 0; i<count; i++)
        	{
        		int Request_CPU_units = requestList.get(i).getCpu_units();
				int Request_Memory = requestList.get(i).getMemory();
				int Request_Storage = requestList.get(i).getStorage();
				int requestServed = 0;
        		
        		while((requestList.get(i).isAllocated() == false) && requestServed==0)
        		{
        			for(int j = 0; j<count/2; j++)
        			{
        				int Resource_CPU_units = resourceList.get(j).getCpu_units();
                		int Resoure_Memory = resourceList.get(j).getMemory();
                		int Resource_Storage = resourceList.get(j).getStorage();
                		
        				
        				if(resourceList.get(j).isFullAllocation() == false)
        				{
        					if((Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units)) > 0)
        					{
        						if(((Math.abs(Resoure_Memory)) - Math.abs(Request_Memory)) > 0 )
        						{
        							
        							if((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) > 0)
        							{
        								Resource_CPU_units = Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units);
        								Resoure_Memory = (Math.abs(Resoure_Memory)) - Math.abs(Request_Memory);
        								Resource_Storage = Math.abs(Resource_Storage) - Math.abs(Request_Storage);
        								
        								// Update the resource List
        								resourceList.get(j).setCpu_units(Resource_CPU_units);
        								resourceList.get(j).setMemory(Resoure_Memory);
        								resourceList.get(j).setStorage(Resource_Storage);
        								
        								resourceList.get(j).setFullAllocation(false);
        								resourceList.get(j).setPartialAllocation(true);
        								
        								// Set the request allocated to true
        								requestList.get(i).setAllocated(true); 
        								
        							}
        							else
        							{
        								if((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) == 0)
        								{
        									Resource_CPU_units = Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units);
            								Resoure_Memory = (Math.abs(Resoure_Memory)) - Math.abs(Request_Memory);
            								Resource_Storage = Math.abs(Resource_Storage) - Math.abs(Request_Storage);
            								
            								// Update the resource List
            								resourceList.get(j).setCpu_units(Resource_CPU_units);
            								resourceList.get(j).setMemory(Resoure_Memory);
            								resourceList.get(j).setStorage(Resource_Storage);
            								
            								resourceList.get(j).setFullAllocation(true);
            								resourceList.get(j).setPartialAllocation(false);
            								
            								// Set the request allocated to true
            								requestList.get(i).setAllocated(true); 
        								}
        								
        							}
        							if((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) < 0)
        							{
        								break;
        							}
        							
        						}
        						else
        						{
        							if(((Math.abs(Resoure_Memory)) - Math.abs(Request_Memory)) == 0 )
        							{

            							if((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) > 0)
            							{
            								Resource_CPU_units = Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units);
            								Resoure_Memory = (Math.abs(Resoure_Memory)) - Math.abs(Request_Memory);
            								Resource_Storage = Math.abs(Resource_Storage) - Math.abs(Request_Storage);
            								
            								// Update the resource List
            								resourceList.get(j).setCpu_units(Resource_CPU_units);
            								resourceList.get(j).setMemory(Resoure_Memory);
            								resourceList.get(j).setStorage(Resource_Storage);
            								
            								resourceList.get(j).setFullAllocation(true);
            								resourceList.get(j).setPartialAllocation(false);
            								
            								// Set the request allocated to true
            								requestList.get(i).setAllocated(true); 
            								
            							}
            							else
            							{
            								if((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) == 0)
            								{
            									Resource_CPU_units = Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units);
                								Resoure_Memory = (Math.abs(Resoure_Memory)) - Math.abs(Request_Memory);
                								Resource_Storage = Math.abs(Resource_Storage) - Math.abs(Request_Storage);
                								
                								// Update the resource List
                								resourceList.get(j).setCpu_units(Resource_CPU_units);
                								resourceList.get(j).setMemory(Resoure_Memory);
                								resourceList.get(j).setStorage(Resource_Storage);
                								
                								resourceList.get(j).setFullAllocation(true);
                								resourceList.get(j).setPartialAllocation(false);
                								
                								// Set the request allocated to true
                								requestList.get(i).setAllocated(true); 
            								}
            								
            							}
            							if((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) < 0)
            							{
            								continue;
            							}				
        								
        							}
        						}
        					}
        					else
        					{
        						if((Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units)) == 0)
        						{
            						if(((Math.abs(Resoure_Memory)) - Math.abs(Request_Memory)) > 0 )
            						{
            							
            							if((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) > 0)
            							{
            								Resource_CPU_units = Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units);
            								Resoure_Memory = (Math.abs(Resoure_Memory)) - Math.abs(Request_Memory);
            								Resource_Storage = Math.abs(Resource_Storage) - Math.abs(Request_Storage);
            								
            								// Update the resource List
            								resourceList.get(j).setCpu_units(Resource_CPU_units);
            								resourceList.get(j).setMemory(Resoure_Memory);
            								resourceList.get(j).setStorage(Resource_Storage);
            								
            								resourceList.get(j).setFullAllocation(true);
            								resourceList.get(j).setPartialAllocation(false);
            								
            								// Set the request allocated to true
            								requestList.get(i).setAllocated(true); 
            								
            							}
            							else
            							{
            								if((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) == 0)
            								{
            									Resource_CPU_units = Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units);
                								Resoure_Memory = (Math.abs(Resoure_Memory)) - Math.abs(Request_Memory);
                								Resource_Storage = Math.abs(Resource_Storage) - Math.abs(Request_Storage);
                								
                								// Update the resource List
                								resourceList.get(j).setCpu_units(Resource_CPU_units);
                								resourceList.get(j).setMemory(Resoure_Memory);
                								resourceList.get(j).setStorage(Resource_Storage);
                								
                								resourceList.get(j).setFullAllocation(true);
                								resourceList.get(j).setPartialAllocation(false);
                								
                								// Set the request allocated to true
                								requestList.get(i).setAllocated(true); 
            								}
            								
            							}
            							if((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) < 0)
            							{
            								continue;
            							}
            							
            						}
            						else
            						{
            							if(((Math.abs(Resoure_Memory)) - Math.abs(Request_Memory)) == 0 )
            							{

                							if((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) > 0)
                							{
                								Resource_CPU_units = Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units);
                								Resoure_Memory = (Math.abs(Resoure_Memory)) - Math.abs(Request_Memory);
                								Resource_Storage = Math.abs(Resource_Storage) - Math.abs(Request_Storage);
                								
                								// Update the resource List
                								resourceList.get(j).setCpu_units(Resource_CPU_units);
                								resourceList.get(j).setMemory(Resoure_Memory);
                								resourceList.get(j).setStorage(Resource_Storage);
                								
                								resourceList.get(j).setFullAllocation(true);
                								resourceList.get(j).setPartialAllocation(false);
                								
                								// Set the request allocated to true
                								requestList.get(i).setAllocated(true); 
                								
                							}
                							else
                							{
                								if((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) == 0)
                								{
                									Resource_CPU_units = Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units);
                    								Resoure_Memory = (Math.abs(Resoure_Memory)) - Math.abs(Request_Memory);
                    								Resource_Storage = Math.abs(Resource_Storage) - Math.abs(Request_Storage);
                    								
                    								// Update the resource List
                    								resourceList.get(j).setCpu_units(Resource_CPU_units);
                    								resourceList.get(j).setMemory(Resoure_Memory);
                    								resourceList.get(j).setStorage(Resource_Storage);
                    								
                    								resourceList.get(j).setFullAllocation(true);
                    								resourceList.get(j).setPartialAllocation(false);
                    								
                    								// Set the request allocated to true
                    								requestList.get(i).setAllocated(true); 
                								}
                								
                							}
                							if((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) < 0)
                							{
                								continue;
                							}				
            								
            							}
            						}
        						}
        					}
        					
        					if((Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units)) < 0)
        					{
        						continue;
        					}
        					
        				}
        				else
        				{
        					System.out.println("Insufficient Resource for allocation");
        				}
        			}
        			if(requestList.get(i).isAllocated()==true)
        			{
        				System.out.println("******************************************************************");
        				System.out.println("Request ID: "+requestList.get(i).getRequestId() +" is Allocated");
        				System.out.println("******************************************************************");
        			}
        			else
        			{
        				requestServed = requestServed + 1;
        			}
        		}
        	}       	
        	
        	for(int j=0; j< resourceList.size(); j++ )
        	{
        		String requestName = resourceList.get(j).getResourceName(); 
        		int cpu = resourceList.get(j).getCpu_units();
        		int memory = resourceList.get(j).getMemory();
        		int storage = resourceList.get(j).getStorage();
        		System.out.println("**********************************************************");
        		System.out.println("### Resources After Allocation ###");
        		System.out.println("Resource Name: "+requestName);
        		System.out.println("Resource CPU units: "+cpu);
        		System.out.println("Resource Memory units: "+memory);
        		System.out.println("Resource Storage units: "+storage);
        		System.out.println("Full Allocation Flag: " + resourceList.get(j).isFullAllocation());
        		System.out.println("Partial Allocation Flag: " + resourceList.get(j).isPartialAllocation());
        		System.out.println("**********************************************************");
        	}
        	
        	resourceNames = resourceStorage.updateResourcesInHashMap(resourceList);
        	
        	 return "**********Following Resources Allocated*********" + resourceNames;
        	
        });
        
        get("/billing", (request, response) ->{
        	
        	final double costOfCpuUnints = 0.30;
        	final double costOfMemoryUnits = 0.50;
        	final double costOfStorageUnits = 0.70;
        	
        	double totalCostOfCpuUnits = 0.0;
        	double totalCostOfMemoryUnits =0.0;
        	double totalCostOfStorageUnits = 0.0;
        	double totalBill = 0.0;
        	double sumOfTotalBills = 0.0;
        	
        	Map<String, Object> billing = new HashMap<>();
        	
        	System.out.println("**********************************************************");
        	System.out.println("The cost of CPU units: $"+costOfCpuUnints+"/units");
        	System.out.println("The cost of Memory units: $"+costOfMemoryUnits+"/units");
        	System.out.println("The cost of Storage units: $"+costOfStorageUnits+"/units");
        	System.out.println("**********************************************************");
        	
        	ArrayList<Resources> allocatedResources = new ArrayList<Resources>();
        	allocatedResources = resourceStorage.getAllocatedResourcesFromHashMap();
        	
        	if(allocatedResources.isEmpty()==false)
        	{
        	
	        	for(int i= 0; i< allocatedResources.size(); i++)
	        	{
	        		if((allocatedResources.get(i).isFullAllocation() == true) || (allocatedResources.get(i).isPartialAllocation()== true))
	        		{
	        			totalCostOfCpuUnits = Math.abs(allocatedResources.get(i).getCpu_units())* costOfCpuUnints;
	        			totalCostOfMemoryUnits = Math.abs(allocatedResources.get(i).getMemory()) * costOfMemoryUnits;
	        			totalCostOfStorageUnits = Math.abs(allocatedResources.get(i).getStorage()) * costOfStorageUnits;
	        			totalBill = totalCostOfCpuUnits + totalCostOfMemoryUnits + totalCostOfStorageUnits;	
	        			
	        			System.out.println("*****************************************************************");
	            		System.out.println("Allocated Resource Name: " +allocatedResources.get(i).getResourceName());
	            		System.out.println("Cost of allocated CPU Units: "+totalCostOfCpuUnits);
	            		System.out.println("Cost of allocated Memory Units: "+totalCostOfMemoryUnits);
	            		System.out.println("Cost of allocated Storage Units: "+totalCostOfStorageUnits);
	            		System.out.println("Total Bill for the allocated Resources: "+totalBill);
	            		System.out.println("*****************************************************************");
	            		
	            		billing.put("message","Billing of Allocated Resources");
	            		billing.put("Allocated Resource Name -- ", allocatedResources.get(i).getResourceName());
	            		billing.put("Cost of allocated CPU Units -- ", totalCostOfCpuUnits);
	            		billing.put("Cost of allocated Memory Units -- ", totalCostOfMemoryUnits);
	            		billing.put("Cost of allocated Storage Units -- ", totalCostOfStorageUnits);
	            		billing.put("Total Bill for the allocated Resources --", totalBill);
	            		
	        		}
	        		sumOfTotalBills = totalBill + sumOfTotalBills;
	        	}
        	}
        	else
        	{
        		billing.put("message","Due to insufficient resource, no requests are fullfilled");
        		
        	}
        	
        	return "Total Cost of the Resources: $" +sumOfTotalBills;
        
        });
        
       /* get("/hello", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("message", "Hello World!");

            // The hello.ftl file is located in directory:
            // src/test/resources/spark/template/freemarker
            return new ModelAndView(attributes, "hello.ftl");
        }, new FreeMarkerEngine());*/

    }

}