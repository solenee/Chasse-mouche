package impl;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;

public interface IGameClient extends Remote{
	void receiveFlyHunted(String playerName, int newPoints) throws RemoteException;
	void receiveFlyPosition(int x, int y) throws RemoteException;
	
	void receiveCurrentParticipants(Hashtable<String, Integer> currentScores) throws RemoteException;
}
