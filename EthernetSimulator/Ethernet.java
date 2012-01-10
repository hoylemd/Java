
import java.util.ArrayList;

public class Ethernet {
	private int seed;
	private int devices;
	private ArrayList<Device> deviceList;
	private int collisionCount[];
	private int frames;
	private int arrivalRate;
	private double SLOT_TIME = 25.6;
	private Generator generator;
	private ArrayList<EventLog> eventList;
	private double sectionLatency;
	private double frameFinishes[];
	
	public Ethernet(int seed, int devices, int frames, int arrivalRate)
	{
		this.seed = seed;
		this.devices = devices;
		this.frames = frames;
		this.arrivalRate = arrivalRate;
		this.sectionLatency = SLOT_TIME / (this.devices - 1);
	}
	
	boolean insertEvent(EventLog event)
	{
		int i = this.eventList.size() - 1;
		
		if (this.eventList.size() == 0)
		{
			this.eventList.add(event);
			return true;
		}
		
		//System.out.println("Inserting event into list of size " + this.eventList.size() + ":\n\t" + event.toString());
		
		while(i >= 0)
		{
			if (eventList.get(i).Time() < event.Time())
				{
					this.eventList.add(i+1, event);
					return true;
				}
			else i--;
		}
		
		return false;
		
	}
	
	double Latency(int firstDevice, int secondDevice)
	{
		int sectionDist = Math.abs(firstDevice - secondDevice);
		
		return sectionDist * this.sectionLatency;
	}
	
	EventLog CableBusy(double time, int device)
	{
		int i = 0;
		EventLog focus;
		
		//System.out.println("Checking if the cable's busy (time = " + time + ", device = " + device);
		// check later devices
		//System.out.println("Checking higher devices");
		for (i = 1; i<(this.devices - device); i++ )
		{
			//System.out.println("Checking " + (device + i));
			focus = this.deviceList.get(device + i).lastEventAt(time - (sectionLatency * i));
			
			if (focus.Event().equals("Frame Started")) return focus;
			else if (focus.Event().equals("Transmission Started")) return focus;
		}
		// check earlier devices
		//System.out.println("Checking lower devices");
		for(i=0; i<device; i++)
		{
			//System.out.println("Checking " + i);
			focus = this.deviceList.get(i).lastEventAt(time - (sectionLatency * (device - i)));
			
			if (focus.Event().equals("Frame Started")) return focus;
			else if (focus.Event().equals("Transmission Started")) return focus;
		}
		return null;
	}
	
