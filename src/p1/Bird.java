package p1;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;

import neat.NeuralNet;
import neat.NeuralNet.run_type;


public class Bird {
	private BufferedImage bird = null;
	double posX,posY;
	private final double GRAVITY_PER_FRAME;
	private double velocity;
	  double height,width;
	private final double starty,startx;
	private double fitness;// fraction of seconds passed or frame passed
	private boolean alive;
	private int called=0;
	private NeuralNet brain;
	private double vh,vw;
	public Bird(double virtualWidth, double virtualHeight,double GRAVITY_PER_FRAME,int y) {

		try {
			bird = ImageIO.read(new File("src/p1/bird.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//	System.out.println(bird.getHeight()+" width:"+bird.getWidth());
		width = bird.getWidth();
		height = bird.getHeight();
		startx=(virtualWidth/2-width/2);
		starty=(virtualHeight/2-height/2);
		posX=startx;
		posY= starty;
		this.GRAVITY_PER_FRAME=GRAVITY_PER_FRAME;
		velocity=0;
		fitness = 0;
		alive= true;
		vw=virtualWidth;
		vh=virtualHeight;

	}

	public BufferedImage getImage()
	{
		return bird;
	}

	public void update(PipePair pp)
	{

		velocity+=GRAVITY_PER_FRAME;
		posY+=velocity;
		called++;
		if(called==20) {
			called =0;
			fitness++;
		}

		//
		Vector<Double> inputs = new Vector<>();
		double topDist,bottomDist,horizontalDist;
		if(pp!=null) {



			topDist=this.posY - pp.pipes.get("upper").posY;
			bottomDist = pp.pipes.get("lower").posY-(this.posY+height);
			horizontalDist = (pp.posX+pp.PIPE_WIDTH)-(this.posX+width/2);
		}
		else
		{
			horizontalDist = vw/2-20;
			bottomDist = topDist = height/2;
		}
		//inputs.add(velocity*4);
		inputs.add(topDist);
		inputs.add(bottomDist);
		inputs.add(horizontalDist);
		inputs.add(this.posY);
		/*
		 * for(Double d:inputs) System.out.print(d+" ");System.out.println();
		 */
		Vector<Double> output =brain.update(inputs,run_type.active );


		//	System.out.println("output:  "+output.get(0));
		if(output.get(0)>.5)
			jump();
		//	System.out.println("horizontal:"+ horizontalDist+", bottom:"+bottomDist+",top:"+topDist );



		/*
		 * if(posY>288) { posY=(int)Math.random()*288; velocity=0; }
		 */
	}

	public void jump() 
	{
		velocity=-5;

	}
	public void render(Graphics2D g)
	{

		if(posX<0) {

		}
		else
		{
			if(!alive)
			{
				posX-=2;
			}
			g.drawImage(bird, (int)posX,(int) posY, null);
		}
	}

	public boolean collide(Pipe pipe) 
	{
		if(pipe.orientation.equals("bottom"))
			if(( (posX+ 2+width-4) >= pipe.posX) &&( posX + 2 <= (pipe.posX + Pipe.PIPE_WIDTH) ))
				if ((((posY + 2) + (height - 4) )>= pipe.posY) && ( posY + 2 )<=( pipe.posY + Pipe.PIPE_HEIGHT))
					return true;
		if(pipe.orientation.equals("top"))
			if(( (posX+ 2+width-4) >= pipe.posX) &&( posX + 2 <= (pipe.posX + Pipe.PIPE_WIDTH) ))
				if ((((posY + 2) )<= pipe.posY) && ( posY + 2 )>=( pipe.posY - Pipe.PIPE_HEIGHT))
					return true;


		return false;
		// TODO Auto-generated method stub

	}

	public boolean boundCollide() {
		return (posY+height>288||posY<0);
	}

	public void kill()
	{
		alive = false;
		//System.out.println(fitness);
	}
	public boolean isAlive()
	{
		return alive;
	}

	public void setBrain(NeuralNet brain)
	{
		this.brain = brain;
	}
	public double getFitness()
	{
		return fitness;
	}

	public void reset()
	{

		posX=startx;
		posY= starty;
		velocity=0;
		fitness = 0;
		alive= true;
	}

}
