import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

//klasse: "Spel"
public class Spel implements KeyListener, Runnable  {
	/**
	 * De attributen van de klasse
	 */
	Tekenaar t;
	JFrame scherm;
	Thread tr;
	
	ArrayList<Enemy> enemies;
	ArrayList<Rand> randen;
	ArrayList<Kogel> kogels;
	ArrayList<Stat> stats;
	ArrayList<PowerUp> powerups;
	int punten = 0, levens = 3, ammo;
	BufferedImage image;
	boolean running;
	Achtergrond bg;
	Enemy vijand;
	Mario mario;
	boolean gebotst;
	int vx; //Alle objecten moeten meebewegen! Mario en achtergrond bewegen niet als enige!
	int g = 2; //Zwaartekracht! MOET NOG VOOR MEER OBJECTEN GEBRUIKT WORDEN
	boolean pressedUp;
	boolean pressedSpace;
	boolean pressedDown;
	boolean pressedLeft;
	boolean pressedRight;
	
	PowerUp deletePowerUp;
	Kogel deleteKogel;
	Enemy deleteEnemy;
	MysteryBox deleteBox;
	
	Rand addBox;
	String plaatjes; //Het converteren van een int naar plaatjes bij updateCoins()
	String powerUp;
	int coin; //Save van het aantal dat coins nu op staat (stats)
	int leven; //Save van het aantal dat levens nu op staat (stats) 
	int amm;  //Save van het aantal dat ammo nu op staat (stats)
	boolean changed;
	boolean bounceLeft;
	boolean bounceRight;
	int teller;
	boolean gifSwitch;
	boolean kogelLeft;
	boolean bullet;

	
	public Spel(int level){
		
		image = laadPlaatje("kijktRechts.gif");
		mario = new Mario(image, 500, 400, this.g);
		changeType(mario, true);
		
		image = laadPlaatje("background.jpg");
		bg = new Achtergrond(image, 0, 0, 1750, 750);
		
		createEnemies(1, 0, 0);
		
		randen = new ArrayList<Rand>();

		
		stats = new ArrayList<Stat>();
		powerups = new ArrayList<PowerUp>();
		
		createMap(level);
		
		kogels = new ArrayList<Kogel>();
		
		scherm = new JFrame("Mario - Thomas & Niek");
		scherm.setBounds(0, 0, 1000, 600);
		scherm.setLayout(null);
		
		t = new Tekenaar(kogels, bg, enemies, mario, randen, stats, powerups);
		t.setBounds(0, 0, 1000, 600);		
		scherm.add(t);
		
		
		scherm.setVisible(true);
		scherm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		scherm.addKeyListener(this);
		
		running = true;
		gebotst = false;
		coin = 0;
		changed = false;
		
		tr = new Thread(this, "Thomas & Niek - Mario");
		tr.start();
		
	}
	
	public BufferedImage laadPlaatje(String fileName) {
		 BufferedImage img = null;
		 try{
			 img = ImageIO.read(new File(fileName));
		 } catch(IOException e){
			 System.out.println("Er is iets fout gegaan bij het laden van het plaatje " + fileName + ".");
		 }
		 return img;
	}
	
	private void createMap(int level){
		if(level == 1) {
			image = laadPlaatje("grass.jpg");
			for(int i=0; i<30; i++) {
				randen.add(new Rand(image, 50*i, 525, 50, 50));
			}

			randen.add(new Rand(image, 1000, 500, 25, 25));
			
			image = laadPlaatje("mysterybox.jpg");
			randen.add(new MysteryBox(image, 300, 430, 25, 25, "groot"));
			randen.add(new MysteryBox(image, 325, 430, 25, 25, "groot"));
		}
	}

