package p1;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import neat.GeneticAlgorithm;
import neat.NeuralNet;
import neat.Params;

public class Canvas extends JPanel
{
	private BufferedImage back = null;
	private BufferedImage ground=null;

	private double gameSpeed=1;

	static  double virtualHeight,virtualWidth;
	private double heightScale,widthScale;
	private int groundScroll=0,backScroll=0;
	private List<PipePair> pipePairs;
	private final int Frames=60;
	private final double Gravity= 20;
	private final double GRAVITY_PER_FRAME=Gravity/Frames;
	private final int BACKGROND_SCROLL_PER_FRAME = 1;
	private final int GROUND_SCROLL_PER_FRAME = 2;
	private final int BACKGROUND_LOOP_POINT = 413;
	private final int GROUND_LOOPING_POINT = 514;
	private int spawnTimer=0,score=0;
	private boolean scrolling;
	private int deadCount = 0;
	private boolean kill;

	private PipePair incomingPipe=null;
	private Vector<Bird> birds;
	private Vector<Bird> bestBirds;
	//-- height of pipe image, globally accessible
	int PIPE_HEIGHT = 288;
	int PIPE_WIDTH = 70;
	private double lastY = -PIPE_HEIGHT + Math.random()*80+20;



	private GeneticAlgorithm genetic;
	private int generation=0;
	public Canvas() {
		scrolling=true;kill=false;
		// TODO Auto-generated constructor stub
		virtualHeight=288;
		virtualWidth=512;
		heightScale=1;
		widthScale=1;
		birds=createBirds(virtualWidth, virtualHeight,Params.NumBirds);
		bestBirds= createBirds(virtualWidth, virtualHeight, Params.NumBestBirds);
		pipePairs = new LinkedList<PipePair>();
		try {
			back = ImageIO.read(new File("src/p1/background.png"));
			ground = ImageIO.read(new File("src/p1/ground.png"));

		} catch (IOException e) {
			System.out.println("no");
		}

	 
		addComponentListener(new ComponentAdapter() {


			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
				heightScale = (getHeight()-15+1)/virtualHeight;
				widthScale = getWidth()/virtualWidth;
				//System.out.println(getHeight()+" w"+getWidth());
			}

			@Override
			public void componentResized(ComponentEvent e) {
				// TODO Auto-generated method stub
				heightScale = (getHeight()-15+1)/virtualHeight;
				widthScale = getWidth()/virtualWidth;
				//System.out.println(getHeight()+" w"+getWidth());

			}

		});
		setFocusable(true);
		genetic = new GeneticAlgorithm(Params.NumBirds, Params.numInputs,Params.numOutputs, getWidth(), getHeight());
		Vector<NeuralNet> allBrains = genetic.createPhenotypes();

		for(int i=0;i<Params.NumBirds;i++)
		{
			birds.get(i).setBrain(allBrains.get(i));
		}

		addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if(e.getKeyCode()==KeyEvent.VK_F)
				{
					gameSpeed-=.2;
					if(gameSpeed<0)gameSpeed = .2;
					System.out.println("calllll");
				}
				if(e.getKeyCode()==KeyEvent.VK_S)
				{
					gameSpeed+=.2;
				}
				
