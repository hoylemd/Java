
import java.util.ArrayList;

public class Device {
	private int index;
	ArrayList<EventLog> events;
	
	public Device(int index)
	{
		this.index = index;
		this.events = new ArrayList<EventLog>();
		
		//System.out.println("Device " + this.index + " ready.");
	}
	
	EventLog lastEventAt(double t)
	{
		int i = 0;
		EventLog last = new EventLog(0, "Frame Completed", -1, this.index, 0);
		EventLog curr;
		
		
		for (i=0; i < this.events.size(); i++){
			curr = events.get(i);
			if (curr.Time() < t)
			{
				if (curr.Time() > last.Time()) last = curr;
			}
		}
		
		return last;
	}
	
	void RegisterEvent(EventLog event)
	{
		this.events.add(event);
	}
	
	/*
	 * Listens to the ethernet to see if it's clear.
	 * 
	 * returns true on clear cable,
	 * false on collision
	 */
	
	
}
