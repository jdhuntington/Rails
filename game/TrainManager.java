	/* $Header: /Users/blentz/rails_rcs/cvs/18xx/game/Attic/TrainManager.java,v 1.1 2005/08/17 21:58:00 evos Exp $ *  * Created on 17-08-2005 by Erik Vos * Changes: */package game;import java.util.*;import org.w3c.dom.*;import util.XmlUtils;public class TrainManager implements TrainManagerI, ConfigurableComponentI{   /**    * No-args constructor.    */   public TrainManager()   {      //Nothing to do here, everything happens when configured.   }   /**    * @see game.ConfigurableComponentI#configureFromXML(org.w3c.dom.Element)    */   public void configureFromXML(Element el) throws ConfigurationException   {   }   }