	void runMain()
	{
		int i;
		double nextFrame = 0;
		int currDevice = 0;
		double time = 0;
		double wait = 0;
		EventLog event, currentEvent, blockingEvent;
		int frame = 0;
		int currFrame = 0;
		int eventsCleared = 0;
		int dropped = 0;
		double timeframe = 0;
		double timeFree = 0;
		int frameIndex = 0;
		int throughput = 0;
		collisionCount = new int[this.frames]; // index is frame number, element is number of collisions	


		System.out.println(
			"Ethernet Simulation Initiated.\n" +
			" Seed = " + this.seed +"\n" +
			" Number of Devices = " + this.devices + "\n" +
			" Number of frames to simulate = " + this.frames + "\n" +
			" Mean # arrival frames/100 slot times = " + this.arrivalRate +
			" Mean interarrival time = " + (SLOT_TIME * 100) / this.arrivalRate + " mus\n"
		);// slot time = 25.6 mus
		
		deviceList = new ArrayList<Device>();
		eventList = new ArrayList<EventLog>();
		frameFinishes = new double[frames];
		
		for (i=0; i < this.devices; i++)
		{
			deviceList.add(new Device(i));
			
		}
		
		generator = new Generator(this.seed, this.arrivalRate);
		
		// Test the generator's exponential distribution guts
		//or (i = 0; i < 10; i++) System.out.println("exp"+i+": " + generator.nextInterarrival()+"mus");
		
		event = new EventLog(nextFrame, "Frame Arrives", frame, -1, this.generator.nextFrame());
		insertEvent(event);
		frame ++;
		//System.out.println("First frame scheduled: # events scheduled now: " + this.eventList.size());
		
		// while we still have events queued
		while ((this.eventList.size() - eventsCleared) > 0)
		{
			// pop the next event
			currentEvent = this.eventList.get(eventsCleared);
			eventsCleared++;
			
			//System.out.println("Event Popped:\n\t" + currentEvent.toString());
			
			// store the variables locally
			time = currentEvent.Time();
			currDevice = currentEvent.Device();
			currFrame = currentEvent.Frame();
			
			if (currentEvent.Event().equals("Frame Arrives"))
			{
				currDevice = randomDevice(time);
				
				if (currDevice != -1){
					System.out.println("Frame "+(currFrame+1)+" arrives at "+Math.round(time)+" mus");
					//System.out.println("currFrameB1: " + currFrame);
					timeframe = (currentEvent.Size() / 36) * SLOT_TIME;

					// New frame
					// schedule the next frame
					if (frame < this.frames)
					{
						//System.out.println("We are not done making frames.");
						nextFrame += this.generator.nextInterarrival();
						event = new EventLog(nextFrame, "Frame Arrives", currFrame + 1, -1, this.generator.nextFrame());
						insertEvent(event);
						//System.out.println("Generated new Task:\n\t" + event.toString());
						frame ++;
					}
					//System.out.println("currFrameafter: " + currFrame);
					//System.out.println("Assigning to new device");
					// assign current frame to a device
				
					//System.out.println("Assigning current frame " + currentEvent.Frame() + " to device #" + currDevice);
					// schedule frame start in 1 mus
					
					event = new EventLog(time + 1, "Frame Started", currentEvent.Frame(), currDevice, currentEvent.Size());
					insertEvent(event);
					currentEvent.Reassign(currDevice);
					this.deviceList.get(currDevice).RegisterEvent(currentEvent);
					this.deviceList.get(currDevice).RegisterEvent(event);
					System.out.format("\tFrame %d: len=%d col=%d sdr=%d sta=listen atm=%.2f trantm=%.2f\n", currFrame+1, event.Size(), this.collisionCount[currFrame], currDevice, time, frameFinishes[currFrame]);
					
				} else
				{
					//redo the missing frame;
					//System.out.println("replacing frame.");
					nextFrame = time + this.generator.nextInterarrival();
					event = new EventLog(nextFrame, "Frame Arrives", currFrame, -1, this.generator.nextFrame());
					insertEvent(event);
					//System.out.println("Generated new Task:\n\t" + event.toString());
				}
			} else if (currentEvent.Event().equals("Frame Started"))
			{
				// frame started
				// check for busy cable
				blockingEvent = CableBusy(time, currDevice);
				
				
				System.out.print("Frame "+(currFrame + 1)+" sender listens at "+Math.round( time)+" mus ... ");
				
				if (blockingEvent != null)
				{
					frameIndex = blockingEvent.Frame();
					timeFree = frameFinishes[frameIndex];
					// if it is, hold off until transmission complete + 1 mus
					currentEvent.Reclassify("Busy");
					event = new EventLog(timeFree + 1 + Latency(currDevice, blockingEvent.Device()), "Frame Started", currentEvent.Frame(), currDevice, currentEvent.Size());
					System.out.println("busy");
					// else schedule the hail
				} else
				{
					event = new EventLog(time + (SLOT_TIME*2), "Transmission Started", currentEvent.Frame(), currDevice, currentEvent.Size());
					
					System.out.println("quiet");
					//System.out.println("adding hail end at time [" + time + "]" + event.toString());
				}
				
				
				insertEvent(event);
				this.deviceList.get(currDevice).RegisterEvent(event);

			} else if (currentEvent.Event().equals("Transmission Started"))
			{
				// frame's Hail is sent
				// check if it collided
				blockingEvent= CableBusy(time, currDevice);
				

				if (blockingEvent != null)
				{
					timeFree = blockingEvent.Frame();
					// on collision, check if this is 10th
					if (this.collisionCount[currentEvent.Frame()] < 10)
					{
						// if less than 10th collision, reschedule
						currentEvent.Reclassify("Collision");
						collisionCount[currentEvent.Frame()]++;
						System.out.println("Frame "+(currFrame+1)+" sender detects "+collisionCount[currFrame]+"'th collision at "+Math.round(time)+" mus");
						wait = this.generator.backoff(collisionCount[currentEvent.Frame()]);
						System.out.println("Frame "+(currFrame+1)+" sender backs off "+Math.round(2*SLOT_TIME * (2*wait))+" mus to "+Math.round(time + (2*SLOT_TIME) * (2*wait)));
						event = new EventLog(time + (2*SLOT_TIME) * (2*wait), "Frame Started", currentEvent.Frame(), currDevice, currentEvent.Size());
						insertEvent(event);
						this.deviceList.get(currDevice).RegisterEvent(event);
					} else
					{	
						currentEvent.Reclassify("Dropped");
						dropped++;
					}
					// if no collision schedule the end of frame
				} else
				{
					System.out.println("Frame "+ (currFrame + 1)+" 72 bytes sent at "+ Math.round(time)+" mus");
					timeframe = (currentEvent.Size() / 36) * SLOT_TIME;
					//System.out.println("Frame size : " + frameSizes[currFrame] + " taking " + timeframe + " mus to transmit ");
					event = new EventLog(time + (timeframe), "Frame Completed", currentEvent.Frame(), currDevice, currentEvent.Size());
					frameFinishes[currFrame] = time + timeframe;
					insertEvent(event);
					this.deviceList.get(currDevice).RegisterEvent(event);
				}
			} else if (currentEvent.Event().equals("Frame Completed"))
			{
				System.out.println(("Frame " + currFrame+ " sending completed at " + Math.round(time) + " mus"));
				throughput += currentEvent.Size();
			}
		}
		System.out.println("\nPerformance Report:\nFrames sent Successfully = " + throughput + " bytes\nNumber of framed dropped = " + dropped + "\nNetwork throughput = " + (throughput / time) + " bps");
	}
	
