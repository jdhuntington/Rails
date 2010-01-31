/* $Header: /Users/blentz/rails_rcs/cvs/18xx/rails/ui/swing/ORWindow.java,v 1.34 2010/01/31 22:22:34 macfreek Exp $*/
package rails.ui.swing;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import rails.common.GuiDef;
import rails.game.GameManager;
import rails.game.OperatingRound;
import rails.game.action.*;
import rails.util.LocalText;

/**
 * This Window displays the available operations that may be performed during an
 * Operating Round. This window also contains the Game Map.
 */
public class ORWindow extends JFrame implements ActionPerformer {
    private static final long serialVersionUID = 1L;
    protected GameUIManager gameUIManager;
    protected ORUIManager orUIManager;
    protected MapPanel mapPanel;
    protected ORPanel orPanel;
    protected UpgradesPanel upgradePanel;
    protected MessagePanel messagePanel;

    protected Rectangle lastBounds;

    protected PossibleActions possibleActions = PossibleActions.getInstance();

    List<LayTile> allowedTileLays = new ArrayList<LayTile>();
    List<LayToken> allowedTokenLays = new ArrayList<LayToken>();

    protected static Logger log =
            Logger.getLogger(ORWindow.class.getPackage().getName());

    public ORWindow(GameUIManager gameUIManager) {
        super();
        this.gameUIManager = gameUIManager;

        String orUIManagerClassName = gameUIManager.getClassName(GuiDef.ClassName.OR_UI_MANAGER);
        try {
            Class<? extends ORUIManager> orUIManagerClass =
                Class.forName(orUIManagerClassName).asSubclass(ORUIManager.class);
            log.debug("Class is "+orUIManagerClass.getName());
            orUIManager = orUIManagerClass.newInstance();
        } catch (Exception e) {
            log.fatal("Cannot instantiate class " + orUIManagerClassName, e);
            System.exit(1);
        }
        gameUIManager.setORUIManager(orUIManager);
        orUIManager.setGameUIManager(gameUIManager);

        getContentPane().setLayout(new BorderLayout());

        messagePanel = new MessagePanel();
        getContentPane().add(messagePanel, BorderLayout.NORTH);

        mapPanel = new MapPanel(gameUIManager);
        getContentPane().add(mapPanel, BorderLayout.CENTER);

        upgradePanel = new UpgradesPanel(orUIManager);
        getContentPane().add(upgradePanel, BorderLayout.WEST);
        addMouseListener(upgradePanel);

        orPanel = new ORPanel(this, orUIManager);
        getContentPane().add(orPanel, BorderLayout.SOUTH);

        orUIManager.init(this);

        setTitle(LocalText.getText("MapWindowTitle"));
        setLocation(10, 10);
        setVisible(false);
        setSize(800, 600);

        final JFrame frame = this;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                StatusWindow.uncheckMenuItemBox(StatusWindow.MAP_CMD);
                frame.dispose();
            }
        });

        gameUIManager.reportWindow.addLog();
    }

    public ORUIManager getORUIManager() {
        return orUIManager;
    }

    public GameUIManager getGameUIManager() {
        return gameUIManager;
    }

    public MapPanel getMapPanel() {
        return mapPanel;
    }

    public ORPanel getORPanel() {
        return orPanel;
    }

    public UpgradesPanel getUpgradePanel() {
        return upgradePanel;
    }

    public MessagePanel getMessagePanel() {
        return messagePanel;
    }

    public boolean process(PossibleAction action) {

        // Add the actor for safety checking in the server
        if (action != null) action.setPlayerName(orPanel.getORPlayer());
        // Process the action
        boolean result = gameUIManager.processOnServer(action);
        // Display any error message
        //displayServerMessage();

        return result;
    }

    // Not yet used
    public boolean processImmediateAction() {
        return true;
    }

    public void displayORUIMessage(String message) {
        if (message != null) {
            JOptionPane.showMessageDialog(this, message);
        }
    }

    public void repaintORPanel() {
        orPanel.revalidate();
    }

    public void activate(OperatingRound or) {
        GameManager gameManager = (GameManager) gameUIManager.getGameManager();
        String numORs = gameManager.getNumOfORs ();

        orPanel.recreate(or);
        setTitle(LocalText.getText("MapWindowORTitle",
                gameManager.getORId(),
                String.valueOf(gameManager.getRelativeORNumber()),
                numORs ));
        pack();
        if (lastBounds != null) {
            Rectangle newBounds = getBounds();
            lastBounds.width = newBounds.width;
            setBounds (lastBounds);
        }
        setVisible(true);
        requestFocus();
    }

    public void updateStatus() {
        // Safety check. Do nothing if this method is called outside Operating Rounds,
        // for instance when a token is exchanged during a Stock Round.
        if (!(gameUIManager.getCurrentRound() instanceof OperatingRound)) return;

        orUIManager.updateStatus();
        requestFocus();
    }

    /**
     * Round-end settings
     *
     */
    public void finish() {
        lastBounds = getBounds();
        orPanel.finish();
        upgradePanel.finish();
        messagePanel.setMessage("");
        setTitle(LocalText.getText("MapWindowTitle"));
    }
}
