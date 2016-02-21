package client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Semaphore;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import impl.IGameClient;

/**
 * 
 * @author EHOLIE Solene
 *
 */
public class ClientGUI extends JFrame {
  	
	// Size of the game area 
	// TODO ATTENTION : Should be fixed by the server
	public static int nbL = 4;
	public static int nbC = 4;
	
	final JLabel[][] area = new JLabel[nbL][nbC];
	DefaultListModel<String> scores = new DefaultListModel();;
	
	final JButton bLogin = new JButton("Login");
	final JButton bLogout = new JButton("Logout");
	
	public static String NOTIF_START = "Login to start the game";
	public static String NOTIF_WELCOME = "Welcome!";
	public static String NOTIF_NEW_GAME = "New game";
	final JLabel notifDisplayer = new JLabel(NOTIF_START);
	
	public static String NOTIF_FLY_HUNTED = "Fly hunted!";
	public static String NOTIF_WIN = "Bravo!";
	final JLabel notifLastWinner = new JLabel();
	
	// -------------------------
	// Constructors
	// -------------------------
  	public ClientGUI() {
		super("Shared Fly Hunting Game");
	  	
		//		Main frame
		// -------------------------------------------
		this.setLocation(500, 200);

		Container content = this.getContentPane();
		content.setLayout(new BorderLayout());
		content.doLayout();
		
		/*	Toolbar */
		content.add(createToolbar(), BorderLayout.PAGE_END);
		/* Fly space */
		content.add(createGameArea(), BorderLayout.CENTER);
		
		/* Informations */
		/* Notifications square + Participants with scores*/
		content.add(createInformationSquare(), BorderLayout.EAST);

		/*	Display main frame */
		this.setSize(500,400);
		this.setVisible(true);


		//		Build controler (event management)
		new Controller(this);
	}

	private Component createGameArea() {
		JPanel p = new JPanel(new GridLayout(nbL, nbC));
		for (int i = 0; i < this.area.length; i++) {
			for (int j = 0; j < this.area[i].length; j++) {
				this.area[i][j] = new JLabel();
				p.add(area[i][j]);
			}
		}
		return p;
	}

	private Component createInformationSquare() {
		JPanel square = new JPanel(new BorderLayout());
		
		// Notifications
		square.add(notifDisplayer, BorderLayout.PAGE_START);
		
		// Participants with score
		square.add(new JList<String>(this.scores));
		
		// Hunted fly signalization 
		square.add(notifLastWinner, BorderLayout.PAGE_END);
		
		return square;
	}

	private Component createToolbar() {
		JPanel bar = new JPanel(new GridLayout(1, 2));
		bar.add(bLogin);
		bar.add(bLogout);
		return bar;
	}
	

	// -------------------------
	// Main methode
	// -------------------------

	public static void main(String[] args) {
	  	new ClientGUI();
	}
		
		
	class Controller implements Observer {

		ClientGUI view;
		GameClient modelClient;
		Semaphore mutexParticipants;
		
		public Controller(ClientGUI _view) {
			view = _view;
			this.manage();
			mutexParticipants = new Semaphore(1); 
		}
		
		// -------------------------
		// Controller actions
		// -------------------------
		
		private void manage() {
			view.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			/*	Toolbar */
			view.bLogin.addMouseListener(new ActionBLogin());
			view.bLogout.addMouseListener(new ActionBLogout());
			bLogout.setEnabled(false);
			/* Game area */
			for (int i = 0; i < view.area.length; i++) {
				for (int j = 0; j < view.area[i].length; j++) {
					view.area[i][j].addMouseListener(new ActionClickArea(i, j));
				}
			}
			/* Notifications (fly hunted) : Observer pattern */
			/* Participants (current list, one more, one less) : Observer pattern */	
		}
		
		private void updateFlyPosition(Position fly) {
			// TODO Check out of boundaries
			for (int i = 0; (i < view.area.length) ; i++) {
				for (int j = 0; (j < view.area[i].length) ; j++) {
					view.area[i][j].setIcon(new ImageIcon());
				}
			}
			view.area[fly.getX()][fly.getY()].setIcon(new ImageIcon("fliege-t20678.jpg"));
			view.notifDisplayer.setText(ClientGUI.NOTIF_NEW_GAME);
		}

