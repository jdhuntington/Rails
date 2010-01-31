/* $Header: /Users/blentz/rails_rcs/cvs/18xx/rails/game/action/BuyTrain.java,v 1.17 2010/01/31 22:22:28 macfreek Exp $
 *
 * Created on 20-May-2006
 * Change Log:
 */
package rails.game.action;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import rails.game.*;
import rails.game.special.SpecialProperty;
import rails.game.special.SpecialTrainBuy;
import rails.util.Util;

/**
 * @author Erik Vos
 */
public class BuyTrain extends PossibleORAction {

    // Initial settings
    transient private TrainI train;
    private String trainUniqueId;
    transient private Portfolio from;
    private String fromName;
    private int fixedCost = 0;
    private boolean hasNoTrains = false; // TODO Remove once route checking exists
    transient private List<TrainI> trainsForExchange = null;
    private String[] trainsForExchangeUniqueIds;
    private boolean forcedExchange = false;
    private boolean presidentMustAddCash = false;
    private boolean presidentMayAddCash = false;
    private int presidentCashToAdd = 0;

    transient private SpecialTrainBuy specialProperty = null;
    private int specialPropertyId = 0;
    
    private String extraMessage = null;

    // User settings
    private int pricePaid = 0;
    private int addedCash = 0;
    transient private TrainI exchangedTrain = null;
    private String exchangedTrainUniqueId;

    public static final long serialVersionUID = 2L;

    public BuyTrain(TrainI train, Portfolio from, int fixedCost) {

        this.train = train;
        this.trainUniqueId = train.getUniqueId();
        this.from = from;
        this.fromName = from.getName();
        this.fixedCost = fixedCost;
    }

    public BuyTrain setTrainsForExchange(List<TrainI> trains) {
        trainsForExchange = trains;
        if (trains != null) {
            trainsForExchangeUniqueIds = new String[trains.size()];
            for (int i = 0; i < trains.size(); i++) {
                trainsForExchangeUniqueIds[i] = trains.get(i).getName();
                // TODO: Must be replaced by unique Ids
            }
        }
        return this;
    }

    public BuyTrain setPresidentMustAddCash(int amount) {
        presidentMustAddCash = true;
        presidentCashToAdd = amount;
        return this;
    }

    public BuyTrain setPresidentMayAddCash(int amount) {
        presidentMayAddCash = true;
        presidentCashToAdd = amount;
        return this;
    }

    public BuyTrain setForcedExchange(boolean value) {
        forcedExchange = value;
        return this;
    }

    public void setHasNoTrains(boolean hasNoTrains) {
        this.hasNoTrains = hasNoTrains;
    }
    
    public void setExtraMessage (String message) {
        extraMessage = message;
    }

    public String getExtraMessage() {
        return extraMessage;
    }

    /**
     * @return Returns the specialProperty.
     */
    public SpecialTrainBuy getSpecialProperty() {
        return specialProperty;
    }

    /**
     * @param specialProperty The specialProperty to set.
     */
    public void setSpecialProperty(SpecialTrainBuy specialProperty) {
        this.specialProperty = specialProperty;
        this.specialPropertyId = specialProperty.getUniqueId();
    }

    public boolean hasSpecialProperty() {
        return specialProperty != null;
    }

    /** 
     * To be used for all usage of train, also within this class. 
     * After reloading the 2nd copy etc. of a train with unlimited quantity, 
     * the train attribute will be null (because readObject() is called and the
     * train is initiated before the actions have been executed - the second
     * train is in this case only created after buying the first one).
     * @return
     */ 
    public TrainI getTrain() {
        if (train == null) {
            train = GameManager.getInstance().getTrainManager().getTrainByUniqueId(trainUniqueId);
        }
        return train;
    }

    public Portfolio getFromPortfolio() {
        return from;
    }

    public int getFixedCost() {
        return fixedCost;
    }

    public boolean isForExchange() {
        return trainsForExchange != null && !trainsForExchange.isEmpty();
    }

    public List<TrainI> getTrainsForExchange() {
        return trainsForExchange;
    }

    public boolean isForcedExchange() {
        return forcedExchange;
    }

    public boolean mustPresidentAddCash() {
        return presidentMustAddCash;
    }

    public boolean mayPresidentAddCash() {
        return presidentMayAddCash;
    }

    public int getPresidentCashToAdd() {
        return presidentCashToAdd;
    }

    public boolean hasNoTrains() {
        return hasNoTrains;
    }

    public Portfolio getHolder() {
        return getTrain().getHolder();
    }