	//Hier alle enemies aanmaken --> Constructor Enemy: Enemy(image, x, y, breedte, hoogte);
	//Arguments: aantal van iedere enemy!
	private void createEnemies(int goombas, int koopatroopas, int parakoopatroopas) {
		//Goomba's
		BufferedImage image = laadPlaatje("goomba1.gif");
		enemies = new ArrayList<Enemy>();
		for(int i=0; i<goombas; i++) {
			enemies.add(new Goomba(image, 50*i, 0));
		}
		
		//Koopa Troopa's
		image = laadPlaatje("koopatroopa.png");
		for(int i=0; i<koopatroopas; i++) {
			enemies.add(new KoopaTroopa(image, 50*i, 50, 25, 25));
		}
		
		//Para Koopa Troopa's
		image = laadPlaatje("parakoopatroopa.jpg");
		for(int i=0; i<parakoopatroopas; i++) {
			enemies.add(new ParaKoopaTroopa(image, 50*i, 100, 25, 25));
		}
	}

	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == e.VK_ESCAPE){
			running = false;
		}
		if(e.getKeyCode() == e.VK_RIGHT){
			if(!bounceLeft) {
				this.vx = -2;
			}
			pressedRight = true;
			kogelLeft = false;
		}
		if(e.getKeyCode() == e.VK_LEFT){
			if(!bounceRight) {
				this.vx = 2;
			}
			pressedLeft = true;
			kogelLeft = true;
		}
		if(e.getKeyCode() == e.VK_DOWN){
			pressedDown = true;
		}
		
		if(e.getKeyCode() == e.VK_UP){ 
			if(!pressedUp) {
				if(mario.platform) {
					mario.spring();
					this.pressedUp = true;
				}
			}
		}
		if(e.getKeyCode() == e.VK_SPACE){
			if(ammo > 0 && !pressedSpace && bullet){
				maakKogel();
				ammo--;
				pressedSpace = true;
				
				if(ammo == 0) {
					changeType(mario, false);
				}
			}
		}
		
		if(e.getKeyCode() == e.VK_F5) {
			enemies.clear();
		}
		
	}

	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == e.VK_RIGHT){
			this.vx = 0;
			pressedRight = false;
			if(!bullet) {
				image = laadPlaatje("kijktRechts.gif"); //niet bullet
			} else {
				image = laadPlaatje("kijktRechts.gif"); //wel bullet
			}
			mario.img = image;
		}
		if(e.getKeyCode() == e.VK_LEFT){
			this.vx = 0;
			pressedLeft = false;
			
			if(!bullet) {
				image = laadPlaatje("kijktLinks.gif"); //niet bullet
			} else {
				image = laadPlaatje("kijktLinks.gif"); //wel bullet
			}
			
			mario.img = image;
		}
		if(e.getKeyCode() == e.VK_UP){
			mario.vy = 1;
			pressedUp = false;
		}
		if(e.getKeyCode() == e.VK_SPACE) {
			pressedSpace = false;
		}
		if(e.getKeyCode() == e.VK_DOWN) {
			pressedDown = false;
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public void maakKogel(){
		if(kogelLeft) {
			image = laadPlaatje("kogelLinks.png");
			kogels.add(new Kogel(image, mario.x - 32, mario.y + mario.hoogte - 35, 32, 26, -3, 0));
		} else {
			image = laadPlaatje("kogelRechts.png");
			kogels.add(new Kogel(image, mario.x + mario.breedte, mario.y + mario.hoogte - 35, 32, 26, 3, 0));
		}
	}
	
	public boolean controleerContact(Mario a, ArrayList<Enemy> enemies) {
		for(Enemy p : enemies){
			
			if(a.x + a.breedte >= p.x && a.x <= p.x + p.breedte && a.y + 2 * a.breedte >= p.y && a.y <= p.y + p.breedte) {
				this.vijand = p;
				if(p instanceof KoopaTroopa) {
					punten++;
				} else {
					if(a.yOld + a.hoogte <= p.y) { //komt van boven
						punten++;
					} else {
						if(punten > 0){
							punten--;
						}
						if(levens > 0){
							if(a.type == 0) {
								levens--;
							} else {
								changeType(a, false);
							}
						}
						
						if(levens == 0){
							gameOver();
						}
					}
					

				}
				return true;
			}
		}
		return false;
	}
	
	public boolean controleerSchot(ArrayList<Kogel> kogels, ArrayList<Enemy> enemies, ArrayList<Rand> randen) {
		for(Kogel k : kogels){
			for(Enemy p : enemies){
				if(k.x + k.breedte >= p.x && k.x <= p.x + p.breedte && k.y + k.breedte >= p.y && k.y <= p.y + p.breedte) {
					kogels.remove(k);
					enemies.remove(p);
					punten++;
					t.repaint();
					return true;
				}
			}
			for(Rand r : randen){
				if(k.x + k.breedte >= r.x && k.x <= r.x + r.breedte && k.y + k.breedte >= r.y && k.y <= r.y + r.breedte) {
					kogels.remove(k);
					return true;
				}
				
				
			}
		}
		return false;
	}
	public void controleerEnemies(ArrayList<Rand> randen, ArrayList<Enemy> enemies){
		for(Enemy e: enemies){
			for(Rand r: randen){
				if(e.x + e.breedte >= r.x && e.x <= r.x + r.breedte && e.y + e.breedte >= r.y && e.y <= r.y + r.breedte) {
					if(e instanceof ParaKoopaTroopa) {
						e.vy = -e.vy;
					} else {
						if(e.y + e.hoogte == r.y){
							e.y = e.yOld;
						} else {
							e.vx = -e.vx;
						}
					}
				}
			}
		}
	}
	
	public void controleerPowerUp(ArrayList<Rand> randen, ArrayList<PowerUp> powerups, Mario a){
		for(PowerUp e: powerups){
			for(Rand r: randen){
				if(e.x + e.breedte >= r.x && e.x <= r.x + r.breedte && e.y + e.breedte >= r.y && e.y <= r.y + r.breedte) {
					if(e.yOld + e.hoogte <= r.y){
						e.y = e.yOld;
					} else if((e.x + e.breedte <= r.xOld) || (e.x + 1 >= r.xOld + r.breedte) ) {
						e.vx = -e.vx;
					}
				}
			}
			
			if(a.x + a.breedte >= e.x && a.x <= e.x + e.breedte && a.y + 2 * a.breedte >= e.y && a.y <= e.y + e.breedte) {
				deletePowerUp = e;
				
				if(e.powerUp == "lifeUp") {
					levens++;
					
				}
				
				if(e.powerUp == "groot") {
					changeType(mario, true);
				}
				
				if(a.type != 2 && e.powerUp == "bullet") {
					changeType(mario, true);
				}
			}
		}
		
		powerups.remove(deletePowerUp);
	}
	
	public void controleerMario(Mario a, ArrayList<Rand> rand){
		a.platform = false;
		
		bounceLeft = false;
		bounceRight = false;
		
		for(Rand p : rand) {
			if(a.x + a.breedte >= p.x && a.x <= p.x + p.breedte && a.y + 2 * a.breedte >= p.y && a.y <= p.y + p.breedte) {
				
				if(a.yOld + a.hoogte <= p.y) { //komt van boven?
					a.platform = true;
					a.y = a.yOld;
				} else if(a.yOld >= p.y + p.hoogte) { //komt van onder?
					a.platform = true;
					a.y = a.yOld;
					mario.vy = g;
					
					//als mario een mysterybox van onder raakt komt er een powerup uit
					if(p instanceof MysteryBox) {
						this.deleteBox = (MysteryBox) p;
						powerUp = deleteBox.powerUp;
						if(powerUp == "groot") {
							if(mario.type == 0) {
								powerUp = "groot";
							}
							
							if(mario.type >= 1) {
								powerUp = "vuurBloem";
							}
						}
						
						image = laadPlaatje(powerUp + ".png");
						powerups.add(new PowerUp(image, p.xOld, p.y - 20, 20, 20, 1, this.g, deleteBox.powerUp));
						
						image = laadPlaatje("emptyBlock.png"); //Box wordt leeg
						addBox = new Rand(image, p.x, p.y, p.breedte, p.hoogte);
					}
					
				} else if(a.x + a.breedte <= p.xOld){ //komt van links?
					bounceLeft = true;
					vx = 0;
					
				} else if(a.x + 1 >= p.xOld + p.breedte) { //komt van rechts?
					bounceRight = true;
					vx = 0;
				}
			}
			
		}
		rand.remove(deleteBox);
		//alleen als addBox niet leeg is!
		if(addBox instanceof Rand) {
			rand.add(addBox);
			addBox = null;
		}
		
	}
	
	public void updateStats(int p, int l, int a){
		//Checkt of de code min. 1x is gerunt en cleart dan pas de coins list (anders komt er een error)
		stats.clear();
	
		//Als de punten nog niet hoger dan 10 zijn dan hoeven er geen twee cijfers getekent te worden
		if(p < 10){
			plaatjes = Integer.toString(p) + ".png";
			image = laadPlaatje(plaatjes);
			stats.add(new Stat(image, 50 ,20, 30, 30));
		}
		//Nu moet er een cijfertje extra bij komen
		if(p > 9){
			plaatjes = Integer.toString(p-10) + ".png";
			image = laadPlaatje("1.png");
			stats.add(new Stat(image, 50 ,20, 30, 30));
			image = laadPlaatje(plaatjes);
			stats.add(new Stat(image, 70 ,20, 30, 30));
		}
		if(l < 10){
			plaatjes = Integer.toString(l) + ".png";
			image = laadPlaatje(plaatjes);
			stats.add(new Stat(image, 50 ,65, 30, 30));
		}
		if(l > 9){
			plaatjes = Integer.toString(l-10) + ".png";
			image = laadPlaatje("1.png");
			stats.add(new Stat(image, 50, 65, 30, 30));
			image = laadPlaatje(plaatjes);
			stats.add(new Stat(laadPlaatje(plaatjes), 700 ,65, 30, 30));
		}
		if(bullet) {
			if(a < 10){
				plaatjes = Integer.toString(a) + ".png";
				image = laadPlaatje(plaatjes);
				stats.add(new Stat(image, 50 ,110, 30, 30));
			}
			if(a > 9){
				plaatjes = Integer.toString(a-10) + ".png";
				image = laadPlaatje("1.png");
				stats.add(new Stat(image, 50, 110, 30, 30));
				image = laadPlaatje(plaatjes);
				stats.add(new Stat(laadPlaatje(plaatjes), 700 ,110, 30, 30));
			}
		}
		//Tekent standaard de coin en stelt de coins gelijk aan punten (kijk while functie)
		coin = punten;
		leven = levens;
		amm = ammo;
		if(bullet) {
			image = laadPlaatje("ammo.png");
			stats.add(new Stat(laadPlaatje("ammo.png"), 20, 110, 30, 30));
		}
		image = laadPlaatje("levens.png");
		stats.add(new Stat(image, 20, 65, 30,30));
		image = laadPlaatje("coin.png");
		stats.add(new Stat(image, 10,10, 50, 50));
	}
	
	public void gameOver() {
		running = false;
	}
	
	public void changeType(Mario m, boolean increase) {
		
		if(increase){
			m.type++;
		} else {
			m.type--;
		}
		
		switch(m.type) {
			case 0:
				m.breedte = 20;
				m.hoogte = 40;
				if(!increase) {
					m.x = m.x + 10;
					m.y = m.y + 20;
				}
				bullet = false;
				break;
			case 1:
				m.breedte = 30;
				m.hoogte = 60;
				if(increase) {
					m.x = m.x - 10;
					m.y = m.y - 20;
				}
				bullet = false;
				break;
			case 2:
				bullet = true;
				ammo = 3;
				break;
		}
	}


	public void run() {
		while (running){
			try{
				Thread.sleep(10);
			}
			
			catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			gebotst = controleerContact(mario, enemies);
			if(gebotst){
				enemies.remove(vijand);
			}
			
			mario.yOld = mario.y;
			
			for(Enemy e : enemies) {
				e.yOld = e.y;
				
				//goomba loop animatie (gif effect)
				if(e instanceof Goomba) {
					if(teller == 10) {
						if(gifSwitch) {
							image = laadPlaatje("goomba1.gif");	//Goomba 1						
						} else {
							image = laadPlaatje("goomba2.gif"); //Goomba 2
						}
						e.img = image;
					}
				}
			}
			
			if(!bullet) { //mario loop animatie als hij NIET bullet is (gif effect)
				if(teller == 10) {
					if(pressedLeft) {
						if(gifSwitch) {
							image = laadPlaatje("looptLinks1.gif"); //Mario links 1							
						} else {
							image = laadPlaatje("looptLinks2.gif"); //Mario links 2
						}
						
					}
					
					if(pressedRight) {
						if(gifSwitch) {
							image = laadPlaatje("looptRechts1.gif"); //Mario rechts 1						
						} else {
							image = laadPlaatje("looptRechts2.gif"); //Mario rechts 2
						}
					}
					
					if(pressedRight || pressedLeft) {
						mario.img = image;
					}
				}
			} else { //mario loop animatie als hij WEL bullet is (gif effect)
				if(teller == 10) {
					if(pressedLeft) {
						if(gifSwitch) {
							image = laadPlaatje("looptLinks1.gif"); //Mario links 1							
						} else {
							image = laadPlaatje("looptLinks2.gif"); //Mario links 2
						}
						
					}
					
					if(pressedRight) {
						if(gifSwitch) {
							image = laadPlaatje("looptRechts1.gif"); //Mario rechts 1						
						} else {
							image = laadPlaatje("looptRechts2.gif"); //Mario rechts 2
						}
					}
					
					if(pressedRight || pressedLeft) {
						mario.img= image;
					}
				}
			}
			
			
			
			for(Rand p : randen) {
				p.xOld = p.x;
				p.x += this.vx;
			}
			
			for(PowerUp p : powerups){
				p.xOld = p.x;
				p.yOld = p.y;
				
				p.x += this.vx;
				p.x += p.vx;
				
				if(p.y == 0) {
					System.out.println("y = 0");
				}
				
				if(p.yOld == 0) {
					System.out.println("yOld = 0");
				}
				
				p.y += p.vy;
			}
			
			mario.y += mario.vy;
			
			if(mario.y < mario.spring - 50){
				mario.vy = g;
				mario.spring = 0;
			}
			
			for(Enemy e : enemies) {
				e.x += e.vx + vx;
				e.y += e.vy;
				if(e.y < 0 || e.y > scherm.getHeight()) {
					deleteEnemy = e;
				}
			}
			
			enemies.remove(deleteEnemy);
			
			
			for(Kogel k : kogels){
				k.x += k.vx + vx;
				if(k.y < 0 || k.y > scherm.getHeight() || k.x < 0 || k.x > scherm.getWidth()){
					deleteKogel = k;
				}
			}
			kogels.remove(deleteKogel);
			
			controleerSchot(kogels, enemies, randen);
			controleerMario(mario, randen);
			controleerEnemies(randen, enemies);
			controleerPowerUp(randen, powerups, mario);
			
			//Checkt of de coins moeten worden geupdate
			if(coin != punten || leven != levens || amm != ammo){
				updateStats(punten,levens,ammo);
			}
			
			if(teller == 10) {
				teller = 0;
				gifSwitch = !gifSwitch;
			}
			
			teller++;
			t.repaint();
		}
		
		scherm.dispose();
		System.out.println("QUIT");
	}
	
}