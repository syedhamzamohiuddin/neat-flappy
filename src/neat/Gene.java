package neat;

public class Gene {

}
enum neuron_type
{
	input,hidden,output,bias,none
}

class NeuronGene
{
	//its identification
	int id;

	//it's type
	neuron_type type;
	
	boolean recurrent;
	
	//sets the curvature of the sigmoid
	double activationResponse;
	
	//position in network grid
	double splitX,splitY;
	
	public NeuronGene(neuron_type type,int id,double y,double x)
	{
 
		this.id=id;
		this.type=type;
		this.recurrent=false;
		this.splitX=x;
		this.splitY=y;
		this.activationResponse=1;
	
	}
	public NeuronGene(neuron_type type,int id,double y,double x,boolean r)
	{
		this( type, id, y, x);
		this.recurrent=r; 
	}
	
}

class LinkGene implements Comparable<LinkGene>
{
	int fromNeuron,toNeuron;
	
	double weight;
	
	boolean enabled;
	
	boolean recurrent;
	
	int innovationId;
	public LinkGene() {
		 
	}
	
	LinkGene(int in,int out,boolean enable,int tag,double w)
	{
		this.enabled=enable;
		this.innovationId=tag;
		this.fromNeuron=in;
		this.toNeuron=out;
		this.weight=w;
		this.recurrent=false;
	}
	
	 
	
	LinkGene(int in,int out,boolean enable,int tag,double w,boolean rec)
	{
		this(in,out,enable,tag,w);
		this.recurrent=rec;
	}

	@Override
	public int compareTo(LinkGene o) {
		

		if(this.innovationId < o.innovationId)
			return -1;
		else if(this.innovationId> o.innovationId)
			return +1;
		return 0;
	}
}