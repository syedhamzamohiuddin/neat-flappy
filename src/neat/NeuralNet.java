package neat;

import java.awt.Graphics2D;
import java.util.Vector;

public class NeuralNet 
{

	private Vector<Neuron> neurons;
	
	private int depth;
	
	public NeuralNet(Vector<Neuron> neurons,int depth) 
	{
		this.neurons=neurons;
		this.depth=depth;
	}
	
	double sigmoid(double netinput, double response)
	{
		return ( 1 / ( 1 + Math.exp(-netinput / response)));
	}
	
	
	
	public Vector<Double> update(Vector<Double> inputs, run_type type)
	{
		Vector<Double> outputs = new Vector<Double>();
		
		int flushCount=0;
		
		if(type==run_type.snapshot)
		{
			flushCount=depth;
		}
		else
		{
			flushCount=1;
		}
		
		//iterate through the network flushCount times
		for(int i=0;i<flushCount;i++)
		{
			outputs.clear();
			
			int neuron=0;
			
			
			//set input neurons to input
			while(neurons.get(neuron).type== neuron_type.input)
			{
				neurons.get(neuron).output = inputs.get(neuron);
				neuron++;
			}
			
			//bias
			neurons.get(neuron).output = 1;
			neuron++;
			
			//then step through the network a neuron at time
			while(neuron<neurons.size())
			{
				
				//sum of input X weights
				double sum=0;
				
				//sum this neuron's inputs by iterating through all the links 
				//into the neuron
				for(int lnk=0;lnk<neurons.get(neuron).linksIn.size();lnk++)
				{
					double weight = neurons.get(neuron).linksIn.get(lnk).weight;
					
					double neuronOutput = neurons.get(neuron).linksIn.get(lnk).in.output;
					
					sum+= weight * neuronOutput;
				}
				
				//now put the sum through the activation function and assign
				//the value to this neurons'output;
				neurons.get(neuron).output=sigmoid(sum, neurons.get(neuron).activationResponse);
				
				if(neurons.get(neuron).type== neuron_type.output)
				{
					outputs.add(neurons.get(neuron).output);
				}
				
				neuron++;
				
			}
			
		}//next iteration
		
		 
		if(type== run_type.snapshot)
		{
			for(Neuron n:neurons)
			{
				n.output = 0;
			}
		}
		
		
		//return the outputs
		return outputs;
	}
	
	
	void drawNet(Graphics2D g)
	{
		
	}
	public static enum run_type{
		snapshot, active
	}
}

 

class Neuron 
{

	Vector<Link> linksIn;
	
	Vector<Link> linksOut;
	
	double sumActivation;
	
	double output;
	
	neuron_type type;
	
	int neuronId;
	
	double activationResponse;
	
	int posX,posY;
	
	double splitY, splitX;
	
	public Neuron(neuron_type type,int id,double y,double x,double actResponse)
	{
		this.type=type;
		this.neuronId=id;
		this.sumActivation=0;
		this.output=0;
		this.posX=0;
		this.posY=0;
		this.splitY=y;
		this.splitX=x;
		this.activationResponse=actResponse;
		
		linksIn = new Vector<Link>();
		linksOut = new Vector<Link>();
	
	}
}

class Link
{
	Neuron in;
	Neuron out;
	
	double weight;
	
	boolean recurrent;
	
	public Link(double w,Neuron in,Neuron out,boolean recur) 
	{
		this.weight=w;
		this.in=in;
		this.out=out;
		this.recurrent=recur;
		
	}
}