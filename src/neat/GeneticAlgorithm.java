package neat;

import java.awt.Graphics2D;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;
import java.util.Vector;

class splitDepth
{
	double val;

	int depth;

	public splitDepth(double v,int d) 
	{
		val= v;
		depth =d;

	}
}
public class GeneticAlgorithm 
{

	//current population
	private Vector<Genome> genomes;

	private Vector<Genome> bestGenomes;

	private Vector<Species> species;

	private Innovations innovations;

	private int generation;

	private int nextGenomeId;

	private int nextSpeciesId;

	private int popSize;

	private double totFitAdj,avgFitAdj;

	private int fittestGenome;

	private double bestEverFitness;


	private int xClient, yClient;

	private Vector<splitDepth> splits;
	
	private Random r;

	private void addNeuronId(int nodeId, Vector<Integer> vec)
	{

		for(int i =0;i < vec.size() ; i++)
		{
			if(vec.get(i)==nodeId)
			{
				return;
			}
		}
		
		vec.add(nodeId);
	}

	private void resetandKill()
	{

		totFitAdj = 0;
		avgFitAdj =0;
		
		//purge the species
		for(int i=0;i<species.size();i++)
		{
			Species tmp = species.get(i);
			
			tmp.purge();
			
			if(tmp.GensNoImprovement() > Params.NumGensAllowedNoImprovement &&
					tmp.BestFitness() < bestEverFitness)
			{
				species.remove(i);i--;
			}
		}
		
		for(Genome g:genomes)g.deletePhenotype();
	}

	//separates each individual into its respective species by calculating
	//a compatibility score with every other member of the population and 
	//niching accordingly. The function then adjusts the fitness scores of
	//each individual by species age and by sharing and also determines
	//how many offspring each individual should spawn.

	private void speciateAndCalculateSpawnLevels()
	{

		boolean added = false;
		System.out.println(species.size()+" ll");
		for(Genome gen:genomes)
		{
			for(Species spc:species)
			{
 				double compatibility = gen.getCompatibiltyScore(spc.Leader());
				
				if(compatibility <=Params.CompatibilityThreshold)
				{
					spc.addMember(gen);
					
					gen.SetSpecies(spc.ID());
					
					added = true;
					
					break;
					
				}
				
			}
			
			
			
			if(!added)
			{
				species.add(new Species(gen, nextSpeciesId++));
				System.out.println("new");
			}
			
			added = false;
			
		}
		
		adjustSpeciesFitness();
		
		for(Genome gen:genomes)
		{
			totFitAdj+=gen.GetAdjFitness();
		}
		
		avgFitAdj=totFitAdj/genomes.size();
		
		//calculate how many offspring each member of population 
		//should spawn
		
		for(Genome gen:genomes)
		{
			double toSpawn = gen.GetAdjFitness() / avgFitAdj;
			
			gen.SetAmountToSpawn(toSpawn);
		}
		
		for(Species sp:species)
		{
			sp.calculateSpawnAmount();
		}
		
 	}

	private void adjustSpeciesFitness()
	{

		for(Species sp:species)
			sp.adjustFitness();
	}

	enum parent_type {MUM,DAD};
	private Genome crossover(Genome mum,Genome dad)
	{
		parent_type best;
		
		if(mum.Fitness()== dad.Fitness())
		{
			if(mum.NumGenes() == dad.NumGenes())
			{
				best = parent_type.values()[r.nextInt(2)];
			}
			else
			{
				if(mum.NumGenes() < dad.NumGenes())
				{
					best = parent_type.MUM;
				}
				else
				{
					best = parent_type.DAD;
				}
			}
			
		}
		
		else
		{
			if(mum.Fitness() > dad.Fitness())
			{
				best = parent_type.MUM;
			}
			else
			{
				best = parent_type.DAD;
			}
		}
		
		Vector<NeuronGene> babyNeurons = new Vector<>();
		Vector<LinkGene>   babyGenes = new Vector<>();
		
		Vector<Integer> neurons = new Vector<>();
		
		ListIterator<LinkGene> curMum=mum.startOfGenes();
		ListIterator <LinkGene>curDad = dad.startOfGenes();
	
		LinkGene selected =null;
		while(curDad.hasNext()||curMum.hasNext())
		{
			//the end of mum's gene has been reached
			if(curDad.hasNext()&&!curMum.hasNext())
			{
				if(best==parent_type.DAD)
				{
					selected = curDad.next();
				}
				else
				{
					curDad.next();
				}
			}
			
			//the end of dad's gene has been reached
			else if(curMum.hasNext()&&!curDad.hasNext())
			{
				if(best==parent_type.MUM)
				{
					selected = curMum.next();
				}
				else
				{
					curMum.next();
				}
			}
			else 
			{
				LinkGene tmpdad = curDad.next();
				LinkGene tmpmom = curMum.next();
				
				curDad.previous();
				curMum.previous();
				 if(tmpdad.innovationId<tmpmom.innovationId)
				{
					
					if(best==parent_type.DAD)
					{
						selected=curDad.next();
					}
					else
					{
						curDad.next();
					}
				}
				else if(tmpdad.innovationId>tmpmom.innovationId)
				{
					
					if(best==parent_type.MUM)
					{
						selected=curMum.next();
					}
					else
					{
						curMum.next();
					}
				}
				
				else if(tmpdad.innovationId==tmpmom.innovationId)
				{
					
					
					if(Math.random() < .5)
					{
						selected=curMum.next();
						curDad.next();
					}
					else
					{
						selected = curDad.next();;
						curMum.next();
					}
				}
			}
			
			
			
			//add the selected gene if not already added
			if(babyGenes.size()==0&&selected!=null)
			{
				babyGenes.add(selected);
			}
			else
			{
				if(babyGenes.get(babyGenes.size()-1).innovationId!=
						selected.innovationId)
				{
					babyGenes.add(selected);
				}
			}
			
			addNeuronId(selected.fromNeuron, neurons);
			addNeuronId(selected.toNeuron, neurons);
			
		
		}//end while
		
		//now 
		Collections.sort(neurons);
		
		for(int id:neurons)
		{
			babyNeurons.add(innovations.createNeuronFromId(id));
		}
		
		//finally, create the genome
		return new Genome(nextGenomeId++,babyNeurons,babyGenes,mum.NumInputs(),mum.NumOutputs());
	}

