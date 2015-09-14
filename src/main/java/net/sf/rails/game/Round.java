package net.sf.rails.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.rails.common.*;
import net.sf.rails.game.model.PortfolioModel;
import net.sf.rails.game.state.ArrayListState;
import net.sf.rails.game.state.BooleanState;
import net.sf.rails.game.state.Creatable;
import net.sf.rails.game.state.Currency;
import net.sf.rails.game.state.Portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rails.game.action.*;


public abstract class Round extends RailsAbstractItem implements Creatable {

    protected static Logger log =
            LoggerFactory.getLogger(Round.class);

    protected final PossibleActions possibleActions;
    protected final GuiHints guiHints;

    protected final GameManager gameManager;
    protected final CompanyManager companyManager;
    protected final PlayerManager playerManager;
    protected final Bank bank;
    protected final PortfolioModel ipo;
    protected final PortfolioModel pool;
    protected final PortfolioModel unavailable;
    protected final PortfolioModel scrapHeap;
    protected final StockMarket stockMarket;
    protected final MapManager mapManager;

    protected final BooleanState wasInterrupted = BooleanState.create(this, "wasInterrupted");


    /** Autopasses */
    // TODO: Should this be moved to the StockRound classes?
    private final ArrayListState<Player> autopasses = ArrayListState.create(this, "autopasses");
    private final ArrayListState<Player> canRequestTurn = ArrayListState.create(this, "canRequestTurn");
    private final ArrayListState<Player> hasRequestedTurn = ArrayListState.create(this, "hasRequestedTurn");

    protected Round (GameManager parent, String id) {
        super(parent, id);

        this.gameManager = parent;
        this.possibleActions = gameManager.getPossibleActions();

        companyManager = getRoot().getCompanyManager();
        playerManager = getRoot().getPlayerManager();
        bank = getRoot().getBank();
        // TODO: It would be good to work with BankPortfolio and Owner instead of PortfolioModels
        // However this requires a lot of work inside the Round classes
        ipo = bank.getIpo().getPortfolioModel();
        pool = bank.getPool().getPortfolioModel();
        unavailable = bank.getUnavailable().getPortfolioModel();
        scrapHeap = bank.getScrapHeap().getPortfolioModel();
        stockMarket = getRoot().getStockMarket();
        mapManager = getRoot().getMapManager();

        guiHints = gameManager.getUIHints();
        guiHints.setCurrentRoundType(getClass());
    }
    
    // TODO: Remove as this is abstract class?
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean process(PossibleAction action) {
        return true;
    }

    /**
     * Default version, does nothing. Subclasses should override this method
     * with a real version.
     */
    // TODO: Remove as this is abstract class?
    public boolean setPossibleActions() {
        return false;
    }

    /** Set the operating companies in their current acting order */
    // What is the reason of that to have that here? => move to OR?
    public List<PublicCompany> setOperatingCompanies() {
        return setOperatingCompanies (null, null);
    }

    // What is the reason of that to have that here => move to OR?
    public List<PublicCompany> setOperatingCompanies(List<PublicCompany> oldOperatingCompanies,
            PublicCompany lastOperatingCompany) {

        Map<Integer, PublicCompany> operatingCompanies =
            new TreeMap<Integer, PublicCompany>();
        List<PublicCompany> newOperatingCompanies;
        StockSpace space;
        int key;
        int minorNo = 0;
        boolean reorder = gameManager.isDynamicOperatingOrder()
        && oldOperatingCompanies != null && lastOperatingCompany != null;

        int lastOperatingCompanyndex;
        if (reorder) {
            newOperatingCompanies = oldOperatingCompanies;
            lastOperatingCompanyndex = oldOperatingCompanies.indexOf(lastOperatingCompany);
        } else {
            newOperatingCompanies = companyManager.getAllPublicCompanies();
            lastOperatingCompanyndex = -1;
        }

        for (PublicCompany company : newOperatingCompanies) {
            if (!reorder && !canCompanyOperateThisRound(company)) continue;

            if (reorder
                    && oldOperatingCompanies.indexOf(company) <= lastOperatingCompanyndex) {
                // Companies that have operated this round get lowest keys
                key = oldOperatingCompanies.indexOf(company);
            } else if (company.hasStockPrice()) {
                // Key must put companies in reverse operating order, because sort
                // is ascending.
                space = company.getCurrentSpace();
                key = 1000000 * (999 - space.getPrice())
                + 10000 * (99 - space.getColumn())
                + 100 * (space.getRow()+1)
                + space.getStackPosition(company);
            } else {
                key = 50 + ++minorNo;
            }
            operatingCompanies.put(new Integer(key), company);
        }

        return new ArrayList<PublicCompany>(operatingCompanies.values());
    }

    /** Can a public company operate? (Default version) */
    // What is the reason of that to have that here? => move to OR?
    protected boolean canCompanyOperateThisRound (PublicCompany company) {
        return company.hasFloated() && !company.isClosed();
    }

    /**
     * Check if a company must be floated, and if so, do it. <p>This method is
     * included here because it is used in various types of Round.
     *
     * @param company
     */
    // What is the reason of that to have that here? => move to SR?
    protected void checkFlotation(PublicCompany company) {

        if (!company.hasStarted() || company.hasFloated()) return;

        if (company.getSoldPercentage() >= company.getFloatPercentage()) {
            // Company floats
            floatCompany(company);
        }
    }

