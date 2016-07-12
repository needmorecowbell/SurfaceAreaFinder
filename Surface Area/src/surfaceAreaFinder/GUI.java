package surfaceAreaFinder;

import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.ArrayList;
import java.text.DecimalFormat;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GUI {

	private JFrame frame;
	private ArrayList<Point> robotPoints = new ArrayList<Point>(), framePoints = new ArrayList<Point>();
	private int approximateArea;
	private ML ml;
	private Scale sl;
	private double distance, ratio;
	private BufferedImage bi;
	private ProgressBar progressBar;
	private DecimalFormat df;
	
	public GUI() throws Exception{
		
		setUp();
	}
	
	public void setUp() throws Exception{
		
		frame = new JFrame("Surface Area Finder");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLayout(null);
		ml =  new ML();
		sl = new Scale();
		frame.addMouseListener(sl);
		frame.addKeyListener(new KL());
		
		File f = new File("C:\\Users\\David\\Downloads\\image1.jpg");
		bi = ImageIO.read(f);
		
		int width = bi.getWidth();
		int height = bi.getHeight();
		
		ratio = ((double)width) / ((double)height);
		
		System.out.println(ratio);
		
		JPanel panel = new JPanel(){
			/***/
			private static final long serialVersionUID = 5318047352133563272L;
			@Override
			public void paintComponent(Graphics g){
				g.drawImage(bi, 0, 0, this.getWidth(), (int)(this.getHeight()/ratio), null); //this needs changed to keep aspect ratio
			}
		};
		
		panel.setBounds(25, 0, 550, 550);
		
		df = new DecimalFormat("0");
		progressBar = new ProgressBar(df);
		progressBar.setBounds(25, 520, 550, 25);
		progressBar.setStringPainted(true);
		progressBar.setForeground(Color.BLUE);
		progressBar.setMinimum(0);
		progressBar.setValue(0);
		progressBar.setOpaque(true);
		
		frame.add(panel);
		frame.add(progressBar);
		frame.setSize(new Dimension(600, 600));
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
	}
	
	public void connect(){
	
		System.out.println("connecting");
		
		Graphics2D g = (Graphics2D)frame.getGraphics();
		Polygon poly = new Polygon();
		for(int x = 0; x < framePoints.size(); x++){
			Point p = framePoints.get(x);
			poly.addPoint(p.x, p.y);
		}
		g.setColor(Color.RED);
		g.fill(poly);
	
		Rectangle r = poly.getBounds();
		approximateArea = r.width * r.height;
		
		calculateArea(poly);
	}
	
	public void calculateArea(Polygon poly){
		
		int area = 0;
		int approxMinusCounted = 0;
		
		System.out.println("Calculating...");
		
		int startY = robotPoints.get(0).y;
		int endY = robotPoints.get(0).y;
		int startX = robotPoints.get(0).x;
		int endX = robotPoints.get(0).x;
		
		for(int i = 1; i < robotPoints.size(); i++){
			int x = robotPoints.get(i).x;
			int y = robotPoints.get(i).y;
			
			if(x < startX){
				startX = x;
			}
			if(x > endX){
				endX = x;
			}
			if(y < startY){
				startY = y;
			}
			if(y > endY){
				endY = y;
			}
		}
		
		int xdiff = robotPoints.get(0).x - framePoints.get(0).x;
		int ydiff = robotPoints.get(0).y - framePoints.get(0).y;
		
		
		Rectangle r = new Rectangle(startX, startY, endX-startX, endY-startY);
		if(r.contains(poly.getBounds())){
			System.out.println("CONTAINED");
		}
		
		System.out.println(startY);
		System.out.println(endY);
		System.out.println(startX);
		System.out.println(endX);
		
		Robot bot = null;
		try{
			bot = new Robot();
		}catch(AWTException e){
			e.printStackTrace();
		}
		
		Graphics g = frame.getGraphics();
		g.setColor(Color.CYAN);
		
		//this method counts which are red
		/*for(int y = startY; y < endY; y++){
			for(int x = startX; x < endX; x++){
				if(bot.getPixelColor(x, y).equals(Color.RED)){
					area++;
					g.fillRect(x-xdiff, y - ydiff,1,1);
				}
			}
		}*/
		
		//this method counts which are not red
		progressBar.setMaximum(endY-startY);
		new Thread(progressBar).start();
		System.out.println("Maximum: " + (endY-startY));
		int progress = 0;
		
		for(int y = startY; y < endY; y++){
			int x1 = 0, x2 = 0;
			for(int x = startX; x < endX; x++){
				//bot.mouseMove(x, y);
				if(bot.getPixelColor(x, y).equals(Color.RED)){
					x1 = x;
					break;
				}else{
					//g.fillRect(x-xdiff, y-ydiff, 1, 1);
					approxMinusCounted++;
				}
			}
			for(int x = endX; x > startX; x--){
				//bot.mouseMove(x, y);
				if(bot.getPixelColor(x, y).equals(Color.RED)){
					x2 = x;
					break;
				}else{
					//g.fillRect(x-xdiff, y-ydiff, 1, 1);
					approxMinusCounted++;
				}
			}
			area+=(x2-x1);
			progress++;
			progressBar.updateValue(progress);
			//progressBar.update(progressBar.getGraphics());
			System.out.println(progress);
			try{
				Thread.sleep(100);
			}catch(InterruptedException e){}
		}
		
		frame.update(frame.getGraphics());
		for(int i = 0; i < framePoints.size(); i++){
			Graphics graphics = frame.getGraphics();
			graphics.setColor(Color.CYAN);
			graphics.fillOval(framePoints.get(i).x-5, framePoints.get(i).y-5, 10, 10);
		}
		
		progressBar.setString("Done!");
		
		approxMinusCounted = approximateArea - approxMinusCounted;
		
		double areaFromTotal, areaFromSubtraction, average;
		
		System.out.println("Approximate area: " + approximateArea + " px");
		System.out.println("Approximate Area minus Counted: " + approxMinusCounted + " px");
		System.out.println("Area Total: " + area + " px\n");
		System.out.println("Area based on total: " + (areaFromTotal = ((double)area / (Math.pow(distance,2)))) + " in squared");
		System.out.println("Area based on subtraction: " + (areaFromSubtraction = (approxMinusCounted / (Math.pow(distance,2)))) + " in squared\n");
		System.out.println("Accuracy based on total: " + (areaFromTotal / 84.0 * 100) + "%");
		System.out.println("Accuracy based on subtraction: " + (areaFromSubtraction / 84.0 * 100) + "%");
		average = (areaFromTotal + areaFromSubtraction)/2;
		System.out.println("Accuracy based on average: " + (average / 84.0 * 100) + "%");
	}
	
	private class KL implements KeyListener{

		public void keyPressed(KeyEvent arg0) {
			if(arg0.getKeyCode() == KeyEvent.VK_ENTER){
				connect();
				frame.removeMouseListener(ml);
			}else if(arg0.getKeyCode() == KeyEvent.VK_SPACE){
				System.exit(0);
			}
		}
		public void keyReleased(KeyEvent arg0) {}
		public void keyTyped(KeyEvent arg0) {}
	}
	
	private class Scale implements MouseListener{
		
		private int count = 0;
		private Point p1, p2;
		
		public void mouseClicked(MouseEvent arg0) {}
		public void mouseEntered(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		public void mouseReleased(MouseEvent arg0) {}

		public void mousePressed(MouseEvent arg0) {
			Graphics g = frame.getGraphics();
			g.setColor(Color.GREEN);
			if(count == 0){
				p1 = arg0.getPoint();
				g.fillOval(p1.x-5, p1.y-5, 10, 10);
			}else if(count == 1){
				p2 = arg0.getPoint();
				g.fillOval(p2.x-5, p2.y-5, 10, 10);
			}
			count++;
			if(count == 2){
				distance = p2.distance(p1);
				String input = JOptionPane.showInputDialog("Enter the number of inches identified by that scale:");
				double scale = Double.parseDouble(input);
				distance = distance/scale; //scales the answer
				System.out.println(distance);
				g.drawLine(p1.x, p1.y, p2.x, p2.y);
				frame.removeMouseListener(sl);
				frame.addMouseListener(ml);
			}
			  
		}	
	}
	
	private class ML implements MouseListener{

		public void mouseClicked(MouseEvent arg0) {}
		public void mouseEntered(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		public void mouseReleased(MouseEvent arg0) {}

		public void mousePressed(MouseEvent arg0) {
			
			Point p = arg0.getPoint();
			int x = p.x;
			int y = p.y;
			framePoints.add(p);
			robotPoints.add(arg0.getLocationOnScreen());
			Graphics g = frame.getGraphics();
			g.setColor(Color.CYAN);
			g.fillOval(x-5, y-5, 10, 10);
		}	
	}
}
