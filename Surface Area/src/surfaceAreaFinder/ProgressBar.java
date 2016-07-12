package surfaceAreaFinder;

import javax.swing.JProgressBar;
import java.text.DecimalFormat;

public class ProgressBar extends JProgressBar implements Runnable{

	/****/
	private static final long serialVersionUID = -5745532929998900851L;
	private boolean RUN = true;
	private boolean called = false;
	private int value;
	private double percentDone = 0;
	private DecimalFormat df;
	
	public ProgressBar(DecimalFormat df){
		this.df = df;
		setString("Processing...      " + df.format(percentDone) + "%");
		setDoubleBuffered(true);
	}

	public void updateValue(int value){
		called = true;
		this.value = value;
		
	}
	
	public void end(){
		RUN = false;
	}
	
	public void run(){
		int x = 0;
		while(RUN){
			if(called){
				setValue(value);
				percentDone = ((double)value) / ((double)getMaximum()) * 100;
				setString("Processing...      " + df.format(percentDone) + "%");
				if(x == 5){
					update(getGraphics());
					x = 0;
				}
				called = false;
				x++;
			}
			try{
				Thread.sleep(10);
			}catch(InterruptedException e){}
		}
	}
}