	private Genome tournamentSelection(int numComparisons)
	{
		double bestFitnessSoFar =0;
		int chosenOne = 0;

		for(int i=0;i<numComparisons;i++)
		{
			int thistry = r.nextInt(genomes.size());
			
			if(genomes.get(thistry).Fitness() > bestFitnessSoFar)
			{
				chosenOne = thistry;
				
				bestFitnessSoFar = genomes.get(thistry).Fitness();
			}
		}
		
		//return the champion
		return genomes.get(chosenOne);
	}


	private int calculateNetDepth(Genome gen)
	{

		int maxSoFar = 0;
		
		for(int nd=0;nd<gen.NumNeurons();nd++)
		{
			for(int i=0;i<splits.size();i++)
			{
				if((gen.SplitY(nd) == splits.get(i).val)&&(splits.get(i).depth>maxSoFar))
				{
					maxSoFar = splits.get(i).depth;
				}
			}
		}
		
		return maxSoFar +2;
	}

	//sorts the population into descending fitness, keeps a record of the
	//best n genomes and updates any fitness statistics accordingly

	private void sortAndRecord()
	{
		Collections.sort(genomes,Collections.reverseOrder());
		//System.out.println(genomes.get(0).Fitness()+"best best bste ");
		bestEverFitness = (genomes.get(0).Fitness() > bestEverFitness?genomes.get(0).Fitness():bestEverFitness);
		System.out.println(bestEverFitness+"  best eveveerrrr!!  ");
		storeBestGames();
	}

	private Vector<splitDepth> vecSplitss = new Vector<>();
	private Vector<splitDepth> split(double low,double high, int depth)
	{
		double span = high - low;
		vecSplitss.add(new splitDepth(low+span/2, depth+1));
		
		if(depth > 6)
		{
			return vecSplitss;
		}
		else
		{
			split(low, low+span/2, depth+1);
			split(low+span/2, high, depth+1);
			
			return vecSplitss;
		}
	}




	public GeneticAlgorithm(int size,int inputs,int outputs,int cx,int cy)
	{

		this.popSize = size;
		this.generation = 0;
		this.innovations = null;
		this.nextGenomeId = 0;
		this.nextSpeciesId = 0;
		this.fittestGenome = 0;
		this.bestEverFitness=0;
		this.totFitAdj = 0;
		this.avgFitAdj = 0;
		this.xClient = cx;
		this.yClient = cy;
		
		genomes = new Vector<>();
		bestGenomes = new Vector<>();
		species = new Vector<>();
		for(int i=0;i<popSize;i++)
		{
			genomes.add(new Genome(nextGenomeId++,inputs, outputs));
		}
		
		//create the innovation list. first create a minimal genome
		Genome genome = new Genome(1,inputs,outputs);
		
		//create the innovations
		innovations = new Innovations(genome.Genes(), genome.Neurons());
		
		splits = split(0,1,0);
		
		r = new Random();
	}

