package p1;
import java.awt.Graphics2D;
import java.util.Hashtable;
public class PipePair {
	
	int PIPE_HEIGHT = 288;
	int PIPE_WIDTH = 70;
	int GAP_HEIGHT= 90;
	double posX,posY;
	private final int PIPE_SCROLL =2;
	boolean remove;
	private boolean scored;
	Hashtable<String,Pipe> pipes;
	public PipePair(double y) 
	{
		pipes = new Hashtable<>();
		pipes.put("upper", new Pipe("top",y));
		pipes.put("lower",new Pipe("bottom",y+GAP_HEIGHT));
		posX=Canvas.virtualWidth+20;
		posY=y;
		remove=false;
		scored=false;
	}
	
	void update()
	{
		 
		if(posX> -PIPE_WIDTH)
		{
			posX=posX-PIPE_SCROLL;
			pipes.get("upper").posX=posX;
			pipes.get("lower").posX=posX;
		}
		else
		{
			 remove = true;
		}
	}
	
	 
	
	boolean canScore(double x)
	{
		if(!scored)
		if(posX+PIPE_WIDTH<x)
		{
			
			scored=true;
			return true;
		}
		
		return false;
		
	}
	
	void render(Graphics2D g)
	{
		 pipes.forEach((key,pipe)->pipe.render(g));
	}
	
	boolean hasGone(double x)
	{
		if(!scored)
			if(posX+PIPE_WIDTH<x)
			{
				
				scored=true;
				return true;
			}
			
			return false;
	}

}
