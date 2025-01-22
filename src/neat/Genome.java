package neat;

import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;
import java.util.Vector;

public class Genome implements Comparable<Genome>
{

	//its identification number
	private int                     genomeID;

	//all the neurons which make up this genome
	private Vector<NeuronGene>     neurons;

	//and all the the links
	private Vector<LinkGene>       links;

	//pointer to its phenotype
	private NeuralNet           phenotype;

	//its raw fitness score
	private double                  fitness;

	//its fitness score after it has been placed into a 
	//species and adjusted accordingly
	private double                  adjustedFitness;

	//the number of offspring this individual is required to spawn
	//for the next generation
	private double                  amountToSpawn;

	//keep a record of the number of inputs and outputs
	private int                     numInputs,
	numOutputs;

	//keeps a track of which species this genome is in (only used
	//for display purposes)
	private int                     species;

	private Random rand;
	//returns true if the specified link is already part of the genome
	private  boolean    duplicateLink(int NeuronIn, int NeuronOut)
	{

		for (int cGene = 0; cGene < links.size(); ++cGene)
		{
			if ((links.get(cGene).fromNeuron == NeuronIn) && 
					(links.get(cGene).toNeuron == NeuronOut))
			{
				//we already have this link
				return true;
			}
		}

		return false;
	}

	//given a neuron id this function just finds its position in 
	//m_vecNeurons
	private  int     getElementPos(int neuron_id) 
	{

		for (int i=0; i<neurons.size(); i++)
		{
			if (neurons.get(i).id == neuron_id)
			{
				return i;
			}
		}


		return -1;
	}

	//tests if the passed ID is the same as any existing neuron IDs. Used
	//in AddNeuron
	private  boolean    alreadyHaveThisNeuronID( int ID) 
	{

		for (int n=0; n<neurons.size(); ++n)
		{
			if (ID == neurons.get(n).id)
			{
				return true;
			}
		}

		return false;

	}


	//
	Genome()
	{
		phenotype=null;
		genomeID=0;
		fitness=0;
		adjustedFitness=0;
		numInputs=0;
		numOutputs=0;
		amountToSpawn=0;
		rand = new Random();
		neurons = new Vector<>();
		links = new Vector<>();
	}

	//-----------------------------constructor--------------------------------
	//	this constructor creates a minimal genome where there are output +
	//	input neurons and each input neuron is connected to each output neuron.
	//------------------------------------------------------------------------
	Genome(int id, int inputs, int outputs)
	{
		this();
		this.genomeID=id;
		this.numInputs=inputs;
		this.numOutputs=outputs;

		//create the input neurons
		double inputRowSlice = 1.0/(inputs+2);

		for(int i=0;i<inputs;i++)
		{
			neurons.add(new NeuronGene(neuron_type.input, i, 0, (i+2)*inputRowSlice));
		}

		//create bias
		neurons.add(new NeuronGene(neuron_type.bias, inputs, 0, inputRowSlice));

		//create the output neurons
		double outputRowSlice = 1.0/(outputs+1);
		for(int i=0;i<outputs;i++)
		{
			neurons.add(new NeuronGene(neuron_type.output, inputs+1+i, 1, (i+1)*outputRowSlice));
		}

		//create the link genes, connect each input neuron to each output neuron and 
		//assign a random weight -1 < w < 1

		for (int i=0; i<inputs+1; i++)
		{
			for (int j=0; j<outputs; j++)
			{
				links.add(new LinkGene(neurons.get(i).id,
						neurons.get(inputs+j+1).id,
						true,
						inputs+outputs+1+NumGenes(),
						randomClamped()));
			}
		}

	}

	//complete constructor
	Genome(int id,Vector<NeuronGene> neurons,Vector<LinkGene> genes,int inputs,int outputs)
	{

		phenotype=null;
		genomeID=id;
		fitness=0;
		adjustedFitness=0;
		numInputs=inputs;
		numOutputs=outputs;
		amountToSpawn=0;
		this.neurons=neurons;
		this.links=genes;

		rand = new Random();
 	}

	//copy constructor
	Genome(Genome g)
	{
		genomeID   = g.genomeID;
		neurons   = g.neurons;
		links   = g.links;
		phenotype = null;              //no need to perform a deep copy
		fitness   = g.fitness;
		adjustedFitness = g.adjustedFitness;
		numInputs  = g.numInputs;
		numOutputs = g.numOutputs;
		amountToSpawn = g.amountToSpawn;

		rand = new Random();
	}


