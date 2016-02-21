package server;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;

import impl.IGameClient;
import impl.IGameServer;

public class Server extends UnicastRemoteObject implements IGameServer {

	static int PORT = 1234;
	
	static final int POINTS_PER_FLY = 1;
	public static int FLY_X_MAX = 3;
	public static int FLY_X_MIN = 0;
	public static int FLY_Y_MAX = 3;
	public static int FLY_Y_MIN = 0;
	
	Hashtable<String, IGameClient> players;
	Hashtable<String, Integer> scores;
	int flyX;
	int flyY;
	Random random;
	
	
	protected Server() throws RemoteException {
		super();
		players = new Hashtable<String, IGameClient>();
		scores = new Hashtable<String, Integer>();
		random = new java.util.Random();
		newFlyPosition();
	}

	private void newFlyPosition() {
		flyX = random.nextInt(FLY_X_MAX - FLY_X_MIN);
		flyY = random.nextInt(FLY_Y_MAX - FLY_Y_MIN); 
	}
	
	@Override
	public void login(String playerName, IGameClient client)
			throws RemoteException {
		if (!players.containsKey(playerName)) {
			// name available
			players.put(playerName, client);
			scores.put(playerName, 0);
			
			// send fly position
			client.receiveFlyPosition(flyX, flyY);
			// send participants list with their current points : notified as well as the other players
			/*client.receiveCurrentParticipants(getScores());*/
			
			// Notify all players
			Collection<IGameClient> clients = players.values();
			for (Iterator<IGameClient> iterator = clients.iterator(); iterator.hasNext();) {
				IGameClient c = iterator.next();
				try {
					/*c.receiveLoginPlayer(playerName, 0);*/
					c.receiveCurrentParticipants(getScores());
				} catch (RemoteException e) {
					System.err.println("["+playerName+" login] 1 client couldn't be notified");
				}
			}
		} else {
			throw new RemoteException("Name already used");
		}
	}

	@Override
	public void logout(String playerName) throws RemoteException {
		players.remove(playerName);
		scores.remove(playerName);
		
		// Notify all players
		Collection<IGameClient> clients = players.values();
		for (Iterator<IGameClient> iterator = clients.iterator(); iterator.hasNext();) {
			IGameClient c = iterator.next();
			try {
				/*c.receiveLogoutPlayer(playerName);*/
				c.receiveCurrentParticipants(getScores());
			} catch (RemoteException e) {
				System.err.println("["+playerName+"logout] 1 client couldn't be notified");
			}
		}

	}

	@Override
	public void huntFly(String playerName) throws RemoteException {
		// TODO Synchronize
		if (players.containsKey(playerName)) {
			// Register the hunting
			int newScore = scores.get(playerName) + POINTS_PER_FLY;
			scores.put(playerName, newScore);
			
			// Generate a new fly position
			newFlyPosition();
			
			// Notify all players
			Collection<IGameClient> clients = players.values();
			for (Iterator<IGameClient> iterator = clients.iterator(); iterator.hasNext();) {
			  IGameClient c = iterator.next();
			  try {
				  c.receiveFlyHunted(playerName, newScore);
				  c.receiveFlyPosition(flyX, flyY);
			  } catch (RemoteException e) {
				  System.err.println("["+playerName+" huntFly] client couldn't be notified");
			  }
			}
		}
		
	}
	

	public Hashtable<String, Integer> getScores() {
		return (Hashtable<String, Integer>) scores.clone();
	}
	
	public static void main(String args[]) {
		  String URL;

		  try {
		    // Create server and enable acceptation of remote requests
			Registry registry = LocateRegistry.createRegistry(PORT);
		    Server serveur = new Server();
		    
		    // Server URL
		    URL = "//"+InetAddress.getLocalHost().getHostName()+":"+PORT+"/FlyHuntedGameServer";
		    Naming.rebind(URL, serveur);
		    System.out.println("FlyHuntedGameServer " + "bound in RMI registry");
		  } catch(Exception e) {
			  e.printStackTrace();
		  } 
		}

}