	public static void main(String[] args)
	{
		if (args.length == 4 )
		{
			Ethernet core = new Ethernet(
				Integer.parseInt(args[0]),
				Integer.parseInt(args[1]),
				Integer.parseInt(args[2]),
				Integer.parseInt(args[3])
			);
			
			core.runMain();
		} else
		{
			System.out.println(
				"Incorrect arguments\n" +
				"Activate with:\n" +
				">java Ethernet seed N #frames arrival_rate\n"
			);
		}
	}
	
	private int randomDevice(double t)
	{
		double rand = 0;
		int[] available;
		int numAvail = this.devices;
		int candidate = 0;
		int i = 0, n = 0;
		
		//System.out.println("randomDevice(" + t + ")");
		
		available = new int[this.devices];
		for (i=0; i<this.devices; i++) available[i] = i;
		
		while (numAvail > 0)
		{
			// get a candidate device
			do 
			{
				rand = this.generator.next();
			} while (rand == 1);	
			
			candidate = (int)Math.floor((rand * numAvail));
			
			//System.out.println(" device " + available[candidate] + " last event: " + this.deviceList.get(available[candidate]).lastEventAt(t).toString());
			
			// check if it's busy at this time
			if (this.deviceList.get(available[candidate]).lastEventAt(t).Event().equals("Frame Completed")) return available[candidate];
			else
			{
				if ((numAvail - 1) == 0) return -1;
				
				candidate = available[candidate];
				available = new int[numAvail - 1];
				n = 0;
				
				for(i=0; i < numAvail; i++)
				{
					//System.out.println("comparing " + i + " to " +candidate + " numAvail = " + numAvail);
					if (i != candidate)
					{
						//System.out.println(i + "!=" + candidate);
						available[n] = i;
						n++;
					}
				}
				
				numAvail--;
			}
		}
		return -1;
	}
}