		private void updateScores() {
			new Thread(new Runnable() {
		      public void run() {
		    	  try {
					Hashtable<String, Integer> hashtable = modelClient.getScores();
					mutexParticipants.acquire();
					view.scores.removeAllElements();
					for (Iterator<String> iterator = hashtable.keySet().iterator(); iterator.hasNext();) {
						String player = iterator.next();
						view.scores.addElement(player+" : "+hashtable.get(player));
					}
					mutexParticipants.release();
		    	  } catch (InterruptedException e) {
		    		  e.printStackTrace();
		    	  }
		    	  //System.out.println("[ClientGUI] Scores update");
		    	  //view.repaint();
		      }
			}).start();
		}
		
		@Override
		public void update(Observable notifier, Object arg) {
			System.out.println("update : to implement");
			
			// FLY HUNTED
			if (arg instanceof String) {
				// display winner
				String winner = (String) arg;
				if (winner.equals(modelClient.getName())) {
					notifLastWinner.setText("You hunted the fly !");
				} else {
					notifLastWinner.setText("Fly hunted by "+winner);
				}
				// update scores
				updateScores();
			}
			
			// NEW FLY POSITION
			if (arg instanceof Position) {
				Position fly = (Position) arg;
				System.out.println("New fly position received");
				updateFlyPosition(fly);
			} 
			
			// PARTICIPANTS LIST CHANGEMENT
			if (arg instanceof Integer ) {
				int notif = (int) arg;
				if (notif == GameClient.NOTIF_PARTICIPANTS_CHANGED) {
					System.out.println("Participants list changed : update to implement");
					updateScores();
				}
			}
		}
		
		private class ActionBLogin extends MouseAdapter {
		  	public void mouseClicked(MouseEvent e) {
		  		if (bLogin.isEnabled()) {
				  	System.out.println("Button Login pressed :");
				  	String name = JOptionPane.showInputDialog(view, "Enter your name please");
				  	System.out.println("name = "+ name);
				  	if ( (name != null) && (!name.matches("(\\s)*")) ) {
				  		try {
							modelClient = new GameClient(name);
					  		try {
					  			// Subscribe to notifications : fly hunted/ participants login logout
					  			modelClient.addObserver(Controller.this);
					  			// Action
					  			modelClient.login();
					  			// View update
						  		view.notifDisplayer.setText(ClientGUI.NOTIF_WELCOME);
						  		view.bLogin.setEnabled(false);
						  		view.bLogout.setEnabled(true);
						  		// update fly position
						  		updateFlyPosition(modelClient.getFlyPosition());
					  		} catch (RemoteException e1) {
					  			JOptionPane.showMessageDialog(null, "Connection to the server impossible\n"+e1.getMessage());
					  			System.out.println("Login aborted");
					  		}
				  		} catch (RemoteException e2) {
				  			JOptionPane.showMessageDialog(null, "Creation of payer impossible\n"+e2.getMessage());
				  			System.out.println("Login aborted");
						}
				  	} else {
				  		System.out.println("Login aborted");
				  	}
				  	System.out.println("---");
		  		}
				
			}

		}

		private class ActionBLogout extends MouseAdapter {
		  	public void mouseClicked(MouseEvent e) {
				System.out.println("Button Logout pressed");
				try {
		  			modelClient.logout();
		  		} catch (RemoteException e1) {
		  			JOptionPane.showMessageDialog(null, "Connection to the server impossible :"+e1.getMessage());
		  			System.out.println("Logout aborted");
		  		}
				ClientGUI.this.dispose();
		  		System.exit(0);
				}
		}
		
		private class ActionClickArea extends MouseAdapter {
			int x;
			int y;
			public ActionClickArea(int _x, int _y) {
				super();
				x = _x;
				y = _y;
			}
			
			public void mouseClicked(MouseEvent e) {
				if ((modelClient != null)) {
					// game running
					Position fly = modelClient.getFlyPosition();
					if ( (x == fly.getX()) && (y == fly.getY()) ) {
						// Case containing the fly
						try {
							modelClient.huntFly();
							System.out.println("You hunted the fly");
						} catch (RemoteException e1) {
						e1.printStackTrace();
						}
					}
				}
			}
		}
		
		
	}
	
	

}
