package client;


import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;

import impl.IGameClient;
import impl.IGameServer;

public class GameClient extends UnicastRemoteObject implements IGameClient {

	public static int NOTIF_PARTICIPANTS_CHANGED = 0;
	public static int NOTIF_FLY_HUNTED = 0;
	
	private static IGameServer server;
	static int PORT = 1234;
	private String name;
	
	private Position flyPosition;
	private Hashtable<String, Integer> scores;
	private Notifier notifier;
	
	protected GameClient(String _name) throws RemoteException {
		super();
		name = _name;
		notifier = new Notifier();
		flyPosition = new Position();
		scores = new Hashtable<String, Integer>();
	}

	public void login() throws RemoteException {
		try {
			String serverUrl = "//"+InetAddress.getLocalHost().getHostName()+":"+PORT+"/FlyHuntedGameServer";
			server = (IGameServer) Naming.lookup(serverUrl);
			server.login(getName(), this);
			System.out.println("Client : login successful ");
		} catch (MalformedURLException | UnknownHostException
				| NotBoundException e) {
			e.printStackTrace();
		}
	}
	
	public void logout() throws RemoteException {
		server.logout(getName());
	}
	
	public void huntFly() throws RemoteException {
		server.huntFly(getName());
	}
	
	@Override
	public void receiveFlyHunted(String playerName, int newPoints)
			throws RemoteException {
		System.out.println("receiveFlyHunted by "+playerName+" / new score : "+newPoints);
		scores.put(playerName, newPoints);
		// notify IHM
		notifier.xxChanged();
		notifier.notifyObservers(playerName);
	}

	@Override
	public void receiveFlyPosition(int x, int y) throws RemoteException {
		System.out.println("receiveFlyPosition x="+x+", y="+y);
		Position fly = new Position(x, y);
		setFlyPosition(fly);
		// notify IHM
		notifier.xxChanged();
		notifier.notifyObservers(fly);
	}
	
	
	@Override
	public void receiveCurrentParticipants(Hashtable<String, Integer> currentScores)
			throws RemoteException {
		System.out.println("Current scores received");
		setScores(currentScores);
		// Notify IHM
		notifier.xxChanged();
		notifier.notifyObservers(NOTIF_PARTICIPANTS_CHANGED);
		
	}
	
	// -------
	// Getters - Setters
	// -------
	public Position getFlyPosition() {
		return flyPosition.clone();
	}

	public void setFlyPosition(Position flyPosition) {
		this.flyPosition = flyPosition;
	}

	public Hashtable<String, Integer> getScores() {
		return (Hashtable<String, Integer>) scores.clone();
	}

	public void setScores(Hashtable<String, Integer> scores) {
		this.scores = scores;
	}

	public String getName() {
		return name;
	}
	
	// ----------
	// Observable
	// ----------
	public void addObserver(Observer obs) {
		notifier.addObserver(obs);
	}
	
	public class Notifier extends Observable {
		public void xxChanged() {
			this.setChanged();
		}
	}

}