    public CashHolder getOwner() {
        return getTrain().getOwner();
    }

    public int getAddedCash() {
        return addedCash;
    }

    public void setAddedCash(int addedCash) {
        this.addedCash = addedCash;
    }

    public int getPricePaid() {
        return pricePaid;
    }

    public void setPricePaid(int pricePaid) {
        this.pricePaid = pricePaid;
    }

    public TrainI getExchangedTrain() {
        return exchangedTrain;
    }

    public void setExchangedTrain(TrainI exchangedTrain) {
        this.exchangedTrain = exchangedTrain;
        if (exchangedTrain != null)
            this.exchangedTrainUniqueId = exchangedTrain.getName();
    }

    @Override
    public String toString() {

        StringBuffer b = new StringBuffer();
        b.append(company.getName());
        b.append(": buy ").append(getTrain().getName());
        b.append("-train from ").append(from.getName());
        if (fixedCost > 0) {
            b.append(" for ").append(Bank.format(fixedCost));
        } else {
            b.append(" for any amount");
            if (pricePaid > 0) b.append(" - paid: ").append(Bank.format(pricePaid));
        }
        if (specialProperty != null) {
            b.append(" using ").append(specialProperty.getCompany().getName());
        }
        if (isForExchange()) {
            b.append(forcedExchange ? " (forced exchange)" : " (exchanged)");
        }
        if (presidentMustAddCash)
            b.append(" must add cash ").append(Bank.format(presidentCashToAdd));
        else if (presidentMayAddCash)
            b.append(" may add cash up to ").append(
                    Bank.format(presidentCashToAdd));
        if (pricePaid > 0) b.append(" - paid: ").append(Bank.format(pricePaid));

        return b.toString();
    }

    @Override
    public boolean equals(PossibleAction action) {
        if (!(action instanceof BuyTrain)) return false;
        BuyTrain a = (BuyTrain) action;
        return a.getTrain() == getTrain() && a.from == from && a.fixedCost == fixedCost
               && a.trainsForExchange == trainsForExchange;
    }

    /** Deserialize */
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {

        //in.defaultReadObject();
        // TEMPORARY Custom reading for backwards compatibility
        ObjectInputStream.GetField fields = in.readFields();
        trainUniqueId = (String) fields.get("trainUniqueId", trainUniqueId);
        fromName = (String) fields.get("fromName", fromName);
        fixedCost = fields.get("fixedCost", fixedCost);
        hasNoTrains = fields.get("hasNoTrains", hasNoTrains);//TEMPORARY
        trainsForExchangeUniqueIds = (String[]) fields.get("trainsForExchangeUniqueIds", trainsForExchangeUniqueIds);
        forcedExchange = fields.get("forcedExchange", forcedExchange);
        presidentMustAddCash = fields.get("presidentMustAddCash", presidentMustAddCash);
        presidentMayAddCash = fields.get("presidentMayAddCash", presidentMayAddCash);
        presidentCashToAdd = fields.get("presidentCashToAdd", presidentCashToAdd);
        specialPropertyId = fields.get("specialPropertyId", specialPropertyId);
        pricePaid = fields.get("pricePaid", pricePaid);
        addedCash = fields.get("addedCash", addedCash);
        exchangedTrainUniqueId = (String) fields.get("exchangedTrainUniqueId", exchangedTrainUniqueId);
        extraMessage = (String) fields.get("extraMessage", extraMessage);

        GameManagerI gameManager = GameManager.getInstance();
        TrainManager trainManager = gameManager.getTrainManager();

        train = trainManager.getTrainByUniqueId(trainUniqueId);
        // Note: the 2nd etc. copy of an unlimited quantity train will become null this way.
        // Set getTrain() for how this is fixed.
        
        from = gameManager.getPortfolioByName(fromName);
        if (trainsForExchangeUniqueIds != null
            && trainsForExchangeUniqueIds.length > 0) {
            trainsForExchange = new ArrayList<TrainI>();
            for (int i = 0; i < trainsForExchangeUniqueIds.length; i++) {
                trainsForExchange.add(trainManager.getTrainByUniqueId(trainsForExchangeUniqueIds[i]));
            }
        }

        if (specialPropertyId > 0) {
            specialProperty =
                    (SpecialTrainBuy) SpecialProperty.getByUniqueId(specialPropertyId);
        }

        if (Util.hasValue(exchangedTrainUniqueId)) {
            exchangedTrain = trainManager.getTrainByUniqueId(exchangedTrainUniqueId);
        }
    }

}
