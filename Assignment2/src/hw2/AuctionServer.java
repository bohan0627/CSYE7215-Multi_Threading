package hw2;

/**
 *  @author Bo Han(NUID: 001815357)
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class AuctionServer
{
    /**
     * Singleton: the following code makes the server a Singleton. You should
     * not edit the code in the following noted section.
     *
     * For test purposes, we made the constructor protected.
     */

    /* Singleton: Begin code that you SHOULD NOT CHANGE! */
    protected AuctionServer()
    {
    }

    private static AuctionServer instance = new AuctionServer();

    public static AuctionServer getInstance()
    {
        return instance;
    }

    /* Singleton: End code that you SHOULD NOT CHANGE! */


    /* Statistic variables and server constants: Begin code you should likely leave alone. */


    /**
     * Server statistic variables and access methods:
     */
    private int soldItemsCount = 0;
    private int soldItemsCountTotal = 0;
    private int revenue = 0;
    private Date startTime;





    // Implemented some helper functions
    public void setTime(){
        startTime = new Date();
    }
    public int auctionServerRevenue()
    {
        revenue = 0;
        for(int itemID : itemsAndIDs.keySet())
            if(highestBids.containsKey(itemID))
                revenue += highestBids.get(itemID);
        return revenue;
    }

    public int bidderCashSpent(String name){
        int cashSpent = 0;
        for(int i : highestBidders.keySet())
            if(highestBidders.get(i).equals(name))
                cashSpent += highestBids.get(i);
        return cashSpent;
    }

    public int totalSubmittedItems() {
        return lastListingID + 1;
    }

    public int itemsSellingRate(){
        Date current = new Date();
        soldItemsCount = Math.round(soldItemsCountTotal * 100 / (current.getTime() - startTime.getTime()));
        return soldItemsCount;
    }
    public int totalSoldItems()
    {
        soldItemsCountTotal = 0;
        for(int itemID : itemsAndIDs.keySet())
            if(!itemsAndIDs.get(itemID).biddingOpen() && !itemUnbid(itemID) )
                soldItemsCountTotal++;
        return soldItemsCountTotal;
    }

    public int participatedBidders() {
        return itemsPerBuyer.size();
    }

    public int participatedSellers(){
        return itemsPerSeller.size();
    }

    public void highestBid(){
        for(int itemID : highestBids.keySet())
            System.out.println("The highest bid for item " + itemID + " : " + highestBids.get(itemID) );
    }

    // This list for tracking bias working status
    List<String> biasCheck = new ArrayList<>();

    /**Bias Policy:
     * 1. if two bidders with the same bidding amount: then this highestBidder will be allocated to the bidder who has the least cash spent;
     * 2. next: if each one with the same cash spent, then this highestBidder will be allocated to the bidder who has the least items bidding count;
     * 3. if items count that each bidder has been bidding also the same, then do nothing.
    */

     /**
     * Server restriction constants:
     */
    public static final int maxBidCount = 10; // The maximum number of bids at any given time for a buyer.
    public static final int maxSellerItems = 20; // The maximum number of items that a seller can submit at any given time.
    public static final int serverCapacity = 80; // The maximum number of active items at a given time.


    /* Statistic variables and server constants: End code you should likely leave alone. */



    /**
     * Some variables we think will be of potential use as you implement the server...
     */

    // List of items currently up for bidding (will eventually remove things that have expired).
    private List<Item> itemsUpForBidding = new ArrayList<Item>();


    // The last value used as a listing ID.  We'll assume the first thing added gets a listing ID of 0.
    private int lastListingID = -1;

    // List of item IDs and actual items.  This is a running list with everything ever added to the auction.
    private HashMap<Integer, Item> itemsAndIDs = new HashMap<Integer, Item>();

    // List of itemIDs and the highest bid for each item.  This is a running list with everything ever added to the auction.
    private HashMap<Integer, Integer> highestBids = new HashMap<Integer, Integer>();

    // List of itemIDs and the person who made the highest bid for each item.   This is a running list with everything ever bid upon.
    private HashMap<Integer, String> highestBidders = new HashMap<Integer, String>();




    // List of sellers and how many items they have currently up for bidding.
    private HashMap<String, Integer> itemsPerSeller = new HashMap<String, Integer>();

    // List of buyers and how many items on which they are currently bidding.
    private HashMap<String, Integer> itemsPerBuyer = new HashMap<String, Integer>();



    // Object used for instance synchronization if you need to do it at some point
    // since as a good practice we don't use synchronized (this) if we are doing internal
    // synchronization.
    private Object instanceLock = new Object();



    /**
     * Attempt to submit an <code>Item</code> to the auction
     * @param sellerName Name of the <code>Seller</code>
     * @param itemName Name of the <code>Item</code>
     * @param lowestBiddingPrice Opening price
     * @param biddingDurationMs Bidding duration in milliseconds
     * @inv itemsUpForBidding.size() >= 0
     * @pre sellerName != null, itemName != null, lowestBiddingPrice >= 0, biddingDurationMs >= 0
     * @post if successful, itemsUpForBidding.size() = itemsUpForBidding.size()@pre + 1,
     * otherwise, itemsUpForBidding.size() = itemsUpForBidding.size()@pre
     * @return A positive, unique listing ID if the <code>Item</code> listed successfully, otherwise -1
     */
    public int submitItem(String sellerName, String itemName, int lowestBiddingPrice, int biddingDurationMs)
    {
        // TODO: IMPLEMENT CODE HERE
        // Some reminders:
        //Pseudo code:


        //   Make sure there's room in the auction site.
        //   If the seller is a new one, add them to the list of sellers.
        //   If the seller has too many items up for bidding, don't let them add this one.
        //   Don't forget to increment the number of things the seller has currently listed.

        // Check corner case
        if(sellerName==null||itemName==null||lowestBiddingPrice<=0||biddingDurationMs<=0)
            return -1;

        if (itemsUpForBidding.size() < serverCapacity ){
            // This block for existing seller
            if(itemsPerSeller.containsKey(sellerName)){
                // If exceeding the max, then return -1;
                if(itemsPerSeller.get(sellerName) == maxSellerItems || itemsPerSeller == null){
                    return -1;
                }
                else {
                    synchronized (instanceLock) {
                        lastListingID = lastListingID + 1;
                        Item item = new Item(sellerName, itemName, lastListingID, lowestBiddingPrice, biddingDurationMs);
                        int count = itemsPerSeller.get(sellerName) + 1;
                        itemsUpForBidding.add(item);
                        itemsAndIDs.put(lastListingID, item);
                        itemsPerSeller.put(sellerName, count);
                    }
                    return lastListingID;
                }


            }
            // This block for new seller
            else{
                synchronized (instanceLock) {
                    lastListingID = lastListingID + 1;
                    Item itemForNewSeller = new Item(sellerName, itemName, lastListingID, lowestBiddingPrice, biddingDurationMs);
                    itemsUpForBidding.add(itemForNewSeller);
                    itemsAndIDs.put(lastListingID, itemForNewSeller);
                    itemsPerSeller.put(sellerName, 1);
                }
                return lastListingID;

            }
        }
        return -1;
    }




    /**
     * Get all <code>Items</code> active in the auction
     * @return A copy of the <code>List</code> of <code>Items</code>
     * @pre itemsUpForBidding != null
     * @inv itemsUpForBidding.size() >= 0
     * @post return != null
     * @post itemsUpForBidding.size() == itemsUpForBidding.size() @pre
     */
    public List<Item> getItems()
    {
        // TODO: IMPLEMENT CODE HERE
		/*
        Check itemsUpForBidding is NOT NULL
            IF NOT NULL, return itemsUpForBidding list
            ELSE return new ArrayList<Item>();
            */
        // Some reminders:
        // Don't forget that whatever you return is now outside of your control.

        // Caution: Do not directly return itemsUpForBidding list;
        List<Item> getItems = new ArrayList<>();
        synchronized (instanceLock) {
            getItems.addAll(itemsUpForBidding);
            return getItems;
        }


    }


    /**
     * Attempt to submit a bid for an <code>Item</code>
     * @param bidderName Name of the <code>Bidder</code>
     * @param listingID Unique ID of the <code>Item</code>
     * @param biddingAmount Total amount to bid
     * @pre bidderName !=null,listingID != -1, biddingAmount >= 0
     * @post itemsUpForBidding.size() = itemsUpForBidding.size() @pre
     * @return True if successfully bid, false otherwise
     */
    public boolean submitBid(String bidderName, int listingID, int biddingAmount)
    {
        // TODO: IMPLEMENT CODE HERE
        // Some reminders:
        //   See if the item exists. // check if item exists in itemsForBidding

        /**Bias Policy:
         * 1. if two bidders with the same bidding amount: then this highestBidder will be allocated to the bidder who has the least cash spent;
         * 2. next: if each one with the same cash spent, then this highestBidder will be allocated to the bidder who has the least items bidding count;
         * 3. if items count that each bidder has been bidding also the same, then do nothing.
         */
        // Check corner case
        if(bidderName==null||listingID<0||biddingAmount<=0)
            return false;

        StringBuilder sb;
        synchronized(instanceLock) {
            for (Item item : getItems()) {
                //
                if (item.listingID() == listingID) {
                    //This block: for existing bidder
                    if (itemsPerBuyer.containsKey(bidderName)) {
                        //Normal case: new bidder bidding count not exceed maxCount
                        if (itemsPerBuyer.get(bidderName) < maxBidCount) {
                            if (highestBids.containsKey(listingID)) {
                                // Bidder is the one who is already the highestBidder, then bidding fails
                                if (highestBidders.get(listingID).equals(bidderName)) {
                                    return false;
                                }
                                else {
                                    if (highestBids.get(listingID) < biddingAmount) {
                                        String prevBidder = highestBidders.get(listingID);
                                        // Update highest bidding info and itemPerBuyer
                                        highestBidders.put(listingID, bidderName);
                                        highestBids.put(listingID, biddingAmount);
                                        itemsPerBuyer.put(prevBidder, itemsPerBuyer.get(prevBidder) - 1);
                                        itemsPerBuyer.put(bidderName, itemsPerBuyer.get(bidderName) + 1);
                                        return true;
                                    }

                                    // Check bias: Same price with different cash spent
                                    // Comparison standard: 1. biddingAmount 2. Cash Spent 3. Items being bidding
                                    else if(highestBids.get(listingID) == biddingAmount){
                                        sb = new StringBuilder();
                                        sb.append("Bias works status: ");
                                        String prevBidder = highestBidders.get(listingID);
                                        if(bidderCashSpent(prevBidder)>bidderCashSpent(bidderName)){
                                            highestBidders.put(listingID, bidderName);
                                            highestBids.put(listingID, biddingAmount);
                                            itemsPerBuyer.put(prevBidder, itemsPerBuyer.get(prevBidder) - 1);
                                            itemsPerBuyer.put(bidderName, itemsPerBuyer.get(bidderName) + 1);
                                            // Check bias policy actually works
                                            if(highestBidders.get(listingID).equals(bidderName))
                                                sb.append("Successfully");
                                            else
                                                sb.append("Wrong");
                                            biasCheck.add(sb.toString());
                                        }
                                        else if(bidderCashSpent(prevBidder)==bidderCashSpent(bidderName)){
                                            if(itemsPerBuyer.get(prevBidder)>itemsPerBuyer.get(bidderName))
                                                highestBidders.put(listingID, bidderName);
                                            highestBids.put(listingID, biddingAmount);
                                            itemsPerBuyer.put(prevBidder, itemsPerBuyer.get(prevBidder) - 1);
                                            itemsPerBuyer.put(bidderName, itemsPerBuyer.get(bidderName) + 1);
                                            // Check bias policy actually works
                                            if(highestBidders.get(listingID).equals(bidderName))
                                                sb.append("Successfully");
                                            else
                                                sb.append("Wrong");
                                            biasCheck.add(sb.toString());
                                        }

                                    }
                                    else
                                        return false;
                                }
                            }
                            // Existing bidder for a new Item
                            else {
                                highestBids.put(listingID, biddingAmount);
                                highestBidders.put(listingID, bidderName);
                                itemsPerBuyer.put(bidderName, itemsPerBuyer.get(bidderName) + 1);
                                return true;
                            }
                        }
                        else
                            return false;
                    }
                    else {
                        // This part for new Bidder who bids for this item first time
                        if (highestBids.containsKey(listingID)) {
                            //int highestBid = highestBids.get(listingID);
                            if (highestBids.get(listingID) < biddingAmount && itemsPerBuyer.size() < maxBidCount) {
                                // Get info about prev bidder
                                String prevBidder = highestBidders.get(listingID);
                                // Update itemsPerBuyer and bidding info for new bidder and previous one
                                highestBidders.put(listingID, bidderName);
                                highestBids.put(listingID, biddingAmount);
                                itemsPerBuyer.put(prevBidder, itemsPerBuyer.get(prevBidder)- 1);
                                itemsPerBuyer.put(bidderName, 1);
                                return true;
                            }
                            else
                                return false;
                        }
                        // This block: new bidder, item: lowestBiddingPrice
                        else {
                            highestBids.put(listingID, biddingAmount);
                            highestBidders.put(listingID, bidderName);
                            itemsPerBuyer.put(bidderName, 1);
                            return true;
                        }
                    }
                }
                // Having such item, but closed, so cannot accept bids
                else if (item.listingID() == listingID && !item.biddingOpen()) {
                    highestBids.remove(item);

                }
            }
        }
            // If there has no such item
            return false;
    }

    /**
     * Check the status of a <code>Bidder</code>'s bid on an <code>Item</code>
     * @param bidderName Name of <code>Bidder</code>
     * @param listingID Unique ID of the <code>Item</code>
     * @pre listingID >= 0, bidderName != null
     * @post return 1 or 2 or 3
     * @return 1 (success) if bid is over and this <code>Bidder</code> has won<br>
     * 2 (open) if this <code>Item</code> is still up for auction<br>
     * 3 (failed) If this <code>Bidder</code> did not win or the <code>Item</code> does not exist
     */
    public int checkBidStatus(String bidderName, int listingID)
    {
        // TODO: IMPLEMENT CODE HERE
        // Some reminders:
        //   If the bidding is closed, clean up for that item. // if bidduration returns false and there has been a bid, give it to buyer else emove it from the items list.
        //     Remove item from the list of things up for bidding. //
        //     Decrease the count of items being bid on by the winning bidder if there was any...//
        //     Update the number of open bids for this seller//

        // Check corner case
        if (bidderName == null || listingID < 0)
            return 3;

            synchronized (instanceLock) {
                if (itemsAndIDs.containsKey(listingID)) {
                    if (!itemsAndIDs.get(listingID).biddingOpen()) {
                        if (highestBidders.get(listingID).equals(bidderName)) {
                            // Update info for bidder and seller
                            itemsUpForBidding.remove(itemsAndIDs.get(listingID));
                            itemsPerBuyer.put(bidderName, itemsPerBuyer.get(bidderName) - 1);
                            String itemSeller = itemsAndIDs.get(listingID).seller();
                            itemsPerSeller.put(itemSeller, itemsPerSeller.get(itemSeller) - 1);
                            return 1;
                        }
                        else
                            return 3;
                    }
                    else
                        return 2;
                }
                return 3;
            }
    }

    /**
     * Check the current bid for an <code>Item</code>
     * @param listingID Unique ID of the <code>Item</code>
     * @return The highest bid so far or the opening price if no bid has been made,
     * @pre listingID >= 0
     * @post itemsUpForBidding.size() = itemsUpForBidding.size() @pre
     * -1 if no <code>Item</code> exists
     */
    public int itemPrice(int listingID)
    {
        // TODO: IMPLEMENT CODE HERE
        // item price  = highest price
        // if no bid has been made then itemPrice = opening price
        // return item price

        //Check corner case
        if (listingID < 0)
            return -1;
        synchronized (instanceLock) {
            // Two scenarios: this item is being bidding or ready for first time bidding
            if (highestBids.containsKey(listingID)) {
                return highestBids.get(listingID);
            } else {
                for (Item i : getItems())
                    if (i.listingID() == listingID)
                        return i.lowestBiddingPrice();
            }
            return -1;
        }
    }

    /**
     * Check whether an <code>Item</code> has been bid upon yet
     * @param listingID Unique ID of the <code>Item</code>
     * @pre listingID >= 0
     * @post return != null
     * @post itemsUpForBidding.size() = itemsUpForBidding.size() @pre
     * @return True if there is no bid or the <code>Item</code> does not exist, false otherwise
     */
    public  Boolean itemUnbid(int listingID)
    {
        // TODO: IMPLEMENT CODÏE HERE
        //

        // Check corner case
        if(listingID<0)
            return false;
        synchronized (instanceLock) {
            if (!(highestBids.containsKey(listingID))) {
                return true;
            }
        }
        return false;
    }
}


