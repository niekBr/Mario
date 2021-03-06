import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Mario {
	public BufferedImage img;		// Een plaatje van de Enemy (in child gedefineerd)
	public int x, y, breedte, hoogte, xOld, yOld, vy, spring, type;	// De plaats en afmeting van de Enemy in px (in child gedefineerd)
	public boolean platform;
	
	public Mario(BufferedImage image, int xBegin, int yBegin, int vy){
		this.img = image;
		this.x = xBegin;
		this.y = yBegin;
		this.spring = 0;
		this.vy = vy;
		this.type = -1;
	}
	
	public void spring() {
		this.spring = this.y;
		this.vy = -2;
	}
	
	public void teken(Graphics2D tekenObject){
		tekenObject.drawImage(img, x, y, breedte, hoogte, null);
	}

}
