package p1;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Test extends JFrame{

	public Test() {
		// TODO Auto-generated \ stub
		setSize(500,400);
		add(new P());
		setVisible(true);
		int i=0;
		while(i<200) {
			i++;
			System.out.println(Math.random()*40-20);
		}
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Test();

	}
	
	class P extends JPanel{
		BufferedImage i;
		public P() {
			try {
				i = ImageIO.read(new File("src/p1/pipe.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	@Override
	protected void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		super.paintComponent(g);
		Graphics2D g2=(Graphics2D)g;
		
		g2.translate(250, 200);
		g2.rotate(Math.PI);
		g2.drawImage(i, 0, 0, null);
	}
		
	}

}
