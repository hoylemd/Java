

public class EventLog {
	private double time;
	private String event;
	private int frame;
	private int device;
	private int size;
	
	public EventLog(double time, String event, int frame, int device, int size)
	{
		this.time = time;
		this.event = event;
		this.frame = frame;
		this.device = device;
		this.size = size;
	}
	
	double Time()
	{
		return this.time;
	}
	
	String Event()
	{
		return this.event;
	}
	
	int Frame()
	{
		return this.frame;
	}
	
	int Device()
	{
		return this.device;
	}
	
	void Reassign(int newDevice)
	{
		this.device = newDevice;
	}
	
	void Reclassify(String newEvent)
	{
		this.event = newEvent;
	}
	
	int Size()
	{
		return this.size;
	}
	
	public String toString()
	{
		return "Event Log: \"" + this.event + "\" at time " + this.time + " in Frame " + (this.frame+1) + " on Device #" + this.device;
	}
}