				if(e.getKeyCode()==KeyEvent.VK_SPACE)
				{
					scrolling=!scrolling;
				}
				if(e.getKeyCode()==KeyEvent.VK_K)
				{
					kill=true;
				}
				

			}
		});

		new Game();
	}
	private Vector<Bird> createBirds(double virtualWidth2, double virtualHeight2, int numbirds) {

		Vector<Bird> temp = new Vector<>();
		for(int i=0;i<numbirds;i++)
		{
			temp.add(new Bird(virtualWidth, virtualHeight,GRAVITY_PER_FRAME,i*4));

		}
		return temp;
	}
	@Override
	protected void paintComponent(Graphics g1) {
		// TODO Auto-generated method stub
		super.paintComponent(g1);
		Graphics2D g=(Graphics2D)g1;
		g.scale(widthScale, heightScale);
		g.drawImage(back, -backScroll, 0, null);
		for(PipePair pair:pipePairs) {
			pair.render(g);
		} 
		g.drawImage(ground, -groundScroll, (int)(virtualHeight), null);

		for(Bird bird:birds)bird.render(g);

	g.setFont(new Font("TimesRoman", Font.BOLD, 20));
	g.setColor(Color.white);
		g.drawString("Score:"+score, 10, 20);
		g.drawString("Generation: "+generation, (int)virtualWidth-200, 20);
		g.drawString("Alive: "+(Params.NumBirds-deadCount),(int)virtualWidth/2-100, 20);
		int ii=(int) (virtualHeight/5*3);
		g.drawString("Instructions:", 10, ii);ii+=25;
		g.setFont(new Font("TimesRoman", Font.BOLD, 12));
		g.drawString("Press 'Enter' to start!", 10, ii);ii+=20;
		g.drawString("Press 'F' to increase simulation speed!", 10, ii);ii+=20;
		g.drawString("Press 'S' to slow down on simulation speed!", 10, ii);ii+=20;
		g.drawString("Press 'spacebar' to pause the simulation!", 10, ii);ii+=20;
		g.drawString("Press 'K' to kill this generation!", 10, ii);ii+=20;


		

		
		Toolkit.getDefaultToolkit().sync();

	}

	private void update()
	{


		//scrolling=false;
		double dist = 0;
		if(incomingPipe==null&&!pipePairs.isEmpty())
		{
			incomingPipe=pipePairs.get(0);
		}
		for(int i=0;i<pipePairs.size();i++) 
		{
			PipePair pair=pipePairs.get(i);
			pair.update();
			if(pair.remove) 
			{
				pipePairs.remove(pair); 
				continue;
			}
			else 
			{
				for(Bird bird:birds) 
				{
					if(bird.isAlive())
						pair.pipes.forEach((key,pipe)->{
							if(bird.collide(pipe)) 
							{
								bird.kill();deadCount++;
							}});
				}
			}
			if(pair.hasGone((virtualWidth/2-birds.get(0).width/2))) {

				incomingPipe=pipePairs.get(i+1);
				score++;

			}
		}
		//if(incomingPipe!=null)
		//System.out.println(incomingPipe.posX+incomingPipe.PIPE_WIDTH-birds.get(0).posX);
		for(Bird bird:birds) {

			if(bird.isAlive()) {
				bird.update(incomingPipe);
				if(bird.boundCollide()) {
					bird.kill(); deadCount++;
				}
			}

		}

		///if all dead start genetic algorithm
		if(deadCount==Params.NumBirds  || kill) 
		{
			// System.out.println("dead");
			Vector<NeuralNet> brains =genetic.epoch(getFitnessScores());
			for(int i=0;i<Params.NumBirds;i++)
			{
				birds.get(i).setBrain(brains.get(i));
				birds.get(i).reset();
			}

			Vector<NeuralNet> bestBrains =genetic.getBestPhenotypesFromLastGeneration();
			for(int i=0;i<Params.NumBestBirds;i++)
			{
				bestBirds.get(i).setBrain(bestBrains.get(i));
				bestBirds.get(i).reset();
			}
			pipePairs.clear();
			PipePair pp= new PipePair(virtualHeight-200);
			pp.posX =virtualWidth-100;
			pipePairs.add(pp);
			incomingPipe=pp;
			score=deadCount=0;
			generation++;
			kill=false;
		}





	}
	private Vector<Double> getFitnessScores() 
	{
		// TODO Auto-generated method stub
		Vector<Double> scores  = new Vector<>();
		for(Bird bird:birds) {
			scores.add(bird.getFitness());
			//System.out.print(bird.getFitness()+" ");
		}System.out.println();

		return scores;
	}
	private void updateBirds()
	{

	}


	class Game implements Runnable{

		Thread loop;
		public Game() {
			// TODO Auto-generated constructor stub
			loop = new Thread(this);
			addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					// TODO Auto-generated method stub
					if(KeyEvent.VK_ENTER==e.getKeyCode())
					{
						loop.start();
						//System.out.println("addededed");
						removeKeyListener(this);
					}
					
				}
			});

		}
		@Override
		public void run() {
			long lastTime = System.nanoTime();
			double nsPerTck=1000000000/Frames;
			double delta =  0;
			long timer = System.currentTimeMillis();
			int frame = 0;
			
			PipePair pp= new PipePair(Math.max(10, Math.min( Math.random()*60-30,virtualHeight-90)));	
			pipePairs.add(pp);
			incomingPipe = pp;
			while(true)
			{
				if(!scrolling)
				{
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				long now = System.nanoTime();
				delta+=(now-lastTime)/nsPerTck;
				lastTime=now;
				if (delta >= gameSpeed )
				{
					frame++;
					delta=0;
					if(frame%120==0)spawnTimer=2;
					update();
					groundScroll=(groundScroll+GROUND_SCROLL_PER_FRAME)%GROUND_LOOPING_POINT;
					backScroll=(backScroll+BACKGROND_SCROLL_PER_FRAME)%BACKGROUND_LOOP_POINT;
					repaint();


				}



				if(System.currentTimeMillis()-1000>timer)
				{
					timer+=1000;
					//	System.out.println(frame);
					// frame=0;
					//spawnTimer++;
				}

				if(spawnTimer==2)
				{
					double y = Math.max(10, Math.min(lastY+Math.random()*60-30,virtualHeight-90));
					lastY=y;
					//System.out.println("y:"+y+" virtual-90:"+(virtualHeight-90)+" ");
					pipePairs.add(new PipePair(y));
					//pipes.add(new Pipe(virtualWidth, virtualHeight, GRAVITY_PER_FRAME));
					spawnTimer=0;
				}


			}




		}

	}

}
