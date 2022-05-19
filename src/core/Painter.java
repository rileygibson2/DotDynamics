package core;

public class Painter extends Thread {
	Render c;

	public Painter(Render c) {
		this.c = c;
	}

	@Override
	public void run() {
		while (c.paint) {
			if (c.dotsRunning) c.cycleDots(); //Cycle
			
			try {Thread.sleep(20);}
			catch (InterruptedException er) {throw new Error("Sleep error");}
			c.repaint();
		}
	}
}
