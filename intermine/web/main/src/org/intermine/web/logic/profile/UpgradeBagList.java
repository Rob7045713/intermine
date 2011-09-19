package org.intermine.web.logic.profile;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.intermine.api.bag.BagQueryResult;
import org.intermine.api.bag.BagQueryRunner;
import org.intermine.api.profile.InterMineBag;
import org.intermine.api.profile.BagState;
import org.intermine.api.profile.Profile;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.web.logic.Constants;
import org.intermine.web.logic.bag.BagQueryUpgrade;
import org.intermine.web.logic.session.SessionMethods;

/**
 * Runnable object providing upgrading osbag_int table.
 * @author dbutano
 *
 */
public class UpgradeBagList implements Runnable
{
    private static final Logger LOG = Logger.getLogger(UpgradeBagList.class);
    private Profile profile;
    private BagQueryRunner bagQueryRunner;
    private HttpSession session;

    public UpgradeBagList(Profile profile, BagQueryRunner bagQueryRunner, HttpSession session) {
        this.profile = profile;
        this.bagQueryRunner = bagQueryRunner;
        this.session = session;
    }

    public void run() {
        Map<String, String> savedBagsStatus = SessionMethods.getNotCurrentSavedBagsStatus(session);
        Map<String, InterMineBag> savedBags = profile.getSavedBags();
        for (InterMineBag bag : savedBags.values()) {
        	try {
        		Thread.sleep(5000);
        	} catch (Exception e) {
        		//
        	}
            if (bag.getState().equals(BagState.NOT_CURRENT.toString())) {
                savedBagsStatus.put(bag.getName(), Constants.UPGRADING_BAG);

                BagQueryUpgrade bagQueryUpgrade = new BagQueryUpgrade(bagQueryRunner, bag);
                BagQueryResult result = bagQueryUpgrade.getBagQueryResult();
                try {
                    if (result.getIssues().isEmpty() && result.getUnresolved().isEmpty()) {
                        Map<Integer, List> matches = result.getMatches();
                        //we set temporary the updateBagValues parameter to true
                        //in this way will update the extra field recently added
                        bag.upgradeOsb(matches.keySet(), true);
                        savedBagsStatus.put(bag.getName(), BagState.CURRENT.toString());
                    } else {
                        session.setAttribute("bagQueryResult_" + bag.getName(), result);
                        bag.setState(BagState.TO_UPGRADE);
                        savedBagsStatus.put(bag.getName(), BagState.TO_UPGRADE.toString());
                    }
                } catch (ObjectStoreException ose) {
                    LOG.warn("Impossible upgrade the bags list", ose);
                }
            }
        }
    }
    
    
}