	//-------------------------------CreatePhenotype--------------------------
	//
	//		Creates a neural network based upon the information in the genome.
	//		Returns a pointer to the newly created ANN
	//------------------------------------------------------------------------

	NeuralNet createPhenotype(int depth)
	{


		//first make sure there is no existing phenotype for this genome
		deletePhenotype();

		//this will hold all the neurons required for the phenotype
		Vector<Neuron>  phenoNeurons=new Vector<Neuron>();

		//first, create all the required neurons
		for (int i=0; i<neurons.size(); i++)
		{
			Neuron pNeuron = new Neuron(neurons.get(i).type,
					neurons.get(i).id,
					neurons.get(i).splitY,
					neurons.get(i).splitX,
					neurons.get(i).activationResponse);

			phenoNeurons.add(pNeuron);
		}

		//now to create the links. 
		for (int cGene=0; cGene<links.size(); cGene++)
		{
			//make sure the link gene is enabled before the connection is created
			if (links.get(cGene).enabled)
			{
				//get the pointers to the relevant neurons
				int element         = getElementPos(links.get(cGene).fromNeuron);
				Neuron fromNeuron = phenoNeurons.get(element);

				element           = getElementPos(links.get(cGene).toNeuron);
				Neuron toNeuron = phenoNeurons.get(element);

				//create a link between those two neurons and assign the weight stored
				//in the gene
				Link tmpLink= new Link(links.get(cGene).weight,
						fromNeuron,
						toNeuron,
						links.get(cGene).recurrent);

				//add new links to neuron
				fromNeuron.linksOut.add(tmpLink);
				toNeuron.linksIn.add(tmpLink);
			}
		}

		//now the neurons contain all the connectivity information, a neural
		//network may be created from them.
		phenotype = new NeuralNet(phenoNeurons, depth);

		return phenotype;

	}

	void deletePhenotype()
	{

		phenotype=null;
	}

	void addLink(double mutationRate,double recurrentChance,Innovations innovation,int numTrysToFindLoop, int numTrysToAddLink)
	{

		if(Math.random() > mutationRate) return;

		int neuron1Id = -1;
		int neuron2Id = -1;

		boolean recurrent = false;

		if(Math.random() < recurrentChance)
		{

			while(numTrysToFindLoop--!=0)
			{
				int neuronPos = numInputs+1+rand.nextInt((neurons.size()-numInputs-1));

				if(!neurons.get(neuronPos).recurrent &&
						neurons.get(neuronPos).type!=neuron_type.bias&&
						neurons.get(neuronPos).type!=neuron_type.input)
				{
					neuron1Id = neuron2Id =neurons.get(neuronPos).id;

					neurons.get(neuronPos).recurrent = true;

					numTrysToFindLoop =0;

				}
			}
		}

		else
		{
			while(numTrysToAddLink--!=0)
			{
				neuron1Id = neurons.get(rand.nextInt(neurons.size())).id;

				neuron2Id = neurons.get(numInputs+1+rand.nextInt((neurons.size()-numInputs-1))).id;

				if(neuron2Id == 2)
				{
					continue;
				}

				//make sure these two are not already linked and that 
				//they are not the same
				if(!duplicateLink(neuron1Id, neuron2Id)&& neuron1Id!=neuron2Id)
				{
					numTrysToAddLink = 0;
				}
				else 
				{
					neuron1Id =-1;
					neuron2Id = -1;
				}
			}

		}


		//return if unsuccessful in finding a link
		if ( (neuron1Id < 0) || (neuron2Id < 0) )
		{
			return;
		}
		
		int id = innovation.checkInnovation(neuron1Id, neuron2Id, innov_type.new_link);
		
		if(neurons.get(getElementPos(neuron1Id)).splitY>neurons.get(getElementPos(neuron2Id)).splitY)
		{
			recurrent = true;
		}
		
		if(id<0)
		{
			innovation.createNewInnovation(neuron1Id, neuron2Id, innov_type.new_link);
			
			int inn_id = innovation.nextNumber(0) - 1;
			
			links.add(new LinkGene(neuron1Id,neuron2Id,true,inn_id,randomClamped(),recurrent));
			
		}
		else
		{
			links.add(new LinkGene(neuron1Id,neuron2Id,true,id,randomClamped(),recurrent));
			
		}
		
		return;
		


	}

