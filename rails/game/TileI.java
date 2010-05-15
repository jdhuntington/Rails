/* $Header: /Users/blentz/rails_rcs/cvs/18xx/rails/game/TileI.java,v 1.20 2010/05/15 19:05:39 evos Exp $ */
package rails.game;

import java.util.List;
import java.util.Map;

import rails.algorithms.RevenueBonusTemplate;
import rails.util.Tag;

public interface TileI {

    public void configureFromXML(Tag se, Tag te) throws ConfigurationException;

    public void finishConfiguration (TileManager tileManager) 
    throws ConfigurationException;
    
    public String getColourName();

    public int getColourNumber();

    /**
     * @return Returns the id.
     */
    public int getId();

    public int getExternalId();

    public int getPictureId();

    /**
     * @return Returns the name.
     */
    public String getName();

    public boolean hasTracks(int sideNumber);

    public List<Track> getTracks();

    public List<Track> getTracksPerSide(int sideNumber);

    public Map<Integer, List<Track>> getTracksPerStationMap();

    public List<Track> getTracksPerStation(int stationNumber);

    public boolean isUpgradeable();
    
    public boolean allowsMultipleBasesOfOneCompany();

    public List<TileI> getUpgrades(MapHex hex, PhaseI phase);

    public List<TileI> getAllUpgrades();

    public List<TileI> getValidUpgrades(MapHex hex, PhaseI phase);

    public String getUpgradesString(MapHex hex);

    public boolean relayBaseTokensOnUpgrade();
        
    public boolean hasStations();

    public List<Station> getStations();

    public int getNumStations();

    public boolean lay(MapHex hex);

    public boolean remove(MapHex hex);

    public int countFreeTiles();

    public int getQuantity();
    public int getFixedOrientation ();

    public List<RevenueBonusTemplate> getRevenueBonuses();

}
