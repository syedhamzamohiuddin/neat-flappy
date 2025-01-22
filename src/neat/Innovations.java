package neat;

import java.util.Vector;

enum innov_type
{
	new_neuron,
	new_link
}

public class Innovations 
{

	private Vector<Innovation> innovations;
	private int nextNeuronId;
	private int nextInnovationNum;

	public Innovations(Vector<LinkGene> start_genes,Vector<NeuronGene> start_neurons) {
		// TODO Auto-generated constructor stub
		nextNeuronId = 0;
		nextInnovationNum =0;
		innovations =new Vector<>();
		//add the neurons
		for(int n=0;n<start_neurons.size();n++)
		{
			innovations.add(new Innovation(start_neurons.get(n), nextInnovationNum++, nextNeuronId++));
		}
		
		//add the links
		for(LinkGene link:start_genes)
		{
			innovations.add(new Innovation(link.fromNeuron, link.toNeuron, innov_type.new_link, nextInnovationNum++));
		}

	}

	//checks to see if this innovation has already occurred. If it has it
	//returns the innovation ID. If not it returns a negative value.
	int checkInnovation(int in,int out,innov_type type)
	{
		for(Innovation inv:innovations)
		{
			if(inv.neuronIn == in && 
					inv.neuronOut == out &&
					inv.innovationType == type)
			{
				return inv.innovationId;
			}
		}
		
		return -1;
	}
	
	//creates a new innovation and returns its ID
	int createNewInnovation(int in,int out,innov_type type)
	{
		Innovation  newInnov = new Innovation(in,out,type,nextInnovationNum);
		
		if(type == innov_type.new_neuron)
		{
			newInnov.neuronId = nextNeuronId;
			nextNeuronId++;
		}
		
		innovations.add(newInnov);
		
		++nextInnovationNum;
		
		return (nextNeuronId-1);
	}
	
	  //as above but includes adding x/y position of new neuron
	int createNewInnovation(int from,int to,innov_type innnovType,neuron_type neuroType,double x,double y)
	{
		Innovation innov = new Innovation(from, to, innnovType, nextInnovationNum, neuroType, x, y);
		
		if(innnovType == innov_type.new_neuron)
		{
			innov.neuronId = nextNeuronId;
			
			++nextNeuronId;
		}
		
		innovations.add(innov);
		++nextInnovationNum;
		
		return (nextNeuronId-1);
		
	}
	
	//creates the basic neuron from the given id
	NeuronGene createNeuronFromId(int id)
	{
		NeuronGene temp = new NeuronGene(neuron_type.hidden, 0, 0, 0);
		
		for(Innovation inv:innovations)
		{
			if(inv.neuronId==id)
			{
				temp.type = inv.neuronType;
				temp.id = inv.neuronId;
				temp.splitX = inv.splitX;
				temp.splitY = inv.splitY;
				
				return temp;
			}
		}
		return temp;
	}
	
	
	void flush()
	{
		innovations.clear();
	}
	
	//accessor methods
	int getNeuronId(int invId)
	{
		return innovations.get(invId).neuronId;
	}
	
	int nextNumber(int num)
	{
		nextInnovationNum+=num;
		return nextInnovationNum;
	}
	
	


}

class Innovation{

	innov_type innovationType;

	int innovationId;

	int neuronIn;
	int neuronOut;

	int neuronId;

	neuron_type neuronType;

	double splitX,splitY;

	public Innovation(int in,int out,innov_type innv_type,int inovId) {
		// TODO Auto-generated constructor stub
		neuronIn = in;
		neuronOut = out;
		innovationType = innv_type;
		innovationId = inovId;
		neuronId = 0;
		splitX = 0;
		splitY = 0;
		neuronType = neuron_type.none;


	}

	public Innovation(NeuronGene neuron,int innovId,int neuronId){
		// TODO Auto-generated constructor stub
		innovationId = innovId;
		this.neuronId = neuronId;
		splitX = neuron.splitX;
		splitY = neuron.splitY;
		neuronType = neuron.type;
		neuronIn = -1;
		neuronOut = -1;

	}


	public Innovation(int in,int out,innov_type t,int inov_id,neuron_type type,double x,double y)
	{
		neuronIn = in;
		neuronOut = out;
		innovationType = t;
		innovationId = inov_id;
		neuronId = 0;
		neuronType = type;
		splitX = x;
		splitY = y;
	}



}