	void addNeuron(double mutationRate,Innovations innovation,int numTrysToFindOldLink)
	{

		if(Math.random()> mutationRate) return;
		
		boolean done = false;
		
		int chosenLink = 0;
		
		final int sizeThreshold = numInputs+numOutputs +5;
		if(links.size() < sizeThreshold)
		{
			while(numTrysToFindOldLink--!=0)
			{
				chosenLink = rand.nextInt(NumGenes()-1-(int)Math.sqrt(NumGenes()));
				int fromNeuron = links.get(chosenLink).fromNeuron;
				
				if(links.get(chosenLink).enabled && !links.get(chosenLink).recurrent && neurons.get(getElementPos(fromNeuron)).type!=neuron_type.bias)
				{
					done=true;
					
					numTrysToFindOldLink = 0;
				}
				
			}
			
			if (!done)
		    {
		      //failed to find a decent link
		      return;
		    }
		}
		
		else
		{
			while(!done)
			{
				chosenLink = rand.nextInt(NumGenes());
				
				int fromNeuron = links.get(chosenLink).fromNeuron;
				
				if(links.get(chosenLink).enabled && !links.get(chosenLink).recurrent && neurons.get(getElementPos(fromNeuron)).type!=neuron_type.bias)
				{
					done=true;
					
					 
				}
			}
		}
		
		links.get(chosenLink).enabled = false;
		
		double originalWeight = links.get(chosenLink).weight;
		
		int from = links.get(chosenLink).fromNeuron;
		int to   =links.get(chosenLink).toNeuron;
		
		
		double newDepth = (neurons.get(getElementPos(from)).splitY +neurons.get(getElementPos(to)).splitY )/2;
				
		double newWidth = (neurons.get(getElementPos(from)).splitX + neurons.get(getElementPos(to)).splitX)/2;
		
		int id = innovation.checkInnovation(from, to, innov_type.new_neuron);
		
		if(id >=0)
		{
			int neuronId = innovation.getNeuronId(id);
			
			if(alreadyHaveThisNeuronID(neuronId))
			{
				id = -1;
			}
		}
		
		if(id < 0)
		{
			int newNeuronId = innovation.createNewInnovation(from, to, innov_type.new_neuron, neuron_type.hidden, newWidth, newDepth);
			
			neurons.add(new NeuronGene(neuron_type.hidden, newNeuronId, newDepth, newWidth));
			System.out.println("neuron added yaayy 11!!!");

		
			//add links
			//1st link
			int idLink1 = innovation.nextNumber(0);
			
			innovation.createNewInnovation(from, newNeuronId, innov_type.new_link);
			links.add(new LinkGene(from, newNeuronId, true, idLink1,1));
			
			//2nd link
			int idLink2 = innovation.nextNumber(0);
			
			innovation.createNewInnovation(newNeuronId, to, innov_type.new_link);
			
			links.add(new LinkGene(newNeuronId, to, true, idLink2, originalWeight));

			
		}
		else
		{
			int newNeuronId = innovation.getNeuronId(to);
			
			int idLink1 = innovation.checkInnovation(from, newNeuronId, innov_type.new_link);
			
			int idLink2 = innovation.checkInnovation(newNeuronId, to, innov_type.new_link);
			
			if ( (idLink1 < 0) || (idLink2 < 0) )
		    {
		    
					System.out.println("Error in CGenome::AddNeuron Problem!");
		      return;
		    }
			
			links.add(new LinkGene(from, newNeuronId, true, idLink1, 1));
			links.add(new LinkGene(newNeuronId,to,true,idLink2,originalWeight));
			
			
			neurons.add(new NeuronGene(neuron_type.hidden, newNeuronId, newDepth, newWidth));
			System.out.println("neuron added yaayy 22!!!");
			
		}
		
		 
	}

	void mutateWeights(double mutationRate,double probNewMut,double maxPertubation)
	{

		for(LinkGene link:links)
		{
			if(Math.random() < mutationRate)
			{
				
				if(Math.random() < probNewMut)
				{
					link.weight = randomClamped();
				}
				else
				{
					link.weight+=randomClamped()*maxPertubation;
				}
			}
		}
		
		return;
	}