    /**
     * Float a company, including a default implementation of moving cash and
     * shares as a result of flotation. <p>Full capitalisation is implemented
     * as in 1830. Partial capitalisation is implemented as in 1851. Other ways
     * to process the consequences of company flotation must be handled in
     * game-specific subclasses.
     */
    // What is the reason of that to have that here? => move to SR?
    protected void floatCompany(PublicCompany company) {

        // Move cash and shares where required
        int soldPercentage = company.getSoldPercentage();
        int cash = 0;
        int capitalisationMode = company.getCapitalisation();
        if (company.hasStockPrice()) {
            int capFactor = 0;
            int shareUnit = company.getShareUnit();
            if (capitalisationMode == PublicCompany.CAPITALISE_FULL) {
                // Full capitalisation as in 1830
                capFactor = 100 / shareUnit;
            } else if (capitalisationMode == PublicCompany.CAPITALISE_INCREMENTAL) {
                // Incremental capitalisation as in 1851
                capFactor = soldPercentage / shareUnit;
            } else if (capitalisationMode == PublicCompany.CAPITALISE_WHEN_BOUGHT) {
                // Cash goes directly to treasury at each buy (as in 1856 before phase 6)
                capFactor = 0;
            }
            int price = company.getIPOPrice();
            cash = capFactor * price;
        } else {
            cash = company.getFixedPrice();
        }

        // Substract initial token cost (e.g. 1851, 18EU)
        cash -= company.getBaseTokensBuyCost();

        company.setFloated(); // After calculating cash (for 1851: price goes
        // up)

        if (cash > 0) {
            String cashText = Currency.fromBank(cash, company);
            ReportBuffer.add(this, LocalText.getText("FloatsWithCash",
                    company.getId(),
                    cashText ));
        } else {
            ReportBuffer.add(this, LocalText.getText("Floats",
                    company.getId()));
        }

        if (capitalisationMode == PublicCompany.CAPITALISE_INCREMENTAL
                && company.canHoldOwnShares()) {
            // move all shares from ipo to the company portfolio
            // FIXME: Does this work correctly?
            Portfolio.moveAll(ipo.getCertificates(company), company);
        }
    }

    // Could be moved somewhere else (RoundUtils?)
    protected void finishRound() {
        // Report financials
        ReportBuffer.add(this, "");
        for (PublicCompany c : companyManager.getAllPublicCompanies()) {
            if (c.hasFloated() && !c.isClosed()) {
                ReportBuffer.add(this, LocalText.getText("Has", c.getId(),
                        Bank.format(this, c.getCash())));
            }
        }
        for (Player p : playerManager.getPlayers()) {
            ReportBuffer.add(this, LocalText.getText("Has", p.getId(),
                    Bank.format(this, p.getCashValue())));
        }
        // Inform GameManager
        gameManager.nextRound(this);
    }

    /** Generic stub to resume an interrupted round.
     * Only valid if implemented in a subclass.
     *
     */
    // make it abstract?
    public void resume() {
        log.error("Calling Round.resume() is invalid");
    }

    // make it abstract?
    public boolean wasInterrupted () {
        return wasInterrupted.value();
    }
    
    // Do we need that anymore?
    public int getGameParameterAsInt (GameDef.Parm key) {
        if (key.defaultValue() instanceof Integer) {
            return (Integer) gameManager.getGameParameter(key);
        } else {
            return -1;
        }
    }

    // Do we need that anymore?
    public boolean getGameParameterAsBoolean (GameDef.Parm key) {
        if (key.defaultValue() instanceof Boolean) {
            return (Boolean) gameManager.getGameParameter(key);
        } else {
            return false;
        }
    }

    // Make this abstract
    public String getRoundName() {
        return this.getClass().getSimpleName();
    }

    // What is the reason of that to have that here? => move to SR?
    public boolean requestTurn (Player player) {
        if (canRequestTurn(player)) {
            if (!hasRequestedTurn.contains(player)) hasRequestedTurn.add(player);
            return true;
        }
        return false;
    }

    // What is the reason of that to have that here? => move to SR?
    public boolean canRequestTurn (Player player) {
        return canRequestTurn.contains(player);
    }

    // What is the reason of that to have that here? => move to SR?
    public void setCanRequestTurn (Player player, boolean value) {
        if (value && !canRequestTurn.contains(player)) {
            canRequestTurn.add(player);
        } else if (!value && canRequestTurn.contains(player)) {
            canRequestTurn.remove(player);
        }
    }

    // What is the reason of that to have that here? => move to SR?
    public void setAutopass (Player player, boolean value) {
        if (value && !autopasses.contains(player)) {
            autopasses.add(player);
        } else if (!value && autopasses.contains(player)) {
            autopasses.remove(player);
        }
    }

    // What is the reason of that to have that here? => move to SR?
    public boolean hasAutopassed (Player player) {
        return autopasses.contains(player);
    }

    // What is the reason of that to have that here? => move to SR?
    public List<Player> getAutopasses() {
        return autopasses.view();
    }

    /** A stub for processing actions triggered by a phase change.
     * Must be overridden by subclasses that need to process such actions.
     * @param name (required) The name of the action to be executed
     * @param value (optional) The value of the action to be executed, if applicable
     */
    // can this be moved to GameManager?
    public void processPhaseAction (String name, String value) {

    }
}
