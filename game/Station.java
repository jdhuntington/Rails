package game;

import java.util.*;

/**
 * A Station object represents a (group of) token slot(s) on a specific tile.
 * Each tokenable city tile has as many Station objects as it has cities.
 * <p>
 * N.B. The class name City is reserved for a city as a node in a Route,
 * which will be linked to a MapHex object.
 * Of course, each City object will correspond with exactly one Station object.
 * However, as a tile is upgraded, the Station objects will be replaced by those
 * of the new tile, whereas the City objects will remain the same (unless when cities merge).
 * 
 * @author Erik Vos
 */
public class Station implements TokenHolderI, Cloneable
{

	private String id;
	private String type;
	private int value;
	private int baseSlots;
	private Track[] tracks;
	private ArrayList tokens;

	public Station(String id, String type, int value)
	{
		this(id, type, value, 0);
	}

	public Station(String id, String type, int value, int slots)
	{
		this.id = id;
		this.type = type;
		this.value = value;
		this.baseSlots = slots;

		tokens = new ArrayList();
	}

	/**
	 * Creates a clone of the station by calling Station's 4 argument
	 * constructor with specified station argument's values
	 * 
	 * @param s
	 */
	public Station(Station s)
	{
		this(s.id, s.type, s.value, s.baseSlots);
	}

	/**
	 * @return Returns the id.
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @return Returns the baseSlots.
	 */
	public int getBaseSlots()
	{
		return baseSlots;
	}

	/**
	 * @return Returns the tracks.
	 */
	public Track[] getTracks()
	{
		return tracks;
	}

	/**
	 * @return Returns the value.
	 */
	public int getValue()
	{
		return value;
	}

	public boolean addToken(TokenHolderI company)
	{
	    if (tokens.size() + 1 <= baseSlots)
		{
			if (!tokens.contains(company))
			{
				tokens.add(company);
				return true;
			}
			else
			{
				Log.error("Unable to add token to this station.\nThis company already has a token at this location.");
				return false;
			}
		}
		else
		{
			Log.error("Unable to add token to this station. No more open slots.");
			return false;
		}
	}

	public List getTokens()
	{
		return tokens;
	}

	public boolean hasTokens()
	{
		return tokens.size() > 0;
	}
	
	public boolean hasTokenSlotsLeft() {
	    return tokens.size() < baseSlots;
	}

	public boolean removeToken(TokenHolderI company)
	{
		int index = tokens.indexOf(company);
		if (index >= 0)
		{
			tokens.remove(index);
			return true;
		}
		else
			return false;
	}

	/**
	 * 
	 * @param company
	 * @return true if this Station already contains an instance of the
	 *         specified company's token.
	 */
	public boolean contains(PublicCompanyI company)
	{
		if (tokens.contains(company))
			return true;
		return false;
	}

	public void setTokens(ArrayList tokens)
	{
		this.tokens = tokens;
	}

	public String toString()
	{
		return "Station ID: " + id + ", Type: " + type + ", Slots: "
				+ baseSlots + ", Value: " + value;
	}
}