	void mutateActivationResponse(double mutationRate,double maxPertubation)
	{

		for(NeuronGene neuron:neurons)
		{
			if(Math.random()< mutationRate)
			neuron.activationResponse+=randomClamped()*maxPertubation;
		}
	}

	public double randomClamped()
	{
		return (Math.random()-Math.random());
	}
	
	
	//------------------------- GetCompatibilityScore ------------------------
	//
	//  this function returns a score based on the compatibility of this
	//  genome with the passed genome
	//------------------------------------------------------------------------

	double getCompatibiltyScore(Genome genome)
	{

		//travel down the length of each genome counting the number of 
		  //disjoint genes, the number of excess genes and the number of
		  //matched genes
		  double	numDisjoint = 0;
		  double	numExcess   = 0; 
		  double	numMatched  = 0;

		  //this records the summed difference of weights in matched genes
		  double	WeightDifference = 0;

		  //position holders for each genome. They are incremented as we
		  //step down each genomes length.
		  int g1 = 0;
		  int g2 = 0;
		  
		  while((g1<links.size()-1)||(g2<genome.links.size()-1))
		  {
			  
			  //reached end of genome 1 but not 2
			  if(g1==links.size()-1) 
			  {
				  ++g2;
				  ++numExcess;
				  continue;
			  }
			  
			  // reached end of genome 2 but not 1
			  if(g2==genome.links.size()-1)
			  {
				  ++g1;
				  ++numExcess;
				  
				  continue;
			  }
			  
			  
			  //get innovation numbers for each gene at this point
			  int id1 =links.get(g1).innovationId;
			  int id2 = genome.links.get(g2).innovationId;
			  
			  //innovation numbers identical so increase the matched score
			  if(id1==id2)
			  {
				  
				  
				  
				  //get the weight difference
				  WeightDifference += Math.abs(links.get(g1).weight-genome.links.get(g2).weight);
				  
				  ++g1;
				  ++g2;
				  ++numMatched;
			  
			  }
			  
			  
			  //innovation numbers are different so increment the disjoint score
			  if(id1<id2)
			  {
				  ++numDisjoint;
				  ++g1;
			  }
			  
			  if(id1>id2)
			  {
				  ++numDisjoint;
				  ++g2;
			  }
				  
		  }
 		  //get the length of the longest genome
		  int longest = genome.NumGenes();
		  
		  if(NumGenes() > longest)
		  {
			  longest = NumGenes();
		  }
		  
		  //these are multipliers used to tweak the final score
		  double disjoint = 1;
		  double excess = 1;
		  double matched = 0.4;
		  
		  //finally calculate the scores
		  
		  double score = (excess*numExcess/(double)longest) + (disjoint*numDisjoint/(double)longest) + 
				  		(matched*WeightDifference/(double)numMatched);
		  
		  return score;
	}

	void sortGenes()
	{
		Collections.sort(links);
	}


	//---------------------------------accessor methods
	int	    ID(){return genomeID;}
	void    SetID( int val){genomeID = val;}

	int     NumGenes(){return links.size();}
	int     NumNeurons(){return neurons.size();}
	int     NumInputs(){return numInputs;}
	int     NumOutputs(){return numOutputs;}

	double  AmountToSpawn(){return amountToSpawn;}
	void    SetAmountToSpawn(double num){amountToSpawn = num;}

	void    SetFitness( double num){fitness = num;}
	void    SetAdjFitness( double num){adjustedFitness = num;}
	double  Fitness(){return fitness;}
	double  GetAdjFitness(){return adjustedFitness;}

	int     GetSpecies(){return species;}
	void    SetSpecies(int spc){species = spc;}

	double  SplitY( int val){return neurons.get(val).splitY;}
	ListIterator<LinkGene> startOfGenes(){
		return links.listIterator();
	}
	Vector<LinkGene>	  Genes(){return links;}
	Vector<NeuronGene> Neurons(){return neurons;}

	@Override
	public int compareTo(Genome o) {
		// TODO Auto-generated method stub
		if(this.fitness < o.fitness)
			return -1;
		if(this.fitness >o.fitness)
			return 1;
		return 0;
	}


}
