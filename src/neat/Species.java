package neat;

import java.util.Random;
import java.util.Vector;

public class Species implements Comparable<Species>
{

	private Genome leader;
	
	private Vector<Genome> members;
	
	private int speciesId;
	
	private double bestFitness;
	
	private int gensNoImprovement;
	
	private int age;
	
	private double spawnRqd;
	
	private Random rand;
	public Species(Genome firstOrg, int speciesId) 
	{

		rand = new Random();
		
		this.speciesId = speciesId;
		this.bestFitness = firstOrg.Fitness();
		this.gensNoImprovement = 0;
		this.age = 0;
		this.leader = firstOrg;
		this.spawnRqd = 0;
		
		members = new Vector<Genome>();
		members.add(firstOrg);
		leader = firstOrg;
	}
	
	//--------------------------- AdjustFitness ------------------------------
	//
	//  This function adjusts the fitness of each individual by first
	//  examining the species age and penalising if old, boosting if young.
	//  Then we perform fitness sharing by dividing the fitness by the number
	//  of individuals in the species. This ensures a species does not grow 
	//  too large
	//------------------------------------------------------------------------

	public void adjustFitness()
	{
		double total = 0;
		
		for(Genome genome:members)
		{
			double fitness = genome.Fitness();
			
			//boost the fitness scores if the species is young
			if(age < Params.YoungBonusAgeThreshhold)
			{
				fitness *=Params.YoungFitnessBonus;
			}
			
			//punish older species
			if(age > Params.OldAgeThreshold)
			{
				fitness *=Params.OldAgePenalty;
			}
			
			total+= fitness;
			
			//apply fitness sharing to adjusted fitness
			double adjustedFitness = fitness/members.size();
			
			genome.SetAdjFitness(adjustedFitness);
		}
	}
	
	//adds new member to this species
	public void addMember(Genome newMember)
	{
		if(newMember.Fitness() > bestFitness)
		{
			bestFitness = newMember.Fitness();
			
			gensNoImprovement = 0;
			
			leader = newMember;
		}
		
		members.add(newMember);
	}
	
	public void purge()
	{
		members.clear();
		
		//update age etc
		++age;
		
		++gensNoImprovement;
		
		spawnRqd = 0;
	}
	
	public void calculateSpawnAmount()
	{
		for(Genome gen:members)
			spawnRqd+=gen.AmountToSpawn();
	}
	
	public Genome spawn()
	{
		Genome baby = new Genome();
		
		if(members.size()==1)
		{
			baby = members.get(0);
		}
		else
		{
			int maxIndexSize = (int)(Params.SurvivalRate*members.size())-1;
			if(maxIndexSize<=0) {
				maxIndexSize=+1;
			}
			int theone=0;
				try {
					 theone= rand.nextInt(maxIndexSize);

				}catch(IllegalArgumentException e)
				{
					System.out.println(maxIndexSize+":max");
				}
			
			baby  = members.get(theone);
		}
		
		return baby;
	}
	
	//accessor methods
	  Genome  Leader(){return leader;}
	  
	  double   NumToSpawn(){return spawnRqd;}

	  int      NumMembers(){return members.size();}
	  
	  int      GensNoImprovement(){return gensNoImprovement;}

	  int      ID(){return speciesId;}

	  double   SpeciesLeaderFitness(){return leader.Fitness();}
	  
	  double   BestFitness(){return bestFitness;}

	  int      Age(){return age;}

	  
	  //so we can sort species by best fitness. Largest first
	   
	@Override
	public int compareTo(Species o) {
		// TODO Auto-generated method stub
		if(this.bestFitness<o.bestFitness)
			return -1;
		if(this.bestFitness>o.bestFitness)
			return 2;
		return 0;
	}
	
	
}
