

import java.util.Random;

public class Generator {
	private Random randGen;
	private double lambda;
	private double SLOT_TIME = 25.6;
	private int arrivalRate;
	
	public Generator(int seed, int arrivalRate )
	{
		
		this.lambda = (arrivalRate);
		
		this.arrivalRate = arrivalRate;
		
		if (seed != 0)
		{
			this.randGen = new Random(seed);
			//System.out.println("Generator initialized with seed "+ seed + " and lambda " + this.lambda + " and aR " + this.arrivalRate + ".");
			
		} else
		{
			this.randGen = new Random();
			//System.out.println("Generator initialized with random seed.");
		}
		
	}
	
	private double exponential()
	{
		double u = randGen.nextDouble();
		double ret = (-Math.log(u)) / (this.lambda);
		//System.out.println("Exponential random: " + ret + " u = " + u + " lambda = " + this.lambda);
		return ret;
		
	}
	
	double next()
	{
		return randGen.nextDouble();
	}
	
	double nextInterarrival()
	{
		//System.out.println("lambda?" + (100*SLOT_TIME) / this.arrivalRate);
		return this.exponential() * ((100*SLOT_TIME) / this.arrivalRate);
	}
	
	int nextFrame()
	{
		double rand = this.next();
		int frame = (int)((rand * 1454) + 72);
		
		return frame;
	}
	
	double backoff(int m)
	{
		double rand = this.next();
		//System.out.println("-----RAND: "+rand + " m: " + m + " " + ((Math.pow(2, (double)m)) -1 + " " + (rand * (Math.pow(2,(double)m) - 1) ) + " projected backoff: " + (2*SLOT_TIME * (2*(rand * (Math.pow(2,(double)m) - 1) )))));
		double k = (rand * (Math.pow(2,(m) - 1)));
		
		return k;
	}
}
