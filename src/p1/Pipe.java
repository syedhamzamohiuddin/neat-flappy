package p1;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Pipe {

	static int PIPE_WIDTH = 70;
	static int PIPE_HEIGHT = 288;

	private BufferedImage pipe = null;
	 double posX,posY;
	 String orientation =new String();
	private final int PIPE_SCROLL = -2;
	 

	public Pipe(String orientation,double y) {
 		try {
			pipe = ImageIO.read(new File("src/p1/pipe.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		posX=Canvas.virtualWidth+2;
		posY= y;
		this.orientation=orientation;
	}

	public BufferedImage getImage()
	{
		return pipe;
	}

	public void update()
	{
		 posX+=PIPE_SCROLL;
		 
		 
	} 
	
	boolean leftScreen()
	{
		return posX<-pipe.getWidth();
	}
	
	 
	public void render(Graphics2D g)
	{
		AffineTransform t=g.getTransform();
		if(orientation.equals("top"))
		{

			g.translate(posX+PIPE_WIDTH, posY);
			g.rotate(Math.PI);
			g.drawImage(pipe, 0,0, null);

		}
		else {
			g.drawImage(pipe, (int)posX,(int) posY, null);

		}
		g.setTransform(t);
		
	}

}
