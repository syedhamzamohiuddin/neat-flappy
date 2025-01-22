package p1;

import java.awt.EventQueue;

import javax.swing.JFrame;

 
public class Main extends JFrame{
 
	public Main() {
		// TODO Auto-generated constructor stub
		setSize(512,340);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Canvas canvas = new Canvas();
		add(canvas);
		setVisible(true);
		
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
            Main ex = new Main();
         });
	}
}
 