	public Vector<NeuralNet> epoch(Vector<Double> fitnessScore){

		if(fitnessScore.size()!=genomes.size())
		{
			System.out.println("efeefefef");
		}
		//System.out.println(species.size());
		resetandKill();
		
		for(int i=0;i<genomes.size();i++)
		{
			genomes.get(i).SetFitness(fitnessScore.get(i));
		}
		
		sortAndRecord();
		
		speciateAndCalculateSpawnLevels();
		//System.out.println("here");
		Vector<Genome> newPop = new Vector<>();
		
		int numSpawnedSoFar = 0;
		
		Genome baby = null;
		
		for(int spec = 0;spec < species.size();spec++)
		{
			if(numSpawnedSoFar < Params.NumBirds)
			{
				int numToSpawn = (int) Math.round(species.get(spec).NumToSpawn());
			
				boolean chosenBestyet = false;
				
				while(numToSpawn--!=0)
				{
					if(!chosenBestyet)//seems best is not mutated
					{
						baby = species.get(spec).Leader();
						System.out.println("specie:"+spec+"leader fitness:"+baby.Fitness());
						chosenBestyet = true;
								
					}
					
					else
					{
						if(species.get(spec).NumMembers() ==1)
						{
							baby = species.get(spec).spawn();
						}
						
						else
						{
							Genome g1 = species.get(spec).spawn();
							
							if(Math.random() < Params.CrossoverRate)
							{
								
								Genome g2 = species.get(spec).spawn();
								
								int numAttempts = 5;
								
								while((g1.ID()==g2.ID())&&numAttempts--!=0)
								{
									g2 = species.get(spec).spawn();
								}
								
								if(g1.ID()!=g2.ID())
								{
									baby = crossover(g1, g2);
								}
							}
							else
							{
								baby = g1;
							}
						}
						
						++nextGenomeId;
						
 						baby.SetID(nextGenomeId);
 						
 						//now we have a spawned child lets mutate it! 
 						//first there is a chance a neuron be added
 						if(baby.NumNeurons()<Params.MaxPermittedNeurons)
 						{
 							baby.addNeuron(Params.ChanceAddNode, innovations, Params.NumTrysToFindOldLink);
 						}
 						
 						//chance of link be added
 						baby.addLink(Params.ChanceAddLink, Params.ChanceAddRecurrentLink, innovations, Params.NumTrysToFindLoopedLink, Params.NumAddLinkAttempts);
 						
 						//mutate the weights
 						baby.mutateWeights(Params.MutationRate, Params.ProbabilityWeightReplaced, Params.MaxWeightPerturbation);
 						
 						baby.mutateActivationResponse(Params.ActivationMutationRate, Params.MaxActivationPerturbation);
 						
 						
					}
					
					//sort the baby genes by their innovation numbers
					baby.sortGenes();
					
					//add to new pop
					newPop.add(baby);
					
					++numSpawnedSoFar;
					
					if(numSpawnedSoFar == Params.NumBirds)
					{
						numToSpawn=0;
					}
				}//end while
			}//end if
		}//next species
		
		//if there is underflow of population due to rounding error 
		//choose remaining through tournament selection on entire population
		if(numSpawnedSoFar < Params.NumBirds)
		{
			int required = Params.NumBirds - numSpawnedSoFar;
			
			//grap them
			while(required--!=0)
			{
				newPop.add(tournamentSelection(popSize/5));
			}
		}
		
		genomes = newPop;
		System.out.println("new pop assigned");
		Vector<NeuralNet> newPhenotypes = new Vector<>();
		
		for(int gen = 0;gen < genomes.size();gen++)
		{
			int depth = calculateNetDepth(genomes.get(gen));
			
			newPhenotypes.add(genomes.get(gen).createPhenotype(depth));
		}
		
		//increase generation counter
		++generation;
		
		return newPhenotypes;
	}

	public Vector<NeuralNet> createPhenotypes()
	{

		Vector<NeuralNet> networks = new Vector<NeuralNet>();
		
		for(int i=0;i<popSize;i++)
		{
			int depth = calculateNetDepth(genomes.get(i));
			
			NeuralNet net = genomes.get(i).createPhenotype(depth);
			
			networks.add(net);
		}
		
		return networks;
	}

	public void storeBestGames() {

		bestGenomes.clear();
		for(int i=0;i<Params.NumBestBirds;i++)
		{
			bestGenomes.add(genomes.get(i));
		}
	}

	public void renderSpeciesInfo(Graphics2D g2)
	{

	}

	public Vector<NeuralNet>  getBestPhenotypesFromLastGeneration()
	{

		Vector<NeuralNet> brains =  new Vector<NeuralNet>();
		for(Genome gen: bestGenomes)
		{
			int depth = calculateNetDepth(gen);
			
			brains.add(gen.createPhenotype(depth));
		}
		return brains;
	}

	public int numSpecies()
	{
		return species.size();
	}

	double bestEverFitness()
	{
		return bestEverFitness;
	}
}
