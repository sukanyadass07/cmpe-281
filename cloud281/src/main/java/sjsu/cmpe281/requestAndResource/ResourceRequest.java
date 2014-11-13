package sjsu.cmpe281.requestAndResource;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceRequest {
 
	private int requestId;

	@JsonProperty("cpu_units")
	private int cpu_units;	
	private int memory;
	private int storage;
	private boolean isAllocated;
	
	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public int getCpu_units() {
		return cpu_units;
	}

	public void setCpu_units(int cpu_units) {
		this.cpu_units = cpu_units;
	}

	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	public int getStorage() {
		return storage;
	}

	public void setStorage(int storage) {
		this.storage = storage;
	}

	public boolean isAllocated() {
		return isAllocated;
	}

	public void setAllocated(boolean isAllocated) {
		this.isAllocated = isAllocated;
	}
	